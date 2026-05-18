package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("unused")
class JavaBridge(private val jsExecutor: JsExecutor) {

    fun getClassNoArgDeclareMethodNames(clzName: String): List<String> {
        val clz = Class.forName(clzName)
        return clz.declaredMethods
            .filter { it.parameterCount == 0 }
            .map { it.name }
    }

    fun callJava(methodName: String): String {
        return callJava(methodName, null, null)
    }

    fun callJava(methodName: String, arg0: Any?): String {
        return callJava(methodName, arg0, null)
    }

    fun callJava(methodName: String, arg0: Any?, arg1: Any?): String {
        try {
            val args = mutableListOf<Any>()

            var convertArg = convertArg(arg0)
            if (convertArg != null) {
                args.add(convertArg)
            }

            convertArg = convertArg(arg1)
            if (convertArg != null) {
                args.add(convertArg)
            }

            val variableEnum = InnerVariableEnum.Companion.getEnum(methodName)
                ?: throw IllegalArgumentException(NlsBundle.nls("method.not.exists", methodName))

            return variableEnum.exec(methodName, "", *args.toTypedArray())
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    companion object {
        fun convertArg(arg: Any?): Any? {
            if (arg == "undefined") {
                return null
            }
            if (arg is Double) {
                return arg.toInt()
            }

            return arg
        }
    }

}