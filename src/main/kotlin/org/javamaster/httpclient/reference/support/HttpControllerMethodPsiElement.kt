package org.javamaster.httpclient.reference.support

import com.cool.request.view.tool.search.ApiAbstractGotoSEContributor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.Disposer
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.createActionEvent
import org.javamaster.httpclient.utils.HttpUtils.createProcessIndicator
import org.javamaster.httpclient.utils.HttpUtils.findControllerPsiMethods
import org.javamaster.httpclient.utils.HttpUtils.getControllerNavigationItem
import org.javamaster.httpclient.utils.TooltipUtils.showTooltip
import java.util.concurrent.CompletableFuture
import javax.swing.Icon

/**
 * @author yudong
 */
class HttpControllerMethodPsiElement(private val requestTarget: HttpRequestTarget, private val searchTxt: String) :
    ASTWrapperPsiElement(requestTarget.node) {

    override fun getPresentation(): ItemPresentation {
        return HttpItemPresentation
    }

    override fun navigate(requestFocus: Boolean) {
        val virtualFile = requestTarget.containingFile.virtualFile
        val module = if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            HttpUtils.getOriginalModule(requestTarget)
        } else {
            ModuleUtil.findModuleForPsiElement(requestTarget)
        }

        if (module == null) {
            return
        }

        val event = createActionEvent()

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
                    showTooltip("Tip:未能解析到对应的controller mapping,无法跳转", project)
                    return@runInEdt
                }

                val controllerNavigationItem = getControllerNavigationItem(list, searchTxt)

                runReadAction {
                    val psiMethods = findControllerPsiMethods(controllerNavigationItem, module)
                    if (psiMethods.isEmpty()) {
                        showTooltip("Tip:未能解析对应的controller方法,无法跳转", project)
                        return@runReadAction
                    }

                    if (psiMethods.size > 1) {
                        showTooltip("Tip:解析到${psiMethods.size}个的controller方法,无法跳转", project)
                        return@runReadAction
                    }

                    val psiMethod = psiMethods[0]
                    psiMethod.navigate(true)
                }
            }
        }

    }

    object HttpItemPresentation : ItemPresentation {

        override fun getPresentableText(): String {
            return "跳转到对应的 Controller 接口方法"
        }

        override fun getIcon(unused: Boolean): Icon {
            return HttpIcons.REQUEST_MAPPING
        }

    }

}
