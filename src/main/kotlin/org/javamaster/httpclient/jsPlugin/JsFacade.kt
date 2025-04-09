package org.javamaster.httpclient.jsPlugin

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.jsPlugin.support.JavaScript
import org.javamaster.httpclient.jsPlugin.support.WebCalm
import org.javamaster.httpclient.psi.HttpScriptBody

/**
 * @author yudong
 */
object JsFacade {

    fun resolveJsVariable(
        variableName: String,
        project: Project,
        scriptBodyList: List<HttpScriptBody>,
    ): PsiElement? {
        val jsVariable = JavaScript.resolveJsVariable(variableName, project, scriptBodyList)
        if (jsVariable != null) {
            return jsVariable
        }

        return WebCalm.resolveJsVariable(variableName, project, scriptBodyList)
    }

    fun createJsVariable(project: Project, injectedPsiFile: PsiFile, variableName: String): PsiElement? {
        val jsVariable = JavaScript.createJsVariable(project, injectedPsiFile, variableName)
        if (jsVariable != null) {
            return jsVariable
        }

        return WebCalm.createJsVariable(project, injectedPsiFile, variableName)
    }

}
