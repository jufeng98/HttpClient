package org.javamaster.httpclient.reference.support

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.InheritanceUtil
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

        var hasAnno = false
        for (parameter in psiMethod.parameterList.parameters) {
            parameter.getAnnotation(REQUEST_BODY_ANNO_NAME) ?: continue

            hasAnno = true

            break
        }

        if (hasAnno) {
            return emptyArray()
        }

        val list = mutableListOf<LookupElementBuilder>()
        for (parameter in psiMethod.parameterList.parameters) {
            val psiClass = PsiTypeUtils.resolvePsiType(parameter.type)
            if (psiClass?.qualifiedName?.startsWith("java") == true) {
                list.addAll(collectVariants(parameter))
            } else {
                psiClass?.fields?.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    list.add(LookupElementBuilder.create(it))
                }

                val superClass = psiClass?.superClass
                if (superClass?.qualifiedName?.startsWith("java") == false) {
                    superClass.fields.forEach {
                        if (it.modifierList?.hasModifierProperty("static") == true) {
                            return@forEach
                        }

                        list.add(LookupElementBuilder.create(it))
                    }
                }
            }
        }

        return list.toTypedArray()
    }

    private fun collectVariants(parameter: PsiParameter): Array<LookupElementBuilder> {
        val type = parameter.type
        val name = type.toString()

        val isCollection = InheritanceUtil.isInheritor(type, "java.util.Collection")
        if (isCollection) {
            return arrayOf(LookupElementBuilder.create(parameter))
        } else if (name.contains("Boolean")) {
            return arrayOf(LookupElementBuilder.create(parameter))
        } else if (name.contains("Integer") || name.contains("int") || name.contains("Long") || name.contains("long")) {
            return arrayOf(LookupElementBuilder.create(parameter))
        } else if (name.contains("Double") || name.contains("double")) {
            return arrayOf(LookupElementBuilder.create(parameter))
        }

        val psiClass = PsiTypeUtils.resolvePsiType(type)
        if (psiClass == null) {
            return arrayOf(LookupElementBuilder.create(parameter))
        }

        return if (psiClass.qualifiedName?.startsWith("java") == true) {
            arrayOf(LookupElementBuilder.create(parameter))
        } else {
            psiClass.allFields
                .map { LookupElementBuilder.create(it) }
                .toList()
                .toTypedArray()
        }
    }

}
