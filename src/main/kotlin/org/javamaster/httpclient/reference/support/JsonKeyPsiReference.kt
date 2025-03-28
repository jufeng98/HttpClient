package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * @author yudong
 */
class JsonKeyPsiReference(
    private val jsonString: JsonStringLiteral,
    private val searchTxt: String,
    private val module: Module,
    range: TextRange,
) :
    PsiReferenceBase<JsonStringLiteral>(jsonString, range) {

    override fun resolve(): PsiElement {
        return JsonControllerMethodFieldPsiElement(jsonString, searchTxt, module)
    }

}
