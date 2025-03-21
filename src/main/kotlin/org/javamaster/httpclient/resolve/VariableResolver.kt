package org.javamaster.httpclient.resolve

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.psi.HttpTypes
import java.util.*
import java.util.regex.Pattern

/**
 * 解析变量
 *
 * @author yudong
 */
class VariableResolver(
    private val jsExecutor: JsExecutor,
    private val httpFile: PsiFile,
    private val selectedEnv: String?,
) {
    private val project = jsExecutor.project
    val httpFileParentPath = httpFile.virtualFile.parent.path

    private val fileScopeVariableMap = getFileGlobalVariables()

    fun getFileGlobalVariables(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()

        val globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

        globalVariables.forEach {
            val name =
                getNextSiblingByType(it.globalVariableName.firstChild, HttpTypes.GLOBAL_NAME, false)?.text
                    ?: return@forEach
            val globalVariableValue = it.globalVariableValue ?: return@forEach

            val variable = globalVariableValue.variable
            val value = if (variable != null) {
                resolveVariable(variable.name, Collections.emptyMap()) ?: variable.text
            } else {
                getNextSiblingByType(globalVariableValue.firstChild, HttpTypes.GLOBAL_VALUE, false)?.text ?: ""
            }

            map[name] = value
        }

        return map
    }

    fun resolve(str: String): String {
        val matcher = VARIABLE_PATTERN.matcher(str)

        return matcher.replaceAll {
            val matchStr = it.group()
            val variable = matchStr.substring(2, matchStr.length - 2)

            val resolved = resolveVariable(variable, fileScopeVariableMap)
            if (resolved != null) {
                return@replaceAll resolved
            }

            // 无法解析变量,原样返回
            if (variable.startsWith("$")) {
                return@replaceAll "{{\\$variable}}"
            }

            matchStr
        }
    }

    private fun resolveVariable(variable: String, fileMap: Map<String, String>): String? {
        var innerVariable = fileMap[variable]
        if (innerVariable != null) {
            return innerVariable
        }

        innerVariable = jsExecutor.getRequestVariable(variable)
        if (innerVariable != null) {
            return innerVariable
        }

        innerVariable = jsExecutor.getGlobalVariable(variable)
        if (innerVariable != null) {
            return innerVariable
        }

        val envFileService = EnvFileService.getService(project)
        val envValue = envFileService.getEnvValue(variable, selectedEnv, httpFileParentPath)
        if (envValue != null) {
            return envValue
        }

        innerVariable = resolveInnerVariable(variable)
        if (innerVariable != null) {
            return innerVariable
        }

        if (variable.startsWith(PROPERTY_PREFIX)) {
            innerVariable = System.getProperty(variable.substring(PROPERTY_PREFIX.length + 1))
            if (innerVariable != null) {
                return innerVariable
            }
        }

        if (variable.startsWith(ENV_PREFIX)) {
            innerVariable = System.getenv(variable.substring(ENV_PREFIX.length + 1))
            if (innerVariable != null) {
                return innerVariable
            }
        }

        return null
    }

    fun getJsGlobalVariables(): LinkedHashMap<String, String> {
        val globalVariables = jsExecutor.getJsGlobalVariables()

        val map = linkedMapOf<String, String>()
        map.putAll(globalVariables)

        return map
    }

    private fun resolveInnerVariable(variable: String): String? {
        val variableEnum = InnerVariableEnum.getEnum(variable) ?: return null

        return variableEnum.exec(variable, httpFileParentPath)
    }

    companion object {
        val VARIABLE_PATTERN: Pattern = Pattern.compile("(\\{\\{[^{}]+}})")
        const val PROPERTY_PREFIX = "\$property"
        const val ENV_PREFIX = "\$env"
    }
}
