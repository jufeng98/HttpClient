package org.javamaster.httpclient.resolve

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.impl.MyJsonLazyFileElement
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
            val name = it.globalVariableName.name
            val globalVariableValue = it.globalVariableValue ?: return@forEach

            val variable = globalVariableValue.variable
            val value = if (variable != null) {
                resolveVariable(variable.variableName?.name, emptyMap(), false, null) ?: variable.text
            } else {
                globalVariableValue.value ?: ""
            }

            map[name] = value
        }

        return map
    }

    fun resolve(str: String): String {
        val matcher = VARIABLE_PATTERN.matcher(str)

        return matcher.replaceAll {
            val matchStr = it.group()

            val myJsonValue = MyJsonLazyFileElement.parse(matchStr)

            val variable = myJsonValue.variableList[0]
            val variableName = variable.variableName!!
            val name = variableName.name
            val builtin = variableName.isBuiltin
            val args = variable.variableArgs?.toArgsList()

            val result = resolveVariable(name, fileScopeVariableMap, builtin, args) ?: matchStr

            escapeRegexp(result)
        }
    }

    private fun resolveVariable(
        variable: String?,
        fileMap: Map<String, String>,
        builtin: Boolean,
        args: Array<Any>?,
    ): String? {
        if (variable == null) return null

        if (builtin) {
            var innerVariable = resolveInnerVariable(variable, args)
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

        return null
    }

    fun getJsGlobalVariables(): LinkedHashMap<String, String> {
        val globalVariables = jsExecutor.getJsGlobalVariables()

        val map = linkedMapOf<String, String>()
        map.putAll(globalVariables)

        return map
    }

    private fun resolveInnerVariable(variable: String, args: Array<Any>?): String? {
        val variableEnum = InnerVariableEnum.getEnum(variable) ?: return null

        return try {
            variableEnum.exec(httpFileParentPath, *args ?: emptyArray())
        } catch (e: UnsupportedOperationException) {
            variableEnum.exec(httpFileParentPath, project)
        }
    }

    companion object {
        val VARIABLE_PATTERN: Pattern = Pattern.compile("(\\{\\{[^{}]+}})")
        const val PROPERTY_PREFIX = "\$property"
        const val ENV_PREFIX = "\$env"

        fun escapeRegexp(result: String): String {
            return result.replace("\\", "\\\\")
                .replace("\$", "\\$")
        }

        fun resolveInnerVariable(str: String, parentPath: String, project: Project): String {
            val matcher = VARIABLE_PATTERN.matcher(str)

            return matcher.replaceAll {
                val matchStr = it.group()

                val myJsonValue = MyJsonLazyFileElement.parse(matchStr)

                val variable = myJsonValue.variableList[0]
                val variableName = variable.variableName!!
                val name = variableName.name

                val result = InnerVariableEnum.getEnum(name)?.exec(parentPath, project) ?: matchStr

                escapeRegexp(result)
            }
        }
    }
}
