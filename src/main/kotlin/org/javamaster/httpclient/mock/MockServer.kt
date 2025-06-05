package org.javamaster.httpclient.mock

import com.intellij.util.application
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * @author yudong
 */
class MockServer {
    lateinit var resConsumer: Consumer<String>

    fun startServerAsync(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): ServerSocket {
        val serverSocket = ServerSocket(resolvePort(url))
        resConsumer.accept(appendTime(NlsBundle.nls("mock.server.start", serverSocket.localPort) + "\n"))

        CompletableFuture.supplyAsync {
            while (true) {
                serverSocket.accept().use { socket ->
                    socket.getInputStream().use { inputStream ->
                        socket.getOutputStream().use { outputStream ->
                            resConsumer.accept(appendTime(NlsBundle.nls("mock.server.receive", socket.port) + "\n"))

                            val reqStr = readAsString(inputStream)

                            resConsumer.accept(reqStr.replace(CR_LF, "\n") + "\n")

                            val reqBody = computeReqBody(request, variableResolver)

                            val resList = constructResponse(reqHeaderMap, reqBody, paramMap)

                            outputStream.write(resList.joinToString(CR_LF).toByteArray(StandardCharsets.UTF_8))

                            resConsumer.accept(appendTime(NlsBundle.nls("mock.server.res") + "\n"))

                            resList.forEach {
                                if (it == CR_LF) {
                                    resConsumer.accept("\n")
                                } else {
                                    resConsumer.accept(it + "\n")
                                }
                            }

                            resConsumer.accept("-----------------------------\n")
                        }
                    }
                }
            }
        }.exceptionally { ex -> resConsumer.accept(appendTime(NlsBundle.nls("mock.server.error", ex) + "\n")) }

        return serverSocket
    }

    private fun readAsString(inputStream: InputStream): String {
        val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
        val buffer = CharArray(819200000)
        val bytesRead = reader.read(buffer)

        val out = StringBuilder()
        out.appendRange(buffer, 0, bytesRead)

        return out.toString()
    }

    private fun computeReqBody(request: HttpRequest, variableResolver: VariableResolver): Any? {
        return application.runReadAction<Any?> {
            HttpUtils.convertToReqBody(request, variableResolver)
        }
    }

    private fun constructResponse(
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        reqBody: Any?,
        paramMap: Map<String, String>,
    ): MutableList<String> {
        val statusCode = paramMap[ParamEnum.RESPONSE_STATUS.param]?.toLong() ?: 200

        val list = mutableListOf("HTTP/1.1 $statusCode OK")

        reqHeaderMap.forEach { (t, u) ->
            u.forEach {
                list.add("$t: $it")
            }
        }

        list.add(CR_LF)

        if (reqBody != null) {
            list.add(reqBody.toString())
        }

        return list
    }

    private fun resolvePort(url: String): Int {
        val uri = URI(url)
        return if (uri.port != -1) {
            uri.port
        } else {
            80
        }
    }

    private fun appendTime(msg: String): String {
        val time = DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss,SSS")
        return "$time - $msg"
    }

}
