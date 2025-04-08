package org.javamaster.httpclient.inlay

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.psi.*
import org.javamaster.httpclient.enums.SpringHttpMethod

/**
 * @author yudong
 */
object HttpInlayHintsCollector : SharedBypassCollector {
    private val mvcAnnoSet = setOf(
        SpringHttpMethod.REQUEST_MAPPING.qualifiedName,
        SpringHttpMethod.GET_MAPPING.qualifiedName,
        SpringHttpMethod.POST_MAPPING.qualifiedName,
        SpringHttpMethod.PATCH_MAPPING.qualifiedName,
        SpringHttpMethod.PUT_MAPPING.qualifiedName,
        SpringHttpMethod.DELETE_MAPPING.qualifiedName,
    )

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is PsiLiteralExpression) {
            return
        }

        val nameValuePair = element.parent
        if (nameValuePair !is PsiNameValuePair) {
            return
        }

        val psiAnno = nameValuePair.parent.parent
        if (psiAnno !is PsiAnnotation) {
            return
        }

        if (!mvcAnnoSet.contains(psiAnno.qualifiedName)) {
            return
        }

        val pointer = SmartPointerManager.getInstance(element.project).createSmartPsiElementPointer(element)

        @Suppress("DEPRECATION")
        sink.addPresentation(
            InlineInlayPosition(element.textRange.startOffset, false),
            null,
            "Create http request",
            false
        ) {
            text(
                "url",
                InlayActionData(PsiPointerInlayActionPayload(pointer), "HttpInlayHintsCollector")
            )
        }
    }

}
