package org.javamaster.httpclient.jsPlugin.support

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType
import com.intellij.psi.util.PsiTreeUtil.getChildOfType
import org.javamaster.httpclient.psi.HttpScriptBody

object JavaScript {
    private val pluginClassLoader by lazy {
        val plugin = findPlugin() ?: return@lazy null
        plugin.pluginClassLoader
    }

    private val clzJSCallExpression by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSCallExpression", pluginClassLoader!!)
    }
    private val clzJSReferenceExpression by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSReferenceExpression", pluginClassLoader!!)
    }
    private val clzJSArgumentList by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSArgumentList", pluginClassLoader!!)
    }
    private val clzJSLiteralExpression by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSLiteralExpression", pluginClassLoader!!)
    }


    val jsLanguage by lazy {
        val plugin = findPlugin() ?: return@lazy null
        val pluginClassLoader = plugin.pluginClassLoader ?: return@lazy null

        val name = "com.intellij.lang.javascript.JavaScriptSupportLoader"
        val clz = pluginClassLoader.loadClass(name)

        val field = clz.getDeclaredField("JAVASCRIPT")
        field.isAccessible = true
        val languageFileType = field.get(null) as LanguageFileType

        languageFileType.language
    }

    fun resolveJsVariable(
        variableName: String,
        project: Project,
        scriptBodyList: List<HttpScriptBody>,
    ): PsiElement? {
        if (pluginClassLoader == null) {
            return null
        }

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
        return scriptBodyList
            .mapNotNull {
                val injectedPsiFiles = injectedLanguageManager.getInjectedPsiFiles(it)
                if (injectedPsiFiles.isNullOrEmpty()) {
                    return@mapNotNull null
                }

                // com.intellij.lang.javascript.psi.JSFile
                val jsFile = injectedPsiFiles[0].first

                resolveJsVariable(variableName, jsFile)
            }
            .firstOrNull()
    }

    fun resolveJsVariable(
        variableName: String,
        jsFile: PsiFile,
    ): PsiElement? {
        if (pluginClassLoader == null) {
            return null
        }

        return resolveJsVariable(variableName, jsFile as PsiElement)
    }

    private fun resolveJsVariable(variableName: String, jsFile: PsiElement): PsiElement? {
        val expressions = findChildrenOfType(jsFile, clzJSCallExpression)

        for (expression in expressions) {
            val dotExpression = getChildOfType(expression, clzJSReferenceExpression) ?: continue

            val arguments = getChildOfType(expression, clzJSArgumentList) ?: continue

            val text = dotExpression.text
            if (text == "request.variables.set") {
                return findArgumentName(variableName, arguments, clzJSLiteralExpression) ?: continue
            } else if (text == "client.global.set") {
                return findArgumentName(variableName, arguments, clzJSLiteralExpression) ?: continue
            }
        }

        return null
    }

    private fun findArgumentName(variableName: String, arguments: PsiElement, clz: Class<PsiElement>): PsiElement? {
        val jsLiteralExpression = getChildOfType(arguments, clz) ?: return null

        val text = jsLiteralExpression.text
        if (text.substring(1, text.length - 1) == variableName) {
            return jsLiteralExpression
        }

        return null
    }

    private fun loadClass(clzName: String, loader: ClassLoader): Class<PsiElement> {
        @Suppress("UNCHECKED_CAST")
        return loader.loadClass(clzName) as Class<PsiElement>
    }

    private fun findPlugin(): IdeaPluginDescriptor? {
        val pluginId = PluginId.findId("JavaScript") ?: return null
        return PluginManager.getInstance().findEnabledPlugin(pluginId)
    }

    fun createJsVariable(project: Project, injectedPsiFile: PsiFile, variableName: String): PsiElement? {
        if (pluginClassLoader == null) {
            return null
        }

        val loader = pluginClassLoader!!
        val clzJSExpressionStatement = loadClass("com.intellij.lang.javascript.psi.JSExpressionStatement", loader)

        val js = "request.variables.set('$variableName', '');\n"

        val psiFileFactory = PsiFileFactory.getInstance(project)

        val tmpFile = psiFileFactory.createFileFromText("dummy.js", jsLanguage!!, js)
        val newExpressionStatement = PsiTreeUtil.findChildOfType(tmpFile, clzJSExpressionStatement)!!

        val elementCopy = injectedPsiFile.add(newExpressionStatement)
        injectedPsiFile.add(newExpressionStatement.nextSibling)

        // Move the cursor into the quotation marks
        (elementCopy.lastChild as Navigatable).navigate(true)
        val caretModel =
            FileEditorManager.getInstance(project).selectedTextEditor?.caretModel ?: return newExpressionStatement
        caretModel.moveToOffset(caretModel.offset - 2)

        return newExpressionStatement
    }

    fun isAvailable(): Boolean {
        return pluginClassLoader != null
    }

}
