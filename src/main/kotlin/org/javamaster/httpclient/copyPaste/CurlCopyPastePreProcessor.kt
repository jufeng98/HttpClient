package org.javamaster.httpclient.copyPaste

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.action.addHttp.ImportCurlAction.Companion.toHttpRequest
import org.javamaster.httpclient.curl.CurlParser
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.utils.CurlUtils.isCurlString


/**
 * @author yudong
 */
class CurlCopyPastePreProcessor : CopyPastePreProcessor {

    override fun preprocessOnCopy(
        file: PsiFile?,
        startOffsets: IntArray?,
        endOffsets: IntArray?,
        text: String?,
    ): String? {
        return null
    }

    override fun preprocessOnPaste(
        project: Project?,
        file: PsiFile?,
        editor: Editor,
        text: String,
        rawText: RawText?,
    ): String {
        if (file !is HttpFile) {
            return text
        }

        if (!isCurlString(text)) {
            return text
        }

        try {
            val curlParser = CurlParser(text)

            val request = curlParser.parseToCurlRequest()

            return toHttpRequest(request, text)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return text
    }
}
