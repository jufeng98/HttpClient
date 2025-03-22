package org.javamaster.httpclient.reference.support

import com.intellij.psi.*
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.DubboUtils

class HttpHeaderPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val fieldValue = element as HttpHeaderFieldValue
        val rangeInElement = textRange.shiftLeft(textRange.startOffset)

        val psiReference = object : PsiReferenceBase<HttpHeaderFieldValue>(fieldValue, rangeInElement) {

            override fun resolve(): PsiElement? {
                val headerField = fieldValue.parent as HttpHeaderField
                val fieldName = headerField.name

                if (fieldName == DubboUtils.INTERFACE_KEY) {
                    return resolveInterface(fieldValue)
                }

                if (fieldName == DubboUtils.METHOD_KEY) {
                    return resolveMethod(headerField)
                }

                return null
            }

            private fun resolveInterface(fieldValue: HttpHeaderFieldValue?): PsiClass? {
                if (fieldValue == null) {
                    return null
                }

                val module = DubboUtils.getOriginalModule(fieldValue) ?: return null
                val interfaceName = fieldValue.text
                return DubboUtils.findInterface(module, interfaceName)
            }

            private fun resolveMethod(headerField: HttpHeaderField): PsiMethod? {
                val httpRequest = PsiTreeUtil.getParentOfType(headerField, HttpRequest::class.java)!!

                val field = httpRequest.header?.interfaceField ?: return null

                val psiClass = resolveInterface(field.headerFieldValue) ?: return null

                val methodName = fieldValue.text

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

        return arrayOf(psiReference)
    }
}
