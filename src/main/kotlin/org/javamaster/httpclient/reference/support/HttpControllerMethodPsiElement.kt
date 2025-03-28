package org.javamaster.httpclient.reference.support

import com.cool.request.view.tool.search.ApiAbstractGotoSEContributor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.util.Disposer
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.doc.support.CoolRequestHelper
import org.javamaster.httpclient.doc.support.CoolRequestHelper.findControllerNavigationItem
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.utils.HttpUtils.createActionEvent
import org.javamaster.httpclient.utils.HttpUtils.createProcessIndicator
import org.javamaster.httpclient.utils.TooltipUtils.showTooltip
import java.util.concurrent.CompletableFuture
import javax.swing.Icon

/**
 * @author yudong
 */
class HttpControllerMethodPsiElement(val requestTarget: HttpRequestTarget, val searchTxt: String) :
    ASTWrapperPsiElement(requestTarget.node) {

    override fun getPresentation(): ItemPresentation {
        return HttpItemPresentation
    }

    override fun navigate(requestFocus: Boolean) {
        val virtualFile = requestTarget.containingFile.virtualFile
        val module = CoolRequestHelper.findModule(requestTarget, virtualFile) ?: return

        val event = createActionEvent()

        val processIndicator = createProcessIndicator("Tip:正在搜索对应的 Controller ...", project)
        Disposer.register(Disposer.newDisposable(), processIndicator)

        val seContributor = ApiAbstractGotoSEContributor(event)

        CompletableFuture.runAsync {
            val controllers = seContributor.search(searchTxt, processIndicator)
            if (processIndicator.isCanceled) {
                processIndicator.processFinish()
                return@runAsync
            }

            processIndicator.processFinish()

            if (controllers.isEmpty()) {
                runInEdt {
                    showTooltip("Tip:未能解析到对应的 Controller,无法跳转", project)
                }
                return@runAsync
            }

            val controllerNavigationItem = findControllerNavigationItem(controllers, searchTxt)

            HttpBackground
                .runInBackgroundReadActionAsync {
                    CoolRequestHelper.findControllerPsiMethods(controllerNavigationItem, module)
                }
                .finishOnUiThread {
                    if (it!!.isEmpty()) {
                        showTooltip("Tip:未能解析对应的 Controller 方法,无法跳转", project)
                        return@finishOnUiThread
                    }

                    if (it.size > 1) {
                        showTooltip("Tip:解析到 ${it.size} 个 Controller 方法,无法跳转", project)
                        return@finishOnUiThread
                    }

                    it[0].navigate(true)
                }
                .exceptionallyOnUiThread {
                    showTooltip("Tip:$it", project)
                }
        }

    }

    object HttpItemPresentation : ItemPresentation {

        override fun getPresentableText(): String {
            return "跳转到对应的 Controller 方法"
        }

        override fun getIcon(unused: Boolean): Icon {
            return HttpIcons.REQUEST_MAPPING
        }

    }

}
