package org.javamaster.httpclient.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType

/**
 * @author yudong
 */
object PsiTypeUtils {

    fun resolvePsiType(psiType: PsiType?): PsiClass? {
        if (psiType !is PsiClassReferenceType) {
            return null
        }

        return psiType.resolve()
    }

    fun collectFields(psiClass: PsiClass): MutableList<PsiField> {
        val fields = mutableListOf<PsiField>()

        fields.addAll(psiClass.fields)

        val superClass = psiClass.superClass

        if (superClass != null) {
            val collectFields = collectFields(superClass)

            fields.addAll(collectFields)
        }

        return fields
    }
}
