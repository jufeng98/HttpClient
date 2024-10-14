package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.psi.HttpOrdinaryContent
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.psi.HttpUrl
import org.javamaster.httpclient.utils.HttpUtils

/**
 *实现 Ctrl + 点击 json 属性进而跳转到 Spring 对应的 Controller 方法的出入参的 Bean 字段
 *
 * @author yudong
 */
class JsonGotoDeclarationHandler : GotoDeclarationHandler {

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
        if (injectionHost !is HttpOrdinaryContent) {
            return arrayOf()
        }

        val httpRequest = PsiTreeUtil.getParentOfType(injectionHost, HttpRequest::class.java)
        val httpUrl = PsiTreeUtil.getChildOfType(httpRequest, HttpUrl::class.java) ?: return arrayOf()

        val originalFile = HttpUtils.getOriginalFile(httpUrl) ?: return arrayOf()
        val originalModule = ModuleUtilCore.findModuleForFile(originalFile, httpUrl.project) ?: return arrayOf()

        val pair = HttpUtils.getSearchTxtInfo(httpUrl, originalFile.parent.path) ?: return arrayOf()

        return arrayOf(JsonFakePsiElement(jsonString, pair.first, originalModule))
    }

}
