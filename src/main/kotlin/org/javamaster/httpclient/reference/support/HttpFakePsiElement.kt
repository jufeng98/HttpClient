package org.javamaster.httpclient.reference.support

import com.cool.request.view.tool.search.ApiAbstractGotoSEContributor
import com.cool.request.view.tool.search.ControllerNavigationItem
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.ide.DataManager
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.TaskInfo
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.TooltipUtils
import java.util.concurrent.CompletableFuture

/**
 * @author yudong
 */
class HttpFakePsiElement(private val httpRequestTarget: HttpRequestTarget, private val searchTxt: String) :
    ASTWrapperPsiElement(httpRequestTarget.node) {

    override fun getPresentation(): ItemPresentation {
        return HttpItemPresentation
    }

    override fun navigate(requestFocus: Boolean) {
        val virtualFile = httpRequestTarget.containingFile.virtualFile
        val module = if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            HttpUtils.getOriginalModule(httpRequestTarget)
        } else {
            ModuleUtil.findModuleForPsiElement(httpRequestTarget)
        }

        if (module == null) {
            return
        }

        val event = createEvent()

        val processIndicator = createProcessIndicator("Tip:正在搜索对应的Controller...", project)
        Disposer.register(Disposer.newDisposable(), processIndicator)

        val seContributor = ApiAbstractGotoSEContributor(event)

        CompletableFuture.runAsync {
            val list = seContributor.search(searchTxt, processIndicator)
            if (processIndicator.isCanceled) {
                processIndicator.processFinish()
                return@runAsync
            }

            processIndicator.processFinish()

            runInEdt {
                if (list.isEmpty()) {
                    showTip("Tip:未能解析到对应的controller mapping,无法跳转", project)
                    return@runInEdt
                }

                val controllerNavigationItem = getControllerNavigationItem(list, searchTxt)

                runReadAction {
                    val psiMethods = findControllerPsiMethods(controllerNavigationItem, module)
                    if (psiMethods.isEmpty()) {
                        showTip("Tip:未能解析对应的controller方法,无法跳转", project)
                        return@runReadAction
                    }

                    if (psiMethods.size > 1) {
                        showTip("Tip:解析到${psiMethods.size}个的controller方法,无法跳转", project)
                        return@runReadAction
                    }

                    val psiMethod = psiMethods[0]
                    psiMethod.navigate(true)
                }
            }
        }

    }

    companion object {
        fun showTip(msg: String, project: Project) {
            TooltipUtils.showTooltip(msg, project)
        }

        @Suppress("DEPRECATION")
        fun createEvent(): AnActionEvent {
            @Suppress("removal")
            return AnActionEvent(
                null,
                DataManager.getInstance().dataContext,
                "",
                Presentation(""),
                ActionManager.getInstance(),
                1
            )
        }

        fun createProcessIndicator(title: String, project: Project): BackgroundableProcessIndicator {
            return BackgroundableProcessIndicator(project, object : TaskInfo {
                override fun getTitle(): String {
                    return title
                }

                override fun getCancelText(): String {
                    return "Tip:正在取消......"
                }

                override fun getCancelTooltipText(): String {
                    return "Tip:取消中......"
                }

                override fun isCancellable(): Boolean {
                    return true
                }
            })
        }

        fun findControllerPsiMethods(
            navigationItem: ControllerNavigationItem,
            module: Module,
        ): Array<out PsiMethod> {
            val controllerFullClassName = navigationItem.javaClassName
            val controllerMethodName = navigationItem.methodName

            val controllerPsiCls = JavaPsiFacade.getInstance(module.project)
                .findClass(
                    controllerFullClassName,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
                ) ?: return arrayOf()

            val psiMethods = controllerPsiCls.findMethodsByName(controllerMethodName, false)
            if (psiMethods.isEmpty()) {
                return arrayOf()
            }

            return psiMethods
        }

        fun getControllerNavigationItem(list: MutableList<Any>, searchTxt: String): ControllerNavigationItem {
            return if (list.size == 1) {
                list[0] as ControllerNavigationItem
            } else {
                val urlMap = list.groupBy {
                    val navigationItem = it as ControllerNavigationItem
                    navigationItem.url
                }
                val itemList = urlMap[searchTxt]
                if (itemList.isNullOrEmpty()) {
                    list[0] as ControllerNavigationItem
                } else {
                    itemList[0] as ControllerNavigationItem
                }
            }
        }
    }
}
