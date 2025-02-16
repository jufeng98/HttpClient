package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsonValueGotoDeclarationHandler : GotoDeclarationHandler {

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

        if (jsonString.isPropertyName) {
            return arrayOf()
        }

        val injectionHost = InjectedLanguageManager.getInstance(jsonString.project).getInjectionHost(jsonString)
        if (injectionHost !is HttpMessageBody) {
            return arrayOf()
        }

        val value = jsonString.value
        if (!value.startsWith(HttpUtils.VARIABLE_SIGN_START)) {
            return arrayOf()
        }

        val name = value.substring(2, value.length - 2)
        return arrayOf(HttpVariableFakePsiElement(injectionHost, name))
    }

}
