package org.javamaster.httpclient.js.factory

import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory

/**
 * @author yudong
 */
object HttpContextFactory : ContextFactory() {

    /**
     * 解决 Java 的整型数字在 js 中访问时带 .0 的问题
     */
    override fun hasFeature(cx: Context?, featureIndex: Int): Boolean {
        if (featureIndex == Context.FEATURE_INTEGER_WITHOUT_DECIMAL_PLACE) {
            return true
        }

        return super.hasFeature(cx, featureIndex)
    }

    override fun onContextCreated(cx: Context) {
        super.onContextCreated(cx)

        cx.wrapFactory = HttpWrapFactory

        cx.setLanguageVersion(Context.VERSION_ES6)
    }

}
