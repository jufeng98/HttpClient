package org.javamaster.httpclient.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.elementType
import com.intellij.util.Function
import org.javamaster.httpclient.gutter.support.HttpGutterIconNavigationHandler
import org.javamaster.httpclient.gutter.support.HttpLineMarkerInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.utils.HttpUtils
import java.util.function.Supplier

/**
 * @author yudong
 */
class HttpLineMarkerProvider : LineMarkerProvider {
    private val tooltipProvider by lazy { Function { _: PsiElement -> NlsBundle.nls("send.request") } }
    private val accessibleNameProvider by lazy { Supplier { NlsBundle.nls("send.request") } }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        val elementType = element.elementType
        if (elementType != HttpTypes.REQUEST_METHOD) {
            return null
        }

        val parent = element.parent
        if (parent !is HttpMethod) {
            return null
        }

        val virtualFile = PsiUtil.getVirtualFile(element)
        if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            return null
        }

        return HttpLineMarkerInfo(
            element, element.textRange, AllIcons.Actions.Execute, tooltipProvider, HttpGutterIconNavigationHandler,
            GutterIconRenderer.Alignment.CENTER, accessibleNameProvider
        )
    }

}
