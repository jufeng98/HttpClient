package org.javamaster.httpclient.reference.support

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.jsPlugin.JsFacade
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.psi.HttpVariableName
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import java.nio.file.Paths

/**
 * @author yudong
 */
class HttpVariableNamePsiReference(element: HttpVariableName, val textRange: TextRange) :
    PsiReferenceBase<HttpVariableName>(element, textRange) {

    override fun resolve(): PsiElement? {
        return tryResolveVariable(element.name, element.isBuiltin, element, true)
    }

    override fun getVariants(): Array<Any> {
        return getVariableVariants(element)
    }

    companion object {
        fun getVariableVariants(element: PsiElement): Array<Any> {
            val allList = mutableListOf<Any>()

            if (element.parent is HttpFilePath) {
                var tmp = InnerVariableEnum.MVN_TARGET
                allList.add(LookupElementBuilder.create(tmp.methodName).withTypeText(tmp.typeText(), true))

                tmp = InnerVariableEnum.PROJECT_ROOT
                allList.add(LookupElementBuilder.create(tmp.methodName).withTypeText(tmp.typeText(), true))

                tmp = InnerVariableEnum.HISTORY_FOLDER
                allList.add(LookupElementBuilder.create(tmp.methodName).withTypeText(tmp.typeText(), true))

                return allList.toTypedArray()
            }

            val envVariables = EnvFileService.getEnvMap(element.project)
            val list = envVariables.entries
                .map {
                    LookupElementBuilder.create(it.key).withTypeText(it.value, true)
                }

            allList.addAll(list)

            allList.addAll(builtInFunList)

            val propertyList = System.getProperties().entries
                .map {
                    LookupElementBuilder.create(PROPERTY_PREFIX + "." + it.key)
                        .withTypeText("" + it.value, true)
                }
                .toList()
            allList.addAll(propertyList)

            val envList = System.getenv().entries
                .map {
                    LookupElementBuilder.create(ENV_PREFIX + "." + it.key)
                        .withTypeText(it.value, true)
                }
                .toList()
            allList.addAll(envList)

            return allList.toTypedArray()
        }

        fun tryResolveVariable(
            variableName: String,
            builtin: Boolean,
            element: PsiElement,
            searchInPreJs: Boolean,
        ): PsiElement? {
            val httpFile = element.containingFile
            val project = httpFile.project
            val httpFileParentPath = httpFile.virtualFile?.parent?.path ?: return null

            if (builtin) {
                val innerVariableEnum = InnerVariableEnum.getEnum(variableName)
                if (InnerVariableEnum.isFolderEnum(innerVariableEnum)) {
                    val path = innerVariableEnum!!.exec(httpFileParentPath, project) ?: return null
                    val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(path)) ?: return null
                    return PsiManager.getInstance(project).findDirectory(virtualFile)
                }
                return null
            }

            val fileGlobalVariable = HttpUtils.resolveFileGlobalVariable(variableName, httpFile)
            if (fileGlobalVariable != null) {
                return fileGlobalVariable
            }

            val jsElement = tryResolveInJsHandler(variableName, element, httpFile, project, searchInPreJs)
            if (jsElement != null) {
                return jsElement
            }

            val selectedEnv = HttpEditorTopForm.getSelectedEnv(project)

            val jsonLiteral = EnvFileService.getEnvEleLiteral(variableName, selectedEnv, httpFileParentPath, project)

            return PsiTreeUtil.getParentOfType(jsonLiteral, JsonProperty::class.java)
        }

        private fun tryResolveInJsHandler(
            variableName: String,
            element: PsiElement,
            httpFile: PsiFile,
            project: Project,
            searchInPreJs: Boolean,
        ): PsiElement? {
            val requestBlock = PsiTreeUtil.getParentOfType(element, HttpRequestBlock::class.java) ?: return null

            if (searchInPreJs) {
                val scriptBodyList = HttpUtils.getAllPreJsScripts(httpFile, requestBlock).reversed()

                val jsVariable = JsFacade.resolveJsVariable(variableName, project, scriptBodyList)
                if (jsVariable != null) {
                    return jsVariable
                }
            }

            val scriptBodyList = HttpUtils.getAllPostJsScripts(httpFile)

            val jsVariable = JsFacade.resolveJsVariable(variableName, project, scriptBodyList)
            if (jsVariable != null) {
                return jsVariable
            }

            val parentPath = httpFile.virtualFile?.parent?.path ?: return null

            val preJsFiles = HttpUtils.getPreJsFiles(httpFile as HttpFile)

            return JsFacade.resolveJsVariable(variableName, preJsFiles)
        }

        private val builtInFunList by lazy {
            return@lazy InnerVariableEnum.entries
                .map {
                    LookupElementBuilder.create(it.methodName)
                        .withInsertHandler(it.insertHandler())
                        .withTypeText(it.typeText(), true)
                }
                .toTypedArray()
        }
    }
}