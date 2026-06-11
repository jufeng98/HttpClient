package org.javamaster.httpclient.inlay

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.psi.*
import org.javamaster.httpclient.enums.SpringHttpMethod
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
object HttpInlayHintsCollector : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is PsiLiteralExpression) {
            return
        }

        var nameValuePair = element.parent

        if (nameValuePair !is PsiNameValuePair) {
            nameValuePair = nameValuePair.parent

            if (nameValuePair !is PsiNameValuePair) {
                return
            }
        }

        val psiAnno = nameValuePair.parent.parent
        if (psiAnno !is PsiAnnotation) {
            return
        }

        SpringHttpMethod.getByQualifiedName(psiAnno.qualifiedName) ?: return

        if (psiAnno.parent?.parent !is PsiMethod) {
            return
        }

        val pointer = SmartPointerManager.getInstance(element.project).createSmartPsiElementPointer(element)

        @Suppress("DEPRECATION")
        sink.addPresentation(
            InlineInlayPosition(element.textRange.startOffset, false), null, NlsBundle.nls("create.http.req"), false
        ) {
            text("path", InlayActionData(PsiPointerInlayActionPayload(pointer), "HttpInlayHintsCollector"))
        }
    }

}
