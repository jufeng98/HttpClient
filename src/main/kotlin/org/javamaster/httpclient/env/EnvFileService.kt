package org.javamaster.httpclient.env

import com.intellij.json.JsonElementTypes
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.indexing.FileBasedIndex
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.index.HttpEnvironmentIndex.Companion.INDEX_ID
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.impl.MyJsonLazyFileElement
import org.javamaster.httpclient.resolve.VariableResolver.Companion.VARIABLE_PATTERN
import org.javamaster.httpclient.resolve.VariableResolver.Companion.escapeRegexp
import org.javamaster.httpclient.ui.HttpEditorTopForm
import java.io.File


/**
 * 解析环境文件
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
            throw IllegalArgumentException("配置文件:${jsonFile.virtualFile.path}格式不符合规范!")
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

        val text = """
            {
                "$key": "",
            }
        """.trimIndent()

        val psiFileFactory = PsiFileFactory.getInstance(project)
        val tmpFile = psiFileFactory.createFileFromText("dummy.json", JsonLanguage.INSTANCE, text)

        val newProperty = PsiTreeUtil.findChildOfType(tmpFile, JsonProperty::class.java)!!
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

        val value = when (literal) {
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

            else -> {
                throw IllegalArgumentException("error:$literal")
            }
        }

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
                            "contextPath": "/bm-wash"
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

                val myJsonValue = MyJsonLazyFileElement.parse(matchStr)

                val variable = myJsonValue.variableList[0]
                val variableName = variable.variableName!!
                val variableArgs = variable.variableArgs
                val args = variableArgs?.toArgsList()
                val name = variableName.name

                // 支持环境文件内引用内置变量
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
                throw IllegalArgumentException("配置文件:${psiFile.virtualFile.path}外层格式不符合规范!")
            }

            val envProperty = topLevelValue.findProperty(env) ?: return mapOf()
            val jsonValue = envProperty.value
            if (jsonValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${psiFile.virtualFile.path}内层格式不符合规范!")
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
            val env = selectedEnv ?: COMMON_ENV_NAME

            val jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project) ?: return null

            val topLevelValue = jsonFile.topLevelValue
            if (topLevelValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${jsonFile.virtualFile.path}外层格式不符合规范!")
            }

            val envProperty = topLevelValue.findProperty(env) ?: return null

            val jsonValue = envProperty.value
            if (jsonValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${jsonFile.virtualFile.path}内层格式不符合规范!")
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
                    throw IllegalArgumentException("配置文件:${jsonFile.virtualFile.path}最内层格式不符合规范!")
                }
            }
        }

        fun getEnvJsonFile(envFileName: String, httpFileParentPath: String, project: Project): JsonFile? {
            var fileName = "$httpFileParentPath/$envFileName"

            var virtualFile = VfsUtil.findFileByIoFile(File(fileName), true)

            if (virtualFile != null) {
                return PsiUtil.getPsiFile(project, virtualFile) as JsonFile
            }

            fileName = "${project.basePath}/$envFileName"

            virtualFile = VfsUtil.findFileByIoFile(File(fileName), true)
            if (virtualFile != null) {
                return PsiUtil.getPsiFile(project, virtualFile) as JsonFile
            }

            return null
        }
    }

}
