package org.javamaster.httpclient.env

import com.intellij.json.psi.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiUtil
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

        return when (innerJsonValue) {
            is JsonStringLiteral -> {
                innerJsonValue.value
            }

            is JsonNumberLiteral -> {
                innerJsonValue.value.toString()
            }

            is JsonBooleanLiteral -> {
                innerJsonValue.value.toString()
            }

            else -> {
                throw RuntimeException("error:$innerJsonValue")
            }
        }
    }

    companion object {
        const val ENV_FILE_NAME = "http-client.env.json"
        const val PRIVATE_ENV_FILE_NAME = "http-client.private.env.json"

        const val COMMON_ENV_NAME = "common"

        fun getService(project: Project): EnvFileService {
            return project.getService(EnvFileService::class.java)
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
            val fileName = "$httpFileParentPath/$envFileName"

            val virtualFile = VfsUtil.findFileByIoFile(File(fileName), true) ?: return null

            val psiFile = PsiUtil.getPsiFile(project, virtualFile)
            val jsonFile = psiFile as JsonFile
            val topLevelValue = jsonFile.topLevelValue
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
