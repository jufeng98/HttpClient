package org.javamaster.httpclient.js.factory

import org.mozilla.javascript.WrapFactory

/**
 * @author yudong
 */
object HttpWrapFactory : WrapFactory() {

    init {
        isJavaPrimitiveWrap = false
    }

}
