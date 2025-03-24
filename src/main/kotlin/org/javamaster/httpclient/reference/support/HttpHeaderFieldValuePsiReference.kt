package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.DubboUtils

/**
 * @author yudong
 */
class HttpHeaderFieldValuePsiReference(fieldValue: HttpHeaderFieldValue, range: TextRange) :
    PsiReferenceBase<HttpHeaderFieldValue>(fieldValue, range) {

    override fun resolve(): PsiElement? {
        val headerField = element.parent as HttpHeaderField
        val fieldName = headerField.name

        if (fieldName == DubboUtils.INTERFACE_KEY) {
            return resolveInterface(element)
        }

        if (fieldName == DubboUtils.METHOD_KEY) {
            return resolveMethod(headerField, element.text)
        }

        return null
    }

    private fun resolveInterface(fieldValue: HttpHeaderFieldValue): PsiClass? {
        val module = DubboUtils.getOriginalModule(fieldValue) ?: return null

        return DubboUtils.findInterface(module, fieldValue.text)
    }

    private fun resolveMethod(headerField: HttpHeaderField, methodName: String): PsiMethod? {
        val httpRequest = PsiTreeUtil.getParentOfType(headerField, HttpRequest::class.java)!!

        val interfaceFieldValue = httpRequest.header?.interfaceField?.headerFieldValue ?: return null

        val psiClass = resolveInterface(interfaceFieldValue) ?: return null

        ClassInheritorsSearch.search(psiClass)
            .forEach {
                val methods = it.findMethodsByName(methodName, false)
                if (methods.isEmpty()) {
                    return null
                }

                if (methods.size > 1) {
                    println("解析到${methods.size}多个同名方法,直接取第一个")
                }

                return methods[0]
            }

        return null
    }
}