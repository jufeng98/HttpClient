package org.javamaster.httpclient.js.support.func

import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.w3c.dom.Document
import javax.xml.xpath.XPathFactory

/**
 * @author yudong
 */
@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object XpathFunction : BaseFunction() {

    val xPathFactory: XPathFactory by lazy {
        XPathFactory.newInstance()
    }

    override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val obj = args!![0]
        val expression = args[1] as String
        return xPathFactory.newXPath().evaluate(expression, Context.jsToJava(obj, Document::class.java))
    }

    override fun getArity(): Int {
        return 2
    }

}