package org.javamaster.httpclient.jsPlugin.support

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.LightFilePointer
import com.intellij.openapi.vfs.pointers.VirtualFilePointer
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType
import com.intellij.psi.util.PsiTreeUtil.getChildOfType
import org.javamaster.httpclient.handler.JSElementResolveScopeProviderInvocationHandler
import org.javamaster.httpclient.jsPlugin.JsFacade
import org.javamaster.httpclient.psi.HttpScriptBody
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*


object JavaScript {

    private val pluginClassLoader by lazy {
        val plugin = findPlugin() ?: return@lazy null
        plugin.pluginClassLoader
    }

    val clzJSCallExpression by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSCallExpression", pluginClassLoader!!)
    }
    private val clzJSReferenceExpression by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSReferenceExpression", pluginClassLoader!!)
    }
    private val clzJSArgumentList by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSArgumentList", pluginClassLoader!!)
    }
    val clzJSLiteralExpression by lazy {
        loadClass("com.intellij.lang.javascript.psi.JSLiteralExpression", pluginClassLoader!!)
    }

    @Suppress("UNCHECKED_CAST")
    val coreJsStubLib by lazy {
        var method = findMethod(
            "com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider",
            "getJavaScriptCorePredefinedLibraryFiles"
        )
        val files1 = method.invoke(null) as Set<VirtualFile>

        method = findMethod(
            "com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider",
            "getES6CorePredefinedLibraryFiles"
        )
        val files2 = method.invoke(null) as Set<VirtualFile>

        val files = mutableSetOf<VirtualFile>()
        files.addAll(files1)
        files.addAll(files2)
        files
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

    fun referenceToJsVariable(element: PsiElement): Boolean? {
        if (pluginClassLoader == null) {
            return null
        }

        return clzJSLiteralExpression.isAssignableFrom(element.javaClass)
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

            if (JsFacade.interestedExpressions.contains(dotExpression.text)) {
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

    fun installTsLibrary(project: Project) {
        val virtualFilePointers = createTsVirtualFilePointers()

        var declaredMethod = findMethod(
            "com.intellij.webcore.libraries.ScriptingLibraryModel",
            "createPredefinedLibrary",
            String::class.java,
            Array<VirtualFilePointer>::class.java,
            Boolean::class.javaPrimitiveType
        )
        val scriptingLibraryModel =
            declaredMethod.invoke(null, "HttpRequest PrePost Js Handler", virtualFilePointers, false)

        declaredMethod = findMethod(
            "com.intellij.lang.javascript.library.JSPredefinedLibraryManager",
            "getPredefinedLibraryManager",
            Project::class.java
        )
        val jsPredefinedLibraryManager = declaredMethod.invoke(null, project)

        declaredMethod = findMethod("com.intellij.lang.javascript.library.JSPredefinedLibraryManager", "getData")
        val jsPredefinedLibrariesData = declaredMethod.invoke(jsPredefinedLibraryManager)

        modifyJsPredefinedLibrariesData(jsPredefinedLibrariesData, scriptingLibraryModel, virtualFilePointers)

        val myJSElementResolveScopeProvider = Proxy.newProxyInstance(
            pluginClassLoader,
            arrayOf(pluginClassLoader!!.loadClass("com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider")),
            JSElementResolveScopeProviderInvocationHandler()
        )

        addExtension("JavaScript.elementScopeProvider", myJSElementResolveScopeProvider)
    }

    private fun findMethod(className: String, methodName: String, vararg parameterTypes: Class<*>?): Method {
        var clz = pluginClassLoader!!.loadClass(className)
        var declaredMethod = clz.getDeclaredMethod(methodName, *parameterTypes)
        declaredMethod.isAccessible = true
        return declaredMethod
    }

    fun addExtension(key: String, extensionObj: Any) {
        val application = ApplicationManager.getApplication()
        val extensionArea = application.extensionArea
        val extensionPoint = extensionArea.getExtensionPoint<Any>(key)
        @Suppress("DEPRECATION")
        extensionPoint.registerExtension(extensionObj)
    }

    fun createTsVirtualFilePointers(): Array<LightFilePointer> {
        return arrayOf(
            HttpRequestHandlerApiDefinitionFilesHolder.commonLibraryFilePointer,
            HttpRequestHandlerApiDefinitionFilesHolder.dynamicVariablesFilePointer,
            HttpRequestHandlerApiDefinitionFilesHolder.cryptoLibraryFilePointer,
            HttpRequestHandlerApiDefinitionFilesHolder.responseLibraryFilePointer,
            HttpRequestHandlerApiDefinitionFilesHolder.preRequestLibraryFilePointer
        )
    }

    fun modifyJsPredefinedLibrariesData(
        jsPredefinedLibrariesData: Any,
        scriptingLibraryModel: Any,
        virtualFilePointers: Array<LightFilePointer>,
    ) {
        val virtualFiles = virtualFilePointers.map { it.file!! }.toSet()

        var declaredField = jsPredefinedLibrariesData.javaClass.getDeclaredField("myLibraryModels")
        declaredField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val models = declaredField.get(jsPredefinedLibrariesData) as Set<Any>

        val modelsNew = mutableSetOf<Any>()
        modelsNew.addAll(models)
        modelsNew.add(scriptingLibraryModel)

        declaredField.set(jsPredefinedLibrariesData, modelsNew)

        declaredField = jsPredefinedLibrariesData.javaClass.getDeclaredField("myRequiredLibraryFilesForResolve")
        declaredField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val myRequiredLibraryFilesForResolve = declaredField.get(jsPredefinedLibrariesData) as Set<VirtualFile>

        val myRequiredLibraryFilesForResolveNew = mutableSetOf<VirtualFile>()
        myRequiredLibraryFilesForResolveNew.addAll(myRequiredLibraryFilesForResolve)
        myRequiredLibraryFilesForResolveNew.addAll(virtualFiles)

        declaredField.set(jsPredefinedLibrariesData, myRequiredLibraryFilesForResolveNew)

        declaredField = jsPredefinedLibrariesData.javaClass.getDeclaredField("myRequiredLibraryFilesForResolveES5")
        declaredField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val myRequiredLibraryFilesForResolveES5 = declaredField.get(jsPredefinedLibrariesData) as Set<VirtualFile>

        val myRequiredLibraryFilesForResolveES5New = mutableSetOf<VirtualFile>()
        myRequiredLibraryFilesForResolveES5New.addAll(myRequiredLibraryFilesForResolveES5)
        myRequiredLibraryFilesForResolveES5New.addAll(virtualFiles)

        declaredField.set(jsPredefinedLibrariesData, myRequiredLibraryFilesForResolveES5New)

        declaredField = jsPredefinedLibrariesData.javaClass.getDeclaredField("myLibraryFilesForResolve")
        declaredField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val myLibraryFilesForResolve =
            declaredField.get(jsPredefinedLibrariesData) as NotNullLazyValue<Set<VirtualFile>>

        val myLibraryFilesForResolveNew = NotNullLazyValue.lazy<MutableSet<VirtualFile>> {
            val result = mutableSetOf<VirtualFile>()
            result.addAll(myLibraryFilesForResolve.get())
            result.addAll(virtualFiles)
            Collections.unmodifiableSet<VirtualFile>(result)
        }

        declaredField.set(jsPredefinedLibrariesData, myLibraryFilesForResolveNew)

        declaredField = jsPredefinedLibrariesData.javaClass.getDeclaredField("myLibraryFiles")
        declaredField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val myLibraryFiles =
            declaredField.get(jsPredefinedLibrariesData) as NotNullLazyValue<Set<VirtualFile>>
        val myLibraryFilesNew = NotNullLazyValue.lazy<MutableSet<VirtualFile>> {
            val result = mutableSetOf<VirtualFile>()
            result.addAll(myLibraryFiles.get())
            result.addAll(virtualFiles)
            Collections.unmodifiableSet<VirtualFile>(result)
        }

        declaredField.set(jsPredefinedLibrariesData, myLibraryFilesNew)
    }

    fun isAvailable(): Boolean {
        return pluginClassLoader != null
    }

}
