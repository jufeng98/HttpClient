package org.javamaster.httpclient.reference.support

import com.intellij.ide.plugins.PluginManager
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.utils.HttpUtils
import ris58h.webcalm.javascript.psi.*
import java.util.*

object WebCalm {
    private val pluginNotAlive by lazy {
        val pluginId = PluginId.findId("ris58h.webcalm") ?: return@lazy true
        val plugin = PluginManager.getInstance().findEnabledPlugin(pluginId)
        return@lazy plugin == null
    }

    fun resolveJsVariable(variableName: String, element: PsiElement, httpFile: PsiFile): PsiElement? {
        if (pluginNotAlive) {
            return null
        }

        val project = httpFile.project

        val requestBlock = PsiTreeUtil.getParentOfType(element, HttpRequestBlock::class.java) ?: return null

        val allPreJsScripts = HttpUtils.getAllPreJsScripts(httpFile, requestBlock).reversed()

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
        return allPreJsScripts
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

    private fun findArgumentName(variableName: String, arguments: JavaScriptArguments): JavaScriptArgument? {
        val argumentList = PsiTreeUtil.getChildrenOfType(arguments, JavaScriptArgument::class.java)
        return argumentList?.firstOrNull {
            val text = it.text
            text.substring(1, text.length - 1) == variableName
        }
    }

}
