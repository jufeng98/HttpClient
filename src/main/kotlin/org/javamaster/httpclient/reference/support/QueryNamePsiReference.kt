package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.utils.PsiTypeUtils

/**
 * @author yudong
 */
class QueryNamePsiReference(
    psiElement: PsiElement,
    val textRange: TextRange,
    private val controllerMethod: PsiMethod?,
    private val queryName: String,
) :
    PsiReferenceBase<PsiElement>(psiElement, textRange) {

    override fun resolve(): PsiElement? {
        controllerMethod ?: return null

        for (parameter in controllerMethod.parameterList.parameters) {
            if (parameter.name == queryName) {
                return parameter
            }

            val paramPsiType = parameter.type
            val paramPsiCls = PsiTypeUtils.resolvePsiType(paramPsiType) ?: continue

            return paramPsiCls.allFields.firstOrNull { it.name == queryName } ?: continue
        }

        return null
    }

}
