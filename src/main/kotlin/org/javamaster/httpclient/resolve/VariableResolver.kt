package org.javamaster.httpclient.resolve

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jayway.jsonpath.JsonPath
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.jsObject.Console
import org.javamaster.httpclient.js.support.jsObject.JsGlobalVariablesHolder
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
import org.javamaster.httpclient.processHandler.RepeatExecutor
import org.javamaster.httpclient.psi.HttpFileVariable
import org.javamaster.httpclient.psi.HttpGlobalLiteralValue
import org.javamaster.httpclient.psi.HttpVariable
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.mozilla.javascript.NativeArray
import java.util.regex.Pattern

/**
 * Resolve variable
 *
 * @author yudong
 */
class VariableResolver(
    private val jsExecutor: JsExecutor?,
    private val httpFile: PsiFile,
    private val selectedEnv: String?,
    private val project: Project,
) {
    val httpFileParentPath = httpFile.virtualFile.parent.path

    val fileScopeVariableMap = getFileGlobalVariables()

    fun getFileGlobalVariables(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()

        val fileVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpFileVariable::class.java)

        fileVariables.forEach {
            val name = it.fileVariableName?.name ?: return@forEach
            val globalVariableValue = it.fileVariableValue ?: return@forEach

            val value = globalVariableValue.children.joinToString("") { innerIt ->
                when (innerIt) {
                    is HttpVariable -> {
                        val variableName = innerIt.variableName

                        if (variableName == null) {
                            innerIt.text
                        } else {
                            resolveVariable(
                                variableName.name,
                                emptyMap(),
                                variableName.isBuiltin,
                                innerIt.variableArgs?.toArgsList()
                            ) ?: innerIt.text
                        }
                    }

                    is HttpGlobalLiteralValue -> {
                        innerIt.text
                    }

                    else -> {
                        ""
                    }
                }
            }

            map[name] = value
        }

        return map
    }

    fun resolve(str: String): String {
        val matcher = VARIABLE_PATTERN.matcher(str)

        return matcher.replaceAll {
            val matchStr = it.group()

            val myJsonValue = TextVariableLazyFileElement.parse(matchStr)

            val variable = myJsonValue.variableList[0]
            val variableName = variable.variableName ?: return@replaceAll escapeRegexp(matchStr)

            val name = variableName.name
            val builtin = variableName.isBuiltin
            val args = variable.variableArgs?.toArgsList()

            val result = resolveVariable(name, fileScopeVariableMap, builtin, args) ?: matchStr

            escapeRegexp(result)
        }
    }

    private fun resolveVariable(
        variable: String?,
        fileScopeVariableMap: Map<String, String>,
        builtin: Boolean,
        args: Array<Any>?,
    ): String? {
        if (variable == null) return null

        if (builtin) {
            if (variable == InnerVariableEnum.TO_JSON_STR.methodName) {
                val arg = args!![0] as String
                var value = jsExecutor?.getRequestVariable(arg)
                if (value != null) {
                    return Console.convertParamToValidJson(value)
                }

                value = JsGlobalVariablesHolder.get(arg)
                if (value != null) {
                    return Console.convertParamToValidJson(value)
                }
            }

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
        }

        var innerVariable = jsExecutor?.getRequestVariable(variable)
        if (innerVariable != null) {
            return handleInnerVariable(innerVariable)
        }

        innerVariable = fileScopeVariableMap[variable]
        if (innerVariable != null) {
            return handleInnerVariable(innerVariable)
        }

        innerVariable = JsGlobalVariablesHolder.get(variable)
        if (innerVariable != null) {
            return handleInnerVariable(innerVariable)
        }

        innerVariable = resolveAsJsonPath(variable)
        if (innerVariable != null) {
            return handleInnerVariable(innerVariable)
        }

        val envFileService = EnvFileService.getService(project)
        val envValue = envFileService.getEnvValue(variable, selectedEnv, httpFileParentPath)
        if (envValue != null) {
            return envValue
        }

        return null
    }

    private fun resolveAsJsonPath(jsonPathExpression: String): Any? {
        val root = mutableMapOf<String, Any?>()
        root.putAll(JsGlobalVariablesHolder.dataHolder)

        if (jsExecutor != null) {
            root.putAll(jsExecutor.getRequestMap())
        }

        try {
            var value = JsonPath.read<Any>(root, jsonPathExpression)

            value = handleInnerVariable(value)

            ProcessHandlerBase.templateValues.add(value)

            return value
        } catch (e: Exception) {
            logInfo("执行动态变量jsonPath $jsonPathExpression 错误: ${e.message}")
        }

        val envFileService = EnvFileService.getService(project)
        val envJsonObj = envFileService.getEnv(selectedEnv, httpFileParentPath)
        if (envJsonObj == null) {
            return null
        }

        try {
            val envJsonText = computeReadAction { envJsonObj.text }

            var value = JsonPath.read<Any>(envJsonText, jsonPathExpression)

            value = handleInnerVariable(value)

            ProcessHandlerBase.templateValues.add(value)

            return value
        } catch (e: Exception) {
            logInfo("执行环境jsonPath $jsonPathExpression 错误: ${e.message}")
        }

        return null
    }

    private fun resolveInnerVariable(variable: String, args: Array<Any>?): String? {
        val variableEnum = InnerVariableEnum.getEnum(variable) ?: return null

        return try {
            variableEnum.exec(variable, httpFileParentPath, *args ?: emptyArray())
        } catch (_: UnsupportedOperationException) {
            variableEnum.exec(httpFileParentPath, project)
        }
    }

    private fun handleInnerVariable(innerVariable: Any): String {
        if (innerVariable is NativeArray) {
            val httpRepeatExecutor = ProcessHandlerBase.repeatExecutor

            if (httpRepeatExecutor == null) {
                ProcessHandlerBase.repeatExecutor = RepeatExecutor(innerVariable, 0)
            }

            return innerVariable[ProcessHandlerBase.repeatExecutor!!.arrayIndex].toString()
        } else {
            return if (innerVariable is Double) {
                if ((innerVariable.toInt().toDouble()) == innerVariable) {
                    // 移除 .0 后缀
                    innerVariable.toInt().toString()
                } else {
                    innerVariable.toString()
                }
            } else {
                innerVariable.toString()
            }
        }
    }

    companion object {
        val VARIABLE_PATTERN: Pattern = Pattern.compile("(\\{\\{[^{}]+}})")
        const val PROPERTY_PREFIX = "\$property"
        const val ENV_PREFIX = "\$env"

        fun escapeRegexp(result: String): String {
            @Suppress("CanUnescapeDollarLiteral")
            return result.replace("\\", "\\\\").replace("\$", "\\$")
        }

        fun resolveInnerVariable(str: String, parentPath: String, project: Project): String {
            val matcher = VARIABLE_PATTERN.matcher(str)

            return matcher.replaceAll {
                val matchStr = it.group()

                val myJsonValue = TextVariableLazyFileElement.parse(matchStr)

                val variable = myJsonValue.variableList[0]
                val variableName = variable.variableName ?: return@replaceAll escapeRegexp(matchStr)
                val name = variableName.name

                val result = InnerVariableEnum.getEnum(name)?.exec(parentPath, project) ?: matchStr

                escapeRegexp(result)
            }
        }
    }
}
