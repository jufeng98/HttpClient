package org.javamaster.httpclient.js.support.func

import org.apache.commons.lang3.math.NumberUtils
import org.javamaster.httpclient.js.support.GlobalLog
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.Undefined
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
class SleepFunction() : HttpBaseFunction() {

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val lng = NumberUtils.createLong("" + args!![0])

        GlobalLog.log("sleeping $lng ms......")

        TimeUnit.MILLISECONDS.sleep(lng)

        return Undefined.instance
    }

    override fun getArity(): Int {
        return 1
    }

}