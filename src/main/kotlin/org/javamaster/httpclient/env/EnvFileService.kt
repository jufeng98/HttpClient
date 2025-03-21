package org.javamaster.httpclient.env

import com.intellij.json.psi.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import com.intellij.util.indexing.FileBasedIndex
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.index.HttpEnvironmentIndex.Companion.INDEX_ID
import org.javamaster.httpclient.resolve.VariableResolver.Companion.VARIABLE_PATTERN
import org.javamaster.httpclient.ui.HttpEditorTopForm
import java.io.File


/**
 * 解析环境文件
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class EnvFileService(val project: Project) {

    fun getPresetEnvList(httpFileParentPath: String): MutableSet<String> {
        val keySet1 = collectEnvNames(ENV_FILE_NAME, httpFileParentPath)

        val keySet2 = collectEnvNames(PRIVATE_ENV_FILE_NAME, httpFileParentPath)

        val set = mutableSetOf<String>()
        set.addAll(keySet1)
        set.addAll(keySet2)

        set.remove(COMMON_ENV_NAME)

        return set
    }

    private fun collectEnvNames(envFileName: String, httpFileParentPath: String): Set<String> {
        val fileName = "$httpFileParentPath/$envFileName"
        val virtualFile = VfsUtil.findFileByIoFile(File(fileName), true) ?: return setOf()
        return collectEnvNames(virtualFile)
    }

    private fun collectEnvNames(virtualFile: VirtualFile): Set<String> {
        val privateJsonFile = PsiUtil.getPsiFile(project, virtualFile) as JsonFile
        val jsonValue = privateJsonFile.topLevelValue

        if (jsonValue !is JsonObject) {
            throw IllegalArgumentException("配置文件:${virtualFile.name}格式不符合规范!")
        }

        val propertyList = jsonValue.propertyList
        val names = propertyList.map { it.name }.toList()

        return LinkedHashSet(names)
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

    private fun getEnvValue(
        key: String,
        selectedEnv: String?,
        httpFileParentPath: String,
        envFileName: String,
    ): String? {
        val innerJsonValue = getEnvEle(key, selectedEnv, httpFileParentPath, envFileName, project) ?: return null

        val value = when (innerJsonValue) {
            is JsonStringLiteral -> {
                innerJsonValue.value
            }

            is JsonNumberLiteral -> {
                "" + innerJsonValue.value
            }

            is JsonBooleanLiteral -> {
                innerJsonValue.value.toString()
            }

            else -> {
                throw RuntimeException("error:$innerJsonValue")
            }
        }

        return resolveValue(value, httpFileParentPath)
    }

    companion object {
        const val ENV_FILE_NAME = "http-client.env.json"
        const val PRIVATE_ENV_FILE_NAME = "http-client.private.env.json"
        val ENV_FILE_NAMES: Array<String> = arrayOf(ENV_FILE_NAME, PRIVATE_ENV_FILE_NAME)

        const val COMMON_ENV_NAME = "common"

        fun createEnvFile(name: String, isPrivate: Boolean, project: Project): VirtualFile? {
            val editorManager = FileEditorManager.getInstance(project)
            val selectedEditor = editorManager.selectedEditor!!
            val parent = selectedEditor.file.parent
            val parentPath = parent.path

            val file = File(parentPath, name)
            val virtualFile = VfsUtil.findFileByIoFile(file, true)
            if (virtualFile != null) {
                return null
            }

            return WriteAction.computeAndWait<VirtualFile, Exception> {
                val psiDirectory = PsiManager.getInstance(project).findDirectory(parent)!!
                val newFile = psiDirectory.createFile(name).virtualFile
                if (isPrivate) {
                    newFile.writeText(
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
                    )
                } else {
                    newFile.writeText(
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
                    )
                }

                newFile
            }
        }

        fun getService(project: Project): EnvFileService {
            return project.getService(EnvFileService::class.java)
        }

        private fun resolveValue(value: String, httpFileParentPath: String): String {
            val matcher = VARIABLE_PATTERN.matcher(value)

            return matcher.replaceAll {
                val matchStr = it.group()
                val variable = matchStr.substring(2, matchStr.length - 2)
                val innerVariableEnum = InnerVariableEnum.getEnum(variable) ?: return@replaceAll "{{\\$variable}}"

                innerVariableEnum.exec(variable, httpFileParentPath)
            }
        }

        private fun getEnvVariablesFromIndex(
            project: Project,
            selectedEnv: String?,
            httpFileParentPath: String,
            module: Module,
        ): MutableMap<String, String>? {
            if (selectedEnv == null) return null

            val projectScope = GlobalSearchScope.projectScope(project)
            val map = collectEnvMapFromIndex(selectedEnv, httpFileParentPath, projectScope)

            val moduleScope = GlobalSearchScope.moduleScope(module)
            map.putAll(collectEnvMapFromIndex(selectedEnv, httpFileParentPath, moduleScope))

            if (map.isEmpty()) {
                return null
            }

            return map
        }

        private fun collectEnvMapFromIndex(
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

        fun getEnvVariables(project: Project, tryIndex: Boolean = true): MutableMap<String, String> {
            val triple = HttpEditorTopForm.getTriple(project) ?: return mutableMapOf()

            val selectedEnv = triple.first
            val httpFileParentPath = triple.second.parent.path
            val module = triple.third

            if (tryIndex) {
                val mapFromIndex = getEnvVariablesFromIndex(project, selectedEnv, httpFileParentPath, module)
                if (mapFromIndex != null) {
                    return mapFromIndex
                }
            }

            val map = linkedMapOf<String, String>()

            map.putAll(getEnvVariables(COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project))

            map.putAll(getEnvVariables(COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project))

            map.putAll(getEnvVariables(selectedEnv, httpFileParentPath, ENV_FILE_NAME, project))

            map.putAll(getEnvVariables(selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project))

            return map
        }

        private fun getEnvVariables(
            selectedEnv: String?,
            httpFileParentPath: String,
            envFileName: String,
            project: Project,
        ): Map<String, String> {
            val env = selectedEnv ?: COMMON_ENV_NAME
            val fileName = "$httpFileParentPath/$envFileName"

            val virtualFile = VfsUtil.findFileByIoFile(File(fileName), true) ?: return mapOf()

            val psiFile = PsiUtil.getPsiFile(project, virtualFile) as JsonFile

            val topLevelValue = psiFile.topLevelValue
            if (topLevelValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${fileName}外层格式不符合规范!")
            }

            val envProperty = topLevelValue.findProperty(env) ?: return mapOf()
            val jsonValue = envProperty.value
            if (jsonValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${fileName}内层格式不符合规范!")
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

        fun getEnvEle(
            key: String,
            selectedEnv: String?,
            httpFileParentPath: String,
            project: Project,
        ): JsonLiteral? {
            var jsonLiteral = getEnvEle(key, selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project)
            if (jsonLiteral != null) {
                return jsonLiteral
            }

            jsonLiteral = getEnvEle(key, selectedEnv, httpFileParentPath, ENV_FILE_NAME, project)
            if (jsonLiteral != null) {
                return jsonLiteral
            }

            jsonLiteral = getEnvEle(key, COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project)
            if (jsonLiteral != null) {
                return jsonLiteral
            }

            return getEnvEle(key, COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project)
        }

        fun getEnvEle(
            key: String,
            selectedEnv: String?,
            httpFileParentPath: String,
            envFileName: String,
            project: Project,
        ): JsonLiteral? {
            val env = selectedEnv ?: COMMON_ENV_NAME
            var fileName = "$httpFileParentPath/$envFileName"

            var virtualFile = VfsUtil.findFileByIoFile(File(fileName), true)
            if (virtualFile == null) {
                fileName = "${project.basePath}/$envFileName"

                virtualFile = VfsUtil.findFileByIoFile(File(fileName), true)
            }

            if (virtualFile == null) {
                return null
            }

            val psiFile = PsiUtil.getPsiFile(project, virtualFile) as JsonFile

            val topLevelValue = psiFile.topLevelValue
            if (topLevelValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${fileName}外层格式不符合规范!")
            }

            val envProperty = topLevelValue.findProperty(env) ?: return null
            val jsonValue = envProperty.value
            if (jsonValue !is JsonObject) {
                throw IllegalArgumentException("配置文件:${fileName}内层格式不符合规范!")
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
                    throw IllegalArgumentException("配置文件:${fileName}最内层格式不符合规范!")
                }
            }
        }
    }

}
