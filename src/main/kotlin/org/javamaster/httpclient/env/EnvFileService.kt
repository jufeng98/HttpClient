package org.javamaster.httpclient.env

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.openapi.vfs.writeText
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import com.intellij.util.indexing.FileBasedIndex
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.factory.JsonPsiFactory
import org.javamaster.httpclient.index.HttpEnvironmentIndex.Companion.INDEX_ID
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement
import org.javamaster.httpclient.resolve.VariableResolver.Companion.VARIABLE_PATTERN
import org.javamaster.httpclient.resolve.VariableResolver.Companion.escapeRegexp
import org.javamaster.httpclient.ui.HttpEditorTopForm
import java.io.File


/**
 * Resolve environment file
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class EnvFileService(val project: Project) {

    fun getPresetEnvSet(httpFileParentPath: String): MutableSet<String> {
        val envSet = LinkedHashSet<String>()

        val jsonFile = getEnvJsonFile(PRIVATE_ENV_FILE_NAME, httpFileParentPath, project)

        val privateEnvList = collectEnvNames(jsonFile)

        envSet.addAll(privateEnvList)

        val jsonPrivateFile = getEnvJsonFile(ENV_FILE_NAME, httpFileParentPath, project)

        val envList = collectEnvNames(jsonPrivateFile)

        envSet.addAll(envList)

        envSet.remove(COMMON_ENV_NAME)

        return envSet
    }

    private fun collectEnvNames(jsonFile: JsonFile?): List<String> {
        if (jsonFile == null) {
            return emptyList()
        }

        val jsonValue = jsonFile.topLevelValue

        if (jsonValue !is JsonObject) {
            System.err.println("The environment file: ${jsonFile.virtualFile.path} format does not conform to the specification!")
            return emptyList()
        }

        return jsonValue.propertyList.map { it.name }.toList()
    }

    fun getEnvValue(key: String, selectedEnv: String?, httpFileParentPath: String): String? {
        var envValue = getEnvValue(key, selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME)
        if (envValue != null) {
            return envValue
        }

        envValue = getEnvValue(key, selectedEnv, httpFileParentPath, ENV_FILE_NAME)
        if (envValue != null) {
            return envValue
        }

        envValue = getEnvValue(key, COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME)
        if (envValue != null) {
            return envValue
        }

        return getEnvValue(key, COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME)
    }

    fun createEnvValue(key: String, selectedEnv: String, httpFileParentPath: String, envFileName: String) {
        val jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project) ?: return

        val topLevelValue = jsonFile.topLevelValue
        if (topLevelValue !is JsonObject) {
            return
        }

        val envProperty = topLevelValue.findProperty(selectedEnv) ?: return
        val value = envProperty.value
        if (value !is JsonObject) {
            return
        }

        val newProperty = JsonPsiFactory.createStringProperty(project, key, "")

        val newComma = HttpPsiUtils.getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false)!!

        val propertyList = value.propertyList

        if (propertyList.isNotEmpty()) {
            value.addAfter(newComma, propertyList.last())
        }

        val elementCopy = value.addBefore(newProperty, value.lastChild)

        // 将光标移动到引号内
        (elementCopy.lastChild as Navigatable).navigate(true)
        val caretModel = FileEditorManager.getInstance(project).selectedTextEditor?.caretModel ?: return
        caretModel.moveToOffset(caretModel.offset + 1)
    }

    private fun getEnvValue(
        key: String,
        selectedEnv: String?,
        httpFileParentPath: String,
        envFileName: String,
    ): String? {
        val literal = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, envFileName, project) ?: return null

        val value = getJsonLiteralValue(literal)

        return resolveValue(value, httpFileParentPath)
    }

    companion object {
        const val ENV_FILE_NAME = "http-client.env.json"
        const val PRIVATE_ENV_FILE_NAME = "http-client.private.env.json"

        val ENV_FILE_NAMES = setOf(ENV_FILE_NAME, PRIVATE_ENV_FILE_NAME)

        const val COMMON_ENV_NAME = "common"

        fun createEnvFile(name: String, isPrivate: Boolean, project: Project): VirtualFile? {
            val editorManager = FileEditorManager.getInstance(project)
            val selectedEditor = editorManager.selectedEditor!!
            val parent = selectedEditor.file.parent
            val parentPath = parent.path

            val virtualFile = VfsUtil.findFileByIoFile(File(parentPath, name), true)
            if (virtualFile != null) {
                return null
            }

            return WriteAction.computeAndWait<VirtualFile, Exception> {
                val content = if (isPrivate) {
                    """
                        {
                          "dev": {
                            "token": "rRTJHGerfgET"
                          },
                          "uat": {
                            "token": "ERTYHGSDKFue"
                          },
                          "pro": {
                            "token": "efJFGHJKHYTR"
                          }
                        }
                    """.trimIndent()
                } else {
                    """
                        {
                          "dev": {
                            "baseUrl": "http://localhost:8800"
                          },
                          "uat": {
                            "baseUrl": "https://uat.javamaster.org/bm-wash"
                          },
                          "pro": {
                            "baseUrl": "https://pro.javamaster.org/bm-wash"
                          },
                          "common": {
                            "contextPath": "/admin"
                          }
                        }
                    """.trimIndent()
                }

                val psiDirectory = PsiManager.getInstance(project).findDirectory(parent)!!
                val newJsonFile = psiDirectory.createFile(name).virtualFile
                newJsonFile.writeText(content)

                newJsonFile
            }
        }

        fun getService(project: Project): EnvFileService {
            return project.getService(EnvFileService::class.java)
        }

        private fun resolveValue(value: String, httpFileParentPath: String): String {
            val matcher = VARIABLE_PATTERN.matcher(value)

            return matcher.replaceAll {
                val matchStr = it.group()

                val myJsonValue = TextVariableLazyFileElement.parse(matchStr)

                val variable = myJsonValue.variableList[0]
                val variableName = variable.variableName ?: return@replaceAll escapeRegexp(matchStr)
                val variableArgs = variable.variableArgs
                val args = variableArgs?.toArgsList()
                val name = variableName.name

                // Support environment files that reference built-in variables
                val innerVariableEnum = InnerVariableEnum.getEnum(name)

                val result = innerVariableEnum?.exec(httpFileParentPath, *args ?: emptyArray()) ?: matchStr

                escapeRegexp(result)
            }
        }

        private fun getEnvMapFromIndex(
            project: Project,
            selectedEnv: String?,
            httpFileParentPath: String,
            module: Module?,
        ): MutableMap<String, String>? {
            selectedEnv ?: return null

            val projectScope = GlobalSearchScope.projectScope(project)
            val map = getEnvMapFromIndex(selectedEnv, httpFileParentPath, projectScope)

            if (module != null) {
                val moduleScope = GlobalSearchScope.moduleScope(module)
                map.putAll(getEnvMapFromIndex(selectedEnv, httpFileParentPath, moduleScope))
            }

            if (map.isEmpty()) {
                return null
            }

            return map
        }

        private fun getEnvMapFromIndex(
            selectedEnv: String,
            httpFileParentPath: String,
            scope: GlobalSearchScope,
        ): MutableMap<String, String> {
            val map = mutableMapOf<String, String>()
            val fileBasedIndex = FileBasedIndex.getInstance()

            val commonList = fileBasedIndex.getValues(INDEX_ID, COMMON_ENV_NAME, scope)
            commonList.forEach {
                it.forEach { (k, v) ->
                    map[k] = resolveValue(v, httpFileParentPath)
                }
            }

            val envList = fileBasedIndex.getValues(INDEX_ID, selectedEnv, scope)
            envList.forEach {
                it.forEach { (k, v) ->
                    map[k] = resolveValue(v, httpFileParentPath)
                }
            }

            return map
        }

        fun getEnvMap(project: Project, tryIndex: Boolean = true): MutableMap<String, String> {
            val triple = HttpEditorTopForm.getTriple(project) ?: return mutableMapOf()

            val selectedEnv = triple.first
            val httpFileParentPath = triple.second.parent.path
            val module = triple.third

            if (tryIndex) {
                val mapFromIndex = getEnvMapFromIndex(project, selectedEnv, httpFileParentPath, module)
                if (mapFromIndex != null) {
                    return mapFromIndex
                }
            }

            val map = linkedMapOf<String, String>()

            map.putAll(getEnvMap(COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project))

            map.putAll(getEnvMap(COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project))

            map.putAll(getEnvMap(selectedEnv, httpFileParentPath, ENV_FILE_NAME, project))

            map.putAll(getEnvMap(selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project))

            return map
        }

        private fun getEnvMap(
            selectedEnv: String?,
            httpFileParentPath: String,
            envFileName: String,
            project: Project,
        ): Map<String, String> {
            val env = selectedEnv ?: COMMON_ENV_NAME

            val psiFile = getEnvJsonFile(envFileName, httpFileParentPath, project) ?: return emptyMap()

            val topLevelValue = psiFile.topLevelValue
            if (topLevelValue !is JsonObject) {
                System.err.println("The environment file: ${psiFile.virtualFile.path} outer format does not conform to the specification!")
                return emptyMap()
            }

            val envProperty = topLevelValue.findProperty(env) ?: return mapOf()
            val jsonValue = envProperty.value
            if (jsonValue !is JsonObject) {
                System.err.println("The environment file: ${psiFile.virtualFile.path} inner format does not conform to the specification!")
                return emptyMap()
            }

            val envFileService = getService(project)

            val map = linkedMapOf<String, String>()

            jsonValue.propertyList
                .forEach {
                    val envValue = envFileService.getEnvValue(it.name, selectedEnv, httpFileParentPath)
                    map[it.name] = envValue ?: "<null>"
                }

            return map
        }

        fun getEnvEleLiteral(
            key: String,
            selectedEnv: String?,
            httpFileParentPath: String,
            project: Project,
        ): JsonLiteral? {
            var literal = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project)
            if (literal != null) {
                return literal
            }

            literal = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, ENV_FILE_NAME, project)
            if (literal != null) {
                return literal
            }

            literal = getEnvEleLiteral(key, COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project)
            if (literal != null) {
                return literal
            }

            return getEnvEleLiteral(key, COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project)
        }

        private fun getEnvEleLiteral(
            key: String,
            selectedEnv: String?,
            httpFileParentPath: String,
            envFileName: String,
            project: Project,
        ): JsonLiteral? {
            val jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project) ?: return null

            val envProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, envFileName, project) ?: return null

            val jsonValue = envProperty.value
            if (jsonValue !is JsonObject) {
                System.err.println("The environment file: ${jsonFile.virtualFile.path} inner format does not conform to the specification!")
                return null
            }

            val jsonProperty = jsonValue.findProperty(key) ?: return null

            val innerJsonValue = jsonProperty.value ?: return null

            return when (innerJsonValue) {
                is JsonStringLiteral -> {
                    innerJsonValue
                }

                is JsonNumberLiteral -> {
                    innerJsonValue
                }

                is JsonBooleanLiteral -> {
                    innerJsonValue
                }

                else -> {
                    System.err.println("The environment file: ${jsonFile.virtualFile.path} innermost format does not conform to the specification!!")
                    return null
                }
            }
        }

        fun getEnvJsonProperty(
            selectedEnv: String?,
            httpFileParentPath: String,
            project: Project,
        ): JsonProperty? {
            var jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, ENV_FILE_NAME, project)
            if (jsonProperty != null) {
                return jsonProperty
            }

            jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project)
            if (jsonProperty != null) {
                return jsonProperty
            }

            jsonProperty = getEnvJsonProperty(COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project)
            if (jsonProperty != null) {
                return jsonProperty
            }

            return getEnvJsonProperty(COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project)
        }

        private fun getEnvJsonProperty(
            selectedEnv: String?,
            httpFileParentPath: String,
            envFileName: String,
            project: Project,
        ): JsonProperty? {
            val env = selectedEnv ?: COMMON_ENV_NAME

            val jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project) ?: return null

            val topLevelValue = jsonFile.topLevelValue
            if (topLevelValue !is JsonObject) {
                System.err.println("The environment file: ${jsonFile.virtualFile.path} outer format does not conform to the specification!")
                return null
            }

            return topLevelValue.findProperty(env)
        }

        fun getJsonLiteralValue(literal: JsonLiteral): String {
            return when (literal) {
                is JsonStringLiteral -> {
                    val txt = literal.text
                    txt.substring(1, txt.length - 1)
                }

                is JsonNumberLiteral -> {
                    literal.value.toString()
                }

                is JsonBooleanLiteral -> {
                    literal.value.toString()
                }

                is JsonNullLiteral -> {
                    literal.text
                }

                else -> {
                    System.err.println("error:$literal")
                    return ""
                }
            }
        }

        fun getEnvJsonFile(envFileName: String, httpFileParentPath: String, project: Project): JsonFile? {
            val dir = VfsUtil.findFileByIoFile(File(project.basePath!!), true)!!

            return getEnvJsonFile(envFileName, httpFileParentPath, project, dir)
        }

        private fun getEnvJsonFile(
            envFileName: String,
            httpFileParentPath: String,
            project: Project,
            projectDir: VirtualFile,
        ): JsonFile? {
            val dir = VfsUtil.findFileByIoFile(File(httpFileParentPath), true) ?: return null

            val virtualFile = dir.findFile(envFileName)

            if (virtualFile != null) {
                return PsiUtil.getPsiFile(project, virtualFile) as JsonFile
            }

            if (dir == projectDir) {
                return null
            }

            if (dir.parent != null) {
                val path = dir.parent.path
                return getEnvJsonFile(envFileName, path, project, projectDir)
            }

            return null
        }
    }

}
