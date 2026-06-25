package org.javamaster.httpclient.mock.support

import com.alibaba.dubbo.rpc.service.GenericService
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.mock.support.MockServerHelper.appendTime
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.JsonUtils.gson
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * @author yudong
 */
class DubboGenericService(
    private val resConsumer: Consumer<String>,
    private val request: HttpRequest,
    private val variableResolver: VariableResolver,
    private val paramMap: Map<String, String>,
) : GenericService {

    override fun `$invoke`(method: String, parameterTypes: Array<String>, args: Array<Any>): Any? {
        resConsumer.accept("method: $method\n")
        resConsumer.accept("parameterTypes: ${parameterTypes.contentToString()}\n")
        resConsumer.accept("args: ${args.contentToString()}\n")

        val timeout = paramMap[ParamEnum.TIMEOUT_NAME.param]?.toLong()
        if (timeout != null) {
            resConsumer.accept("Sleeping: $timeout ms\n")
            TimeUnit.MILLISECONDS.sleep(timeout)
        }

        val bodyStr = DubboResultGenerator.generate(request, variableResolver)

        val byteArray = bodyStr.toByteArray(StandardCharsets.UTF_8)
        val size = byteArray.size

        resConsumer.accept(appendTime(NlsBundle.nls("mock.server.res") + "Content-Length $size b\n"))
        resConsumer.accept("-----------------------------\n")

        return gson.fromJson(bodyStr, HashMap::class.java)
    }

}
