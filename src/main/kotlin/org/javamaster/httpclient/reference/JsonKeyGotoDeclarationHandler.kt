package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils

/**
 *实现 Ctrl + 点击 json 属性进而跳转到 Spring 对应的 Controller 方法的出入参的 Bean 字段
 *
 * @author yudong
 */
class JsonKeyGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?,
    ): Array<PsiElement> {
        if (element == null) {
            return arrayOf()
        }

        val jsonString = element.parent
        if (jsonString !is JsonStringLiteral) {
            return arrayOf()
        }

        if (!jsonString.isPropertyName) {
            return arrayOf()
        }

        val injectionHost = InjectedLanguageManager.getInstance(jsonString.project).getInjectionHost(jsonString)
        if (injectionHost !is HttpMessageBody) {
            return arrayOf()
        }

        val httpRequest = PsiTreeUtil.getParentOfType(injectionHost, HttpRequest::class.java)
        val httpMethod = httpRequest?.method ?: return arrayOf()
        val methodType = httpMethod.text

        if (methodType == HttpRequestEnum.DUBBO.name) {
            val originalModule = DubboUtils.getOriginalModule(httpRequest) ?: return arrayOf()
            return arrayOf(JsonFakePsiElement(jsonString, "", originalModule))
        }

        val requestTarget = httpRequest.requestTarget ?: return arrayOf()

        val originalFile = HttpUtils.getOriginalFile(requestTarget) ?: return arrayOf()
        val originalModule = ModuleUtilCore.findModuleForFile(originalFile, requestTarget.project) ?: return arrayOf()

        val pair = HttpUtils.getSearchTxtInfo(requestTarget, originalFile.parent.path) ?: return arrayOf()

        return arrayOf(JsonFakePsiElement(jsonString, pair.first, originalModule))
    }

}
