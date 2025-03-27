package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsStringPathGotoDeclarationHandler : GotoDeclarationHandler {

    private val clsNameSet = setOf("JavaScriptLiteral", "JSLiteralExpressionImpl")

    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?,
    ): Array<PsiElement> {
        if (element == null) {
            return arrayOf()
        }

        val project = element.project

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
        if (injectionHost !is HttpScriptBody) {
            return arrayOf()
        }

        val simpleName = element.parent?.javaClass?.simpleName ?: return arrayOf()

        if (!clsNameSet.contains(simpleName)) {
            return arrayOf()
        }

        val text = element.text
        if (text.length < 3) {
            return arrayOf()
        }

        val path = text.substring(1, text.length - 1)

        try {
            val httpFileParentPath = (injectionHost as HttpScriptBody).containingFile.virtualFile.parent.path

            val tmpPath = VariableResolver.resolveInnerVariable(path, httpFileParentPath, project)

            val item = HttpUtils.resolveFilePath(tmpPath, httpFileParentPath, project) ?: return arrayOf()

            return arrayOf(item)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return arrayOf()
    }

}
