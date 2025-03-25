package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.jsPlugin.JavaScript
import org.javamaster.httpclient.jsPlugin.WebCalm
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*

/**
 * @author yudong
 */
class CreateJsVariableQuickFix(private val global: Boolean, private val variableName: String) : LocalQuickFix {

    override fun getFamilyName(): String {
        val tip = if (global) "global" else "pre request"
        return "Create variable unsolved in $tip handler"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        createJsVariable(project, descriptor.psiElement)
    }

    private fun createJsVariable(project: Project, psiElement: PsiElement) {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            return
        }

        val requestBlock = when (psiElement) {
            is HttpVariable -> {
                PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock::class.java)!!
            }

            is JsonStringLiteral -> {
                val element = InjectedLanguageManager.getInstance(project).getInjectionHost(psiElement)
                PsiTreeUtil.getParentOfType(element, HttpRequestBlock::class.java)!!
            }

            else -> {
                return
            }
        }

        val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile

        val scriptBody = if (global) {
            httpFile.getGlobalHandler()?.globalScript?.scriptBody
        } else {
            requestBlock.preRequestHandler?.preRequestScript?.scriptBody
        }

        if (scriptBody == null) {
            createAndAddHandler(project, httpFile, requestBlock)
            return
        }

        val injectedPsiFiles = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(scriptBody)
        val injectedPsiFile = injectedPsiFiles?.get(0)?.first as PsiFile? ?: return

        val jsVariable = JavaScript.createJsVariable(project, injectedPsiFile, variableName)
        if (jsVariable != null) {
            return
        }

        WebCalm.createJsVariable(project, injectedPsiFile, variableName)
    }

    private fun createAndAddHandler(project: Project, httpFile: HttpFile, requestBlock: HttpRequestBlock) {
        val handler = if (global) {
            """
                <! {%
                    request.variables.set('$variableName', '');
                %}
                
                
            """.trimIndent()
        } else {
            """
                < {%
                    request.variables.set('$variableName', '');
                %}
                
            """.trimIndent()
        }

        val psiFileFactory = PsiFileFactory.getInstance(project)
        val tmpFile = psiFileFactory.createFileFromText("dummy.http", HttpLanguage.INSTANCE, handler) as HttpFile

        val scriptBody = if (global) {
            val newGlobalHandler = PsiTreeUtil.findChildOfType(tmpFile, HttpGlobalHandler::class.java)!!
            val globalHandler = httpFile.addBefore(newGlobalHandler, httpFile.firstChild) as HttpGlobalHandler
            globalHandler.globalScript.scriptBody!!
        } else {
            val request = PsiTreeUtil.findChildOfType(requestBlock, HttpRequest::class.java)!!
            val newPreRequestHandler = PsiTreeUtil.findChildOfType(tmpFile, HttpPreRequestHandler::class.java)!!
            val preRequestHandler = requestBlock.addBefore(newPreRequestHandler, request) as HttpPreRequestHandler
            preRequestHandler.preRequestScript.scriptBody!!
        }

        // 将光标移动到引号内
        val jsVariable = WebCalm.resolveJsVariable(variableName, project, listOf(scriptBody))!!
        (jsVariable.parent.lastChild.prevSibling as Navigatable).navigate(true)
        val caretModel = FileEditorManager.getInstance(project).selectedTextEditor?.caretModel ?: return
        caretModel.moveToOffset(caretModel.offset + 1)
    }
}
