package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.func.*
import org.mozilla.javascript.ScriptableObject

/**
 * @author yudong
 */
class JsHandlerPredefineRequestFunctions {

    companion object {

        fun defineDateFunc(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = DateFunction(jsExecutor)
            val methodName = InnerVariableEnum.DATE.methodName
            ScriptableObject.putProperty(reqScriptableObject, methodName, function)
        }

        fun defineTimestampDateFunc(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = TimestampDateFunction(jsExecutor)
            val methodName = InnerVariableEnum.TIMESTAMP_DATE.methodName
            ScriptableObject.putProperty(reqScriptableObject, methodName, function)
        }

        fun defineTimestampFullFunc(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = TimestampFullFunction(jsExecutor)
            val methodName = InnerVariableEnum.TIMESTAMP_FULL.methodName
            ScriptableObject.putProperty(reqScriptableObject, methodName, function)
        }

        fun defineBase64ToFileFunc(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = Base64ToFileFunction(jsExecutor)
            ScriptableObject.putProperty(reqScriptableObject, "\$base64ToFile", function)
        }

        fun defineFileToBase64Func(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = FileToBase64Function(jsExecutor)
            val methodName = InnerVariableEnum.FILE_TO_BASE64.methodName
            val alias = InnerVariableEnum.IMAGE_TO_BASE64.methodName
            ScriptableObject.putProperty(reqScriptableObject, methodName, function)
            ScriptableObject.putProperty(reqScriptableObject, alias, function)
        }

        fun defineReadStringFunc(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = ReadStringFunction(jsExecutor)
            val methodName = InnerVariableEnum.READ_STRING.methodName
            ScriptableObject.putProperty(reqScriptableObject, "readString", function)
            ScriptableObject.putProperty(reqScriptableObject, methodName, function)
        }

        fun defineRequireFunc(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val function = RequireFunction(jsExecutor)
            ScriptableObject.putProperty(reqScriptableObject, "require", function)
        }

    }

}