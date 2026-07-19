package org.javamaster.httpclient.reference.support

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.javamaster.httpclient.completion.support.HttpSuffixInsertHandler.Companion.QUERY_SEPARATOR
import org.javamaster.httpclient.consts.HttpConsts.Companion.REQUEST_BODY_ANNO_NAME
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

    override fun getVariants(): Array<Any> {
        return collectVariants(controllerMethod)
    }

    private fun collectVariants(psiMethod: PsiMethod?): Array<Any> {
        if (psiMethod == null) {
            return emptyArray()
        }

        val list = mutableListOf<LookupElementBuilder>()
        for (parameter in psiMethod.parameterList.parameters) {
            val annotation = parameter.getAnnotation(REQUEST_BODY_ANNO_NAME)
            if (annotation != null) {
                continue
            }

            val psiType = parameter.type
            if (psiType is PsiPrimitiveType || psiType is PsiArrayType) {
                list.add(LookupElementBuilder.create(parameter).withInsertHandler(QUERY_SEPARATOR))
                continue
            }

            val psiClass = PsiTypeUtils.resolvePsiType(psiType) ?: continue

            if (psiClass.qualifiedName?.startsWith("java") == true) {
                list.add(LookupElementBuilder.create(parameter).withInsertHandler(QUERY_SEPARATOR))
                continue
            }

            psiClass.fields.forEach {
                if (it.modifierList?.hasModifierProperty("static") == true) {
                    return@forEach
                }

                list.add(LookupElementBuilder.create(it).withInsertHandler(QUERY_SEPARATOR))
            }

            val superClass = psiClass.superClass ?: continue

            if (superClass.qualifiedName?.startsWith("java") == false) {
                superClass.fields.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    list.add(LookupElementBuilder.create(it).withInsertHandler(QUERY_SEPARATOR))
                }
            }
        }

        return list.toTypedArray()
    }

}
