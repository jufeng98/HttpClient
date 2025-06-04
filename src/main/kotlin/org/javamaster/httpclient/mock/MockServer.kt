package org.javamaster.httpclient.mock

import com.intellij.util.application
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import java.net.ServerSocket
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture


object MockServer {

    fun startServerAsync(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        request: HttpRequest,
        variableResolver: VariableResolver,
    ): CompletableFuture<Void> {
        val port = getPort(url)

        return CompletableFuture.runAsync {
            val serverSocket = ServerSocket(port)
            println("已启动 server, 服务端口: $port")

            while (true) {
                serverSocket.accept().use { socket ->
                    println("收到请求, 客户端口: ${socket.port}")

                    socket.getInputStream().use { inputStream ->
                        socket.getOutputStream().use { outputStream ->
                            Scanner(inputStream, StandardCharsets.UTF_8).use { scanner ->
                                val reqList = mutableListOf<String>()
                                while (scanner.hasNextLine()) {
                                    val line = scanner.nextLine()
                                    reqList.add(line)
                                }

                                val reqStr = reqList.joinToString(CR_LF)
                                println("客户 Request:$CR_LF$reqStr")

                                val reqBody = getReqBody(request, variableResolver)

                                val responseStr = constructResponse(reqHeaderMap, reqBody)

                                outputStream.write(responseStr.toByteArray(StandardCharsets.UTF_8))

                                println("服务端 Response:$CR_LF$responseStr")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getReqBody(request: HttpRequest, variableResolver: VariableResolver): Any? {
        var reqBody: Any? = null

        application.invokeAndWait {
            reqBody = HttpUtils.convertToReqBody(request, variableResolver)
        }

        return reqBody
    }

    private fun constructResponse(reqHeaderMap: LinkedMultiValueMap<String, String>, reqBody: Any?): String {
        val list = mutableListOf<String>()
        list.add("HTTP/1.1 200 OK")

        reqHeaderMap.forEach { (t, u) ->
            u.forEach {
                list.add("$t: $it")
            }
        }

        list.add(CR_LF)

        if (reqBody != null) {
            list.add(reqBody.toString())
        }

        return list.joinToString(CR_LF)
    }

    private fun getPort(url: String): Int {
        val uri = URI(url)
        return if (uri.port != -1) {
            uri.port
        } else {
            80
        }
    }
}
