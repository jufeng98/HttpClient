package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.PsiUtils

/**
 * @author yudong
 */
class JsonKeyDubboMethodFieldPsiReference(
    private val jsonString: JsonStringLiteral,
    range: TextRange,
) :
    PsiReferenceBase<JsonStringLiteral>(jsonString, range) {

    override fun resolve(): PsiElement? {
        val virtualFile = jsonString.containingFile.virtualFile

        val it = DubboUtils.findDubboServiceMethod(jsonString) ?: return null

        val jsonPropertyNameLevels = HttpUtils.collectJsonPropertyNameLevels(jsonString)

        val paramPsiType: PsiType?
        if (virtualFile?.name?.endsWith("res.http") == true) {
            paramPsiType = it.returnType
        } else {
            val name = jsonPropertyNameLevels.pop()
            val psiParameter = it.parameterList.parameters.firstOrNull { parameter -> parameter.name == name }

            if (psiParameter == null) {
                return null
            }

            if (jsonPropertyNameLevels.isEmpty()) {
                return psiParameter
            }

            paramPsiType = psiParameter.type
        }

        val paramPsiCls = PsiUtils.resolvePsiType(paramPsiType) ?: return null

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        return HttpUtils.resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
    }


}
