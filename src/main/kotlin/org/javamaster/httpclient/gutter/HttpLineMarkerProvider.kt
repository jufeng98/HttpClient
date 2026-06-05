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
import org.javamaster.httpclient.gutter.support.HttpRunGutterIconNavigationHandler
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.*
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

    private val tooltipRunProvider by lazy { Function { _: PsiElement -> "Run" } }
    private val accessibleNameRunProvider by lazy { Supplier { "Run" } }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        val elementType = element.elementType

        if (elementType == HttpTypes.REQUEST_METHOD) {
            return createRunIconInfo(element)
        }

        if (elementType == HttpTypes.HISTORY_FILE_SIGN) {
            return createDiffIconInfo(element)
        }

        if (elementType == HttpTypes.RUN) {
            return createRunCommandIconInfo(element)
        }

        return null
    }

    private fun createRunIconInfo(element: PsiElement): HttpLineMarkerInfo? {
        val method = element.parent
        if (method !is HttpMethod) {
            return null
        }

        val virtualFile = PsiUtil.getVirtualFile(method)
        if (HttpUtils.isFileInHistoryDir(virtualFile, method.project)) {
            return null
        }

        return HttpLineMarkerInfo(
            element, element.textRange, AllIcons.Actions.Execute,
            tooltipProvider, HttpGutterIconNavigationHandler,
            GutterIconRenderer.Alignment.CENTER, accessibleNameProvider
        )
    }

    private fun createDiffIconInfo(element: PsiElement): HttpLineMarkerInfo? {
        val historyBodyFile = element.parent
        if (historyBodyFile !is HttpHistoryBodyFile) {
            return null
        }

        if (historyBodyFile.filePath == null) {
            return null
        }

        val bodyFileList = historyBodyFile.parent as HttpHistoryBodyFileList

        if (bodyFileList.historyBodyFileList.size <= 1) return null

        return HttpLineMarkerInfo(
            element, element.textRange, AllIcons.Actions.Diff,
            tooltipCompareProvider, HttpDiffGutterIconNavigationHandler,
            GutterIconRenderer.Alignment.CENTER, accessibleNameCompareProvider
        )
    }

    private fun createRunCommandIconInfo(element: PsiElement): HttpLineMarkerInfo? {
        val runCommand = element.parent
        if (runCommand !is HttpRunCommand) {
            return null
        }

        val virtualFile = PsiUtil.getVirtualFile(runCommand)
        if (HttpUtils.isFileInHistoryDir(virtualFile, runCommand.project)) {
            return null
        }

        return HttpLineMarkerInfo(
            element, element.textRange, AllIcons.Actions.Execute,
            tooltipRunProvider, HttpRunGutterIconNavigationHandler,
            GutterIconRenderer.Alignment.CENTER, accessibleNameRunProvider
        )
    }

}
