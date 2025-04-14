package org.javamaster.httpclient.jsPlugin

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.jsPlugin.support.JavaScript
import org.javamaster.httpclient.jsPlugin.support.WebCalm
import org.javamaster.httpclient.model.PreJsFile
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

    fun resolveJsVariable(variableName: String, preJsFiles: List<PreJsFile>): PsiElement? {
        if (preJsFiles.isEmpty()) {
            return null
        }

        preJsFiles.forEach {
            val psiReferences = it.directionComment.directionValue?.references ?: return@forEach
            if (psiReferences.isEmpty()) {
                return@forEach
            }

            val jsFile = psiReferences[0].resolve()
            if (jsFile !is PsiFile) {
                return@forEach
            }

            var jsVariable = JavaScript.resolveJsVariable(variableName, jsFile)
            if (jsVariable != null) {
                return jsVariable
            }

            jsVariable = WebCalm.resolveJsVariable(variableName, jsFile)
            if (jsVariable != null) {
                return jsVariable
            }
        }

        return null
    }

}
