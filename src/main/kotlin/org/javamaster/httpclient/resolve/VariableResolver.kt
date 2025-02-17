package org.javamaster.httpclient.resolve

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.js.JsScriptExecutor
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpRequest
import java.util.regex.Pattern

/**
 * 解析变量
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class VariableResolver(private val project: Project) {
    private val fileScopeVariableMap: MutableMap<String, String> = mutableMapOf()

    fun addFileScopeVariables(
        httpFile: PsiFile,
        selectedEnv: String?,
        httpFileParentPath: String,
    ) {
        val globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

        globalVariables.forEach {
            val name = it.globalVariableName.text
            val globalVariableValue = it.globalVariableValue ?: return@forEach

            val variable = globalVariableValue.variable
            val value = if (variable != null) {
                resolveVariable(variable.name, selectedEnv, httpFileParentPath)
            } else {
                globalVariableValue.text
            }

            fileScopeVariableMap[name] = value
        }

        val httpRequests = PsiTreeUtil.findChildrenOfType(httpFile, HttpRequest::class.java)
        httpRequests
            .filter {
                val text = it.firstChild.text
                text.startsWith("---")
            }.map {
                it.text
            }.forEach { text ->
                text.split("\n")
                    .forEach {
                        val split = it.split("=")
                        if (split.size == 2) {
                            val variableName = split[0].replace("@", "")
                            val result = resolve(split[1], selectedEnv, httpFileParentPath)
                            fileScopeVariableMap[variableName] = result
                        }

                    }
            }

    }

    fun clearFileScopeVariables() {
        fileScopeVariableMap.clear()
    }

    fun resolve(str: String, selectedEnv: String?, httpFileParentPath: String): String {
        val matcher = VARIABLE_PATTERN.matcher(str)

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
        var innerVariable = fileScopeVariableMap[variable]
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

        innerVariable = resolveInnerVariable(variable, httpFileParentPath)
        if (innerVariable != null) {
            return innerVariable
        }

        return variable
    }

    fun getVariables(): LinkedHashMap<String, String> {
        val jsScriptExecutor = JsScriptExecutor.getService(project)
        val globalVariables = jsScriptExecutor.getGlobalVariables()

        val map = linkedMapOf<String, String>()
        map.putAll(globalVariables)

        return map
    }

    private fun resolveInnerVariable(variable: String, httpFileParentPath: String): String? {
        val variableEnum = InnerVariableEnum.getEnum(variable) ?: return null

        return variableEnum.exec(variable, httpFileParentPath)
    }

    companion object {
        fun getService(project: Project): VariableResolver {
            return project.getService(VariableResolver::class.java)
        }

        val VARIABLE_PATTERN: Pattern = Pattern.compile("(\\{\\{[^{}]+}})")
    }
}
