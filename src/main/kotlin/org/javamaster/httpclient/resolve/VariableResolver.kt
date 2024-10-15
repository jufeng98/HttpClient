package org.javamaster.httpclient.resolve

import com.cool.request.utils.Base64Utils
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.js.JsScriptExecutor
import org.javamaster.httpclient.psi.HttpGlobalVariableDefinition
import org.javamaster.httpclient.utils.HttpUtils
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * 解析变量
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class VariableResolver(private val project: Project) {
    private val patternNotNumber = Pattern.compile("\\D")
    private val fileScopeVariableMap: MutableMap<String, String> = mutableMapOf()

    fun addFileScopeVariables(
        definitions: MutableCollection<HttpGlobalVariableDefinition>,
        selectedEnv: String?,
        httpFileParentPath: String,
    ) {
        definitions.forEach {
            val split = it.text.split("=")
            val variableName = split[0].replace("@", "")
            val result = resolve(split[1], selectedEnv, httpFileParentPath)
            fileScopeVariableMap[variableName] = result
        }
    }

    fun clearFileScopeVariables() {
        fileScopeVariableMap.clear()
    }

    fun resolve(str: String, selectedEnv: String?, httpFileParentPath: String): String {
        val matcher = PATTERN.matcher(str)

        return matcher.replaceAll {
            val matchStr = it.group()
            val variable = matchStr.substring(2, matchStr.length - 2)

            resolveVariable(variable, selectedEnv, httpFileParentPath)
        }
    }

    private fun resolveVariable(
        variable: String,
        selectedEnv: String?,
        httpFileParentPath: String,
    ): String {
        var innerVariable = resolveInnerVariable(variable, httpFileParentPath)
        if (innerVariable != null) {
            return innerVariable
        }

        innerVariable = fileScopeVariableMap[variable]
        if (innerVariable != null) {
            return innerVariable
        }

        val jsScriptExecutor = JsScriptExecutor.getService(project)
        innerVariable = jsScriptExecutor.getRequestVariable(variable)
        if (innerVariable != null) {
            return innerVariable
        }

        innerVariable = jsScriptExecutor.getGlobalVariable(variable)
        if (innerVariable != null) {
            return innerVariable
        }

        val envFileService = EnvFileService.getService(project)
        val envValue = envFileService.getEnvValue(variable, selectedEnv, httpFileParentPath)
        if (envValue != null) {
            return envValue
        }

        throw IllegalArgumentException("无法解析变量${variable}")
    }

    private fun resolveInnerVariable(variable: String, httpFileParentPath: String): String? {
        if (variable == "\$random.uuid") {
            return UUID.randomUUID().toString().replace("-", "")
        }

        if (variable == "\$timestamp") {
            return System.currentTimeMillis().toString()
        }

        if (variable == "\$randomInt") {
            return RandomUtils.nextInt(0, 1000).toString()
        }

        if (variable.startsWith("\$random.integer")) {
            val split = variable.split(",")
            if (split.size == 1) {
                return RandomUtils.nextInt(0, 1000).toString()
            }

            val start = patternNotNumber.matcher(split[0]).replaceAll("")
            val end = patternNotNumber.matcher(split[1]).replaceAll("")
            return (start.toInt() + RandomUtils.nextInt(0, end.toInt())).toString()
        }

        if (variable.startsWith("\$random.alphabetic")) {
            val count = patternNotNumber.matcher(variable).replaceAll("")
            return RandomStringUtils.randomAlphabetic(count.toInt())
        }

        if (variable.startsWith("\$random.alphanumeric")) {
            val count = patternNotNumber.matcher(variable).replaceAll("")
            return RandomStringUtils.randomAlphanumeric(count.toInt())
        }

        if (variable.startsWith("\$random.numeric")) {
            val count = patternNotNumber.matcher(variable).replaceAll("")
            return RandomStringUtils.randomNumeric(count.toInt())
        }

        val funName = "\$imageToBase64"
        if (variable.startsWith(funName)) {
            val imagePath = variable.substring(funName.length + 1, variable.length - 1)
            val filePath = HttpUtils.constructFilePath(imagePath, httpFileParentPath)
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("文件${filePath}不存在!")
            }

            val bytes = FileUtils.readFileToByteArray(file)
            return Base64Utils.encodeToString(bytes)
        }

        return null
    }

    companion object {
        fun getService(project: Project): VariableResolver {
            return project.getService(VariableResolver::class.java)
        }

        val PATTERN: Pattern = Pattern.compile("(\\{\\{[\\w\\-,.\\\\:\$()\u4E00-\u9FA5]+}})", Pattern.MULTILINE)
    }
}
