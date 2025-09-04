package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.PsiUtils

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
        val virtualFile = jsonString.containingFile.virtualFile

        val paramPsiType = if (virtualFile?.name?.endsWith("res.http") == true) {
            controllerMethod.returnType
        } else {
            HttpUtils.resolveTargetParam(controllerMethod)?.type
        }

        val paramPsiCls: PsiClass = PsiUtils.resolvePsiType(paramPsiType) ?: return null

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        val jsonPropertyNameLevels = HttpUtils.collectJsonPropertyNameLevels(jsonString)

        return HttpUtils.resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
    }

}
