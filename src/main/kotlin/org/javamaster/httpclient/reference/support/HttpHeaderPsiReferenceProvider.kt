package org.javamaster.httpclient.reference.support

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.TooltipUtils

class HttpHeaderPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val fieldValue = element as HttpHeaderFieldValue
        val rangeInElement = textRange.shiftLeft(textRange.startOffset)

        val psiReference = object : PsiReferenceBase<HttpHeaderFieldValue>(fieldValue, rangeInElement) {
            override fun resolve(): PsiElement {
                return HttpHeaderValuePathFakePsiElement(fieldValue)
            }
        }

        return arrayOf(psiReference)
    }

    class HttpHeaderValuePathFakePsiElement(private val fieldValue: HttpHeaderFieldValue) :
        ASTWrapperPsiElement(fieldValue.node) {

        override fun navigate(requestFocus: Boolean) {
            val headerField = fieldValue.parent as HttpHeaderField
            val fieldName = headerField.headerFieldName.text

            if (fieldName == DubboUtils.INTERFACE_KEY) {
                val module = DubboUtils.getOriginalModule(fieldValue) ?: return
                val interfaceName = fieldValue.text
                val psiClass = DubboUtils.findInterface(module, interfaceName)
                if (psiClass == null) {
                    TooltipUtils.showTooltip("跳转接口失败,无法解析接口:${interfaceName}", project)
                    return
                }

                psiClass.navigate(requestFocus)
                return
            }

            if (fieldName == DubboUtils.METHOD_KEY) {
                val httpRequest = headerField.parent as HttpRequest
                val field = httpRequest.headerFieldList
                    .firstOrNull {
                        it.headerFieldName.text == DubboUtils.INTERFACE_KEY
                    }
                if (field == null) {
                    TooltipUtils.showTooltip("跳转失败,未找到 ${DubboUtils.INTERFACE_KEY} 请求头", project)
                    return
                }

                val module = DubboUtils.getOriginalModule(fieldValue) ?: return

                val interfaceName = field.headerFieldValue?.text ?: return
                val psiClass = DubboUtils.findInterface(module, interfaceName)
                if (psiClass == null) {
                    TooltipUtils.showTooltip("跳转方法失败,无法解析接口:${interfaceName}", project)
                    return
                }

                val methodName = fieldValue.text

                ClassInheritorsSearch.search(psiClass)
                    .forEach {
                        val methods = it.findMethodsByName(methodName, false)
                        if (methods.isEmpty()) {
                            TooltipUtils.showTooltip("跳转方法失败,在${it.name}中无法解析方法:${methodName}", project)
                            return
                        }

                        if (methods.size > 1) {
                            NotifyUtil.notifyWarn(project, "在${interfaceName}中找到多个同名方法,简单跳转第一个")
                            return
                        }

                        methods[0].navigate(requestFocus)
                        return
                    }
            }
        }
    }
}
