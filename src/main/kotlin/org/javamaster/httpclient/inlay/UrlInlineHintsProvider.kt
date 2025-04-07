package org.javamaster.httpclient.inlay

import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

/**
 * @author yudong
 */
class UrlInlineHintsProvider : InlayHintsProvider {

    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        if (file !is PsiJavaFile) {
            return null
        }

        return HttpInlayHintsCollector
    }

}
