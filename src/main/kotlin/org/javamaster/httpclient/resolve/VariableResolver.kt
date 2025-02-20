package org.javamaster.httpclient.resolve

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.js.JsScriptExecutor
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.psi.HttpTypes
import java.util.regex.Pattern

/**
 * 解析变量
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class VariableResolver(private val project: Project) {
    private val fileScopeVariableMap: MutableMap<String, String> = mutableMapOf()

    fun initFileScopeVariables(
        httpFile: PsiFile,
        selectedEnv: String?,
        httpFileParentPath: String,
    ) {
        val map = getFileGlobalVariables(httpFile, selectedEnv, httpFileParentPath)
        fileScopeVariableMap.putAll(map)
    }

    fun getFileGlobalVariables(
        httpFile: PsiFile,
        selectedEnv: String?,
        httpFileParentPath: String,
    ): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()

        val globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

        globalVariables.forEach {
            val name =
                getNextSiblingByType(it.globalVariableName.firstChild, HttpTypes.GLOBAL_NAME, false)?.text
                    ?: return@forEach
            val globalVariableValue = it.globalVariableValue ?: return@forEach

            val variable = globalVariableValue.variable
            val value = if (variable != null) {
                resolveVariable(variable.name, selectedEnv, httpFileParentPath)
            } else {
                getNextSiblingByType(globalVariableValue.firstChild, HttpTypes.GLOBAL_VALUE, false)?.text ?: ""
            }

            map[name] = value
        }

        return map
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

    fun getJsGlobalVariables(): LinkedHashMap<String, String> {
        val jsScriptExecutor = JsScriptExecutor.getService(project)
        val globalVariables = jsScriptExecutor.getJsGlobalVariables()

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
