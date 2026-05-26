package org.javamaster.httpclient.js.support.func

import com.intellij.openapi.vfs.VirtualFileManager
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.Undefined
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * @author yudong
 */
class Base64ToFileFunction(private val jsExecutor: JsExecutor) : HttpBaseFunction() {

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val base64 = args!![0] as String
        val path = args[1] as String

        val parentPath = jsExecutor.httpFile.virtualFile.parent.path

        val tmpPath = VariableResolver.resolveInnerVariable(path, parentPath, jsExecutor.project)

        val filePath = HttpUtils.constructFilePath(tmpPath, parentPath)

        val file = File(filePath)
        val parentFile = file.parentFile

        if (!parentFile.exists()) {
            parentFile.mkdirs()
        } else {
            if (file.exists()) {
                file.delete()
            }
        }

        val bytes = Base64.getDecoder().decode(base64)
        val toPath = file.toPath()

        Files.write(toPath, bytes, StandardOpenOption.CREATE)

        GlobalLog.log(NlsBundle.nls("base64.convert.to.file") + " ${file.normalize()}")

        VirtualFileManager.getInstance().asyncRefresh(null)

        return Undefined.instance
    }

    override fun getArity(): Int {
        return 2
    }

}