package org.javamaster.httpclient.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.elementType
import com.intellij.util.Function
import org.javamaster.httpclient.gutter.support.HttpDiffGutterIconNavigationHandler
import org.javamaster.httpclient.gutter.support.HttpGutterIconNavigationHandler
import org.javamaster.httpclient.gutter.support.HttpLineMarkerInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpHistoryBodyFile
import org.javamaster.httpclient.psi.HttpHistoryBodyFileList
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
    private val tip by lazy { NlsBundle.nls("compare.with") + "..." }

    private val tooltipCompareProvider by lazy { Function { _: PsiElement -> tip } }
    private val accessibleNameCompareProvider by lazy { Supplier { tip } }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        val elementType = element.elementType

        if (elementType == HttpTypes.REQUEST_METHOD) {
            return createRunIconInfo(element)
        }

        if (elementType == HttpTypes.HISTORY_FILE_SIGN) {
            return createDiffIconInfo(element)
        }

        return null
    }

    private fun createRunIconInfo(element: PsiElement): HttpLineMarkerInfo? {
        val parent = element.parent
        if (parent !is HttpMethod) {
            return null
        }

        val virtualFile = PsiUtil.getVirtualFile(element)
        if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            return null
        }

        return HttpLineMarkerInfo(
            element, element.textRange, AllIcons.Actions.Execute,
            tooltipProvider, HttpGutterIconNavigationHandler,
            GutterIconRenderer.Alignment.CENTER, accessibleNameProvider
        )
    }

    private fun createDiffIconInfo(element: PsiElement): HttpLineMarkerInfo? {
        val historyBodyFile = element.parent as HttpHistoryBodyFile
        val bodyFileList = historyBodyFile.parent as HttpHistoryBodyFileList

        if (historyBodyFile.filePath == null || bodyFileList.historyBodyFileList.size <= 1) return null

        return HttpLineMarkerInfo(
            element, element.textRange, AllIcons.Actions.Diff,
            tooltipCompareProvider, HttpDiffGutterIconNavigationHandler,
            GutterIconRenderer.Alignment.CENTER, accessibleNameCompareProvider
        )
    }

}
