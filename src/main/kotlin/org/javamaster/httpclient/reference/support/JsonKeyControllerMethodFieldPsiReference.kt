package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.PsiTypeUtils

/**
 * @author yudong
 */
class JsonKeyControllerMethodFieldPsiReference(
    private val jsonString: JsonStringLiteral,
    private val controllerMethod: PsiMethod,
    range: TextRange,
) :
    PsiReferenceBase<JsonStringLiteral>(jsonString, range) {

    override fun resolve(): PsiElement? {
        val paramPsiType = HttpUtils.getUrlControllerMethodParamType(jsonString, controllerMethod)

        val paramPsiCls = PsiTypeUtils.resolvePsiType(paramPsiType) ?: return null

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        val jsonPropertyNameLevels = HttpUtils.collectJsonPropertyNameLevels(jsonString)

        return HttpUtils.resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
    }

}
