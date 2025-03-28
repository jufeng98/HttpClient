package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.reference.support.JsonKeyPsiReference
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsonKeyPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonString = element as JsonStringLiteral
        if (!jsonString.isPropertyName) {
            return arrayOf()
        }

        val injectionHost = InjectedLanguageManager.getInstance(jsonString.project).getInjectionHost(jsonString)
        if (injectionHost !is HttpMessageBody) {
            return arrayOf()
        }

        val textRange = jsonString.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        val httpRequest = PsiTreeUtil.getParentOfType(injectionHost, HttpRequest::class.java)
        val httpMethod = httpRequest?.method ?: return arrayOf()
        val methodType = httpMethod.text

        if (methodType == HttpRequestEnum.DUBBO.name) {
            val originalModule = DubboUtils.getOriginalModule(httpRequest) ?: return arrayOf()
            return arrayOf(JsonKeyPsiReference(jsonString, "", originalModule, range))
        }

        val requestTarget = httpRequest.requestTarget ?: return arrayOf()

        val originalFile = HttpUtils.getOriginalFile(requestTarget) ?: return arrayOf()
        val originalModule = ModuleUtilCore.findModuleForFile(originalFile, requestTarget.project) ?: return arrayOf()

        val pair = HttpUtils.getSearchTxtInfo(requestTarget, originalFile.parent.path) ?: return arrayOf()

        return arrayOf(JsonKeyPsiReference(jsonString, pair.first, originalModule, range))
    }

}