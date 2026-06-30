package org.javamaster.httpclient.mock.support

import com.alibaba.dubbo.common.json.JSON
import com.alibaba.dubbo.rpc.service.GenericService
import org.javamaster.httpclient.dubbo.loader.ApiClassLoader
import org.javamaster.httpclient.dubbo.support.DubboBridge
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.JsonUtils.gson
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
class DubboGenericService(
    private val dubboBridge: DubboBridge,
    private val request: HttpRequest,
    private val variableResolver: VariableResolver,
    private val paramMap: Map<String, String>,
    private val apiClz: Class<*>?,
    private val apiClassLoader: ApiClassLoader?,
) : GenericService {

    override fun `$invoke`(method: String, parameterTypes: Array<String>, args: Array<Any>): Any? {
        dubboBridge.showMockServerLog("method: $method\n")
        dubboBridge.showMockServerLog("parameterTypes: ${parameterTypes.contentToString()}\n")
        dubboBridge.showMockServerLog("args: ${args.contentToString()}\n")

        val timeout = paramMap[ParamEnum.TIMEOUT_NAME.param]?.toLong()
        if (timeout != null) {
            dubboBridge.showMockServerLog("Sleeping: $timeout ms\n")
            TimeUnit.MILLISECONDS.sleep(timeout)
        }

        val bodyStr = DubboResultGenerator.generate(request, variableResolver)

        val byteArray = bodyStr.toByteArray(StandardCharsets.UTF_8)
        val size = byteArray.size

        dubboBridge.showMockServerLog(NlsBundle.nls("mock.server.res") + "Content-Length $size b\n")
        dubboBridge.showMockServerLog("-----------------------------\n")

        if (apiClz != null) {
            val classes = parameterTypes.map { apiClassLoader!!.loadClass(it) }.toList().toTypedArray()
            val declaredMethod = apiClz.getDeclaredMethod(method, *classes)

            val classLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = apiClassLoader!!

                return JSON.parse(bodyStr, declaredMethod.returnType)
            } finally {
                Thread.currentThread().contextClassLoader = classLoader
            }

        }

        return gson.fromJson(bodyStr, HashMap::class.java)
    }

}
