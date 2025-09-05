package org.javamaster.httpclient.inlay

import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.javamaster.httpclient.enums.Control

/**
 * @author yudong
 */
class UrlInlineHintsProvider : InlayHintsProvider {

    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        if (file !is PsiJavaFile) {
            return null
        }

        val noControllerCls = file.classes.all {
            it.getAnnotation(Control.Controller.qualifiedName) == null
                    && it.getAnnotation(Control.RestController.qualifiedName) == null
        }

        if (noControllerCls) {
            return null
        }

        return HttpInlayHintsCollector
    }

}
