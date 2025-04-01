package org.javamaster.httpclient.jsPlugin.support

import com.intellij.ide.plugins.PluginManager
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.psi.HttpScriptBody
import ris58h.webcalm.javascript.JavaScriptLanguage
import ris58h.webcalm.javascript.psi.*
import java.util.*

object WebCalm {
    private val pluginNotAlive by lazy {
        val pluginId = PluginId.findId("ris58h.webcalm") ?: return@lazy true
        val plugin = PluginManager.getInstance().findEnabledPlugin(pluginId)
        return@lazy plugin == null
    }

    fun resolveJsVariable(
        variableName: String,
        project: Project,
        scriptBodyList: List<HttpScriptBody>,
    ): PsiElement? {
        if (pluginNotAlive) {
            return null
        }

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
        return scriptBodyList
            .map {
                val injectedPsiFiles = injectedLanguageManager.getInjectedPsiFiles(it)
                if (injectedPsiFiles.isNullOrEmpty()) {
                    return@map null
                }

                val jsFile = injectedPsiFiles[0].first as JavaScriptFile

                val expressions = PsiTreeUtil.findChildrenOfType(jsFile, JavaScriptCallExpression::class.java)
                for (expression in expressions) {
                    val dotExpression =
                        PsiTreeUtil.getChildOfType(expression, JavaScriptMemberDotExpression::class.java)
                            ?: continue

                    val arguments = PsiTreeUtil.getChildOfType(expression, JavaScriptArguments::class.java)
                        ?: continue

                    val text = dotExpression.text
                    if (text == "request.variables.set") {
                        return@map findArgumentName(variableName, arguments) ?: continue
                    } else if (text == "client.global.set") {
                        return@map findArgumentName(variableName, arguments) ?: continue
                    }
                }

                return@map null
            }
            .firstOrNull { Objects.nonNull(it) }
    }

    fun createJsVariable(project: Project, injectedPsiFile: PsiFile, variableName: String): PsiElement? {
        if (pluginNotAlive) {
            return null
        }

        val js = "request.variables.set('$variableName', '');\n"

        val psiFileFactory = PsiFileFactory.getInstance(project)

        val tmpFile = psiFileFactory.createFileFromText("dummy.js", JavaScriptLanguage, js)
        val newExpressionStatement = PsiTreeUtil.findChildOfType(tmpFile, JavaScriptExpressionStatement::class.java)!!

        val elementCopy = injectedPsiFile.add(newExpressionStatement)
        injectedPsiFile.add(newExpressionStatement.nextSibling)

        // Move the cursor into the quotation marks
        (elementCopy.lastChild as Navigatable).navigate(true)
        val caretModel = FileEditorManager.getInstance(project).selectedTextEditor?.caretModel ?: return elementCopy
        caretModel.moveToOffset(caretModel.offset - 2)

        return elementCopy
    }

    private fun findArgumentName(variableName: String, arguments: JavaScriptArguments): JavaScriptArgument? {
        val argumentList = PsiTreeUtil.getChildrenOfType(arguments, JavaScriptArgument::class.java)
        return argumentList?.firstOrNull {
            val text = it.text
            text.substring(1, text.length - 1) == variableName
        }
    }

}
