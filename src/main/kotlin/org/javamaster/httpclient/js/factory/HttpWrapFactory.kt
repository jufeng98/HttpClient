package org.javamaster.httpclient.js.factory

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.WrapFactory

/**
 * @author yudong
 */
object HttpWrapFactory : WrapFactory() {

    override fun wrap(cx: Context, scope: Scriptable, obj: Any?, staticType: Class<*>?): Any? {
        return if (obj is String) {
            // 将 Java/Kotlin 字符串转换为真正的 JS 字符串
            Context.javaToJS(obj, scope)
        } else {
            super.wrap(cx, scope, obj, staticType)
        }
    }

}
