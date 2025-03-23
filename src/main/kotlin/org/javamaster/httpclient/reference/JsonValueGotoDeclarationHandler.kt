package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAMES
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.reference.support.HttpVariablePsiReferenceProvider.Companion.tryResolveVariable
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
        @Suppress("ConstantConditionIf")
        if (true) {
            return emptyArray()
        }

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

        val project = jsonString.project

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(jsonString)
        if (injectionHost !is HttpMessageBody && !ENV_FILE_NAMES.contains(editor?.virtualFile?.name)) {
            return arrayOf()
        }

        val value = jsonString.value
        if (!value.startsWith(HttpUtils.VARIABLE_SIGN_START)) {
            return arrayOf()
        }

        val variableName = value.substring(2, value.length - 2)

        val psiElement = injectionHost ?: element

        val item = tryResolveVariable(variableName, psiElement, false) ?: return arrayOf()

        return arrayOf(item)
    }

}
