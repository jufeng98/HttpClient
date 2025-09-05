package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.utils.DubboUtils

/**
 * @author yudong
 */
class JsonKeyDubboMethodFieldPsiReference(
    private val jsonString: JsonStringLiteral,
    range: TextRange,
) :
    PsiReferenceBase<JsonStringLiteral>(jsonString, range) {

    override fun resolve(): PsiElement? {
        return DubboUtils.resolveTargetPsiElement(jsonString)
    }


}
