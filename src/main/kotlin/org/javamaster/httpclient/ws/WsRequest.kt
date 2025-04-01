package org.javamaster.httpclient.ws

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Disposer.newDisposable
import org.apache.commons.lang3.exception.ExceptionUtils
import org.javamaster.httpclient.dashboard.HttpProcessHandler
import org.javamaster.httpclient.utils.HttpUtils
import org.springframework.util.LinkedMultiValueMap
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer

/**
 * Support WebSocket request
 *
 * @author yudong
 */
class WsRequest(
    private val url: String,
    private val reqHeaderMap: LinkedMultiValueMap<String, String>,
    private val httpProcessHandler: HttpProcessHandler,
    private val paramMap: Map<String, String>,
) : Disposable {
    private var webSocket: WebSocket? = null
    lateinit var resConsumer: Consumer<String>

    init {
        Disposer.register(this, newDisposable())
    }

    fun connect() {
        returnResMsg("Connecting ${url}\n")

        val uri = URI(url)

        val connectTimeout = paramMap[HttpUtils.CONNECT_TIMEOUT_NAME]?.toLong() ?: 6

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeout))
            .build()

        val builder = client.newWebSocketBuilder()
        reqHeaderMap.forEach {
            it.value.forEach { value ->
                builder.header(it.key, value)
            }
        }

        val listener = WsListener(this, httpProcessHandler)

        builder.buildAsync(uri, listener)
            .whenComplete { ws, ex ->
                if (ex == null) {
                    webSocket = ws
                    return@whenComplete
                }

                httpProcessHandler.hasError = true
                httpProcessHandler.destroyProcess()

                returnResMsg("Connected failed:" + ExceptionUtils.getStackTrace(ex) + "\n")
            }
    }

    fun abortConnect() {
        if (webSocket == null) {
            return
        }

        webSocket!!.abort()
        returnResMsg("The ws connection has been disconnected\n")
    }

    fun sendWsMsg(msg: String) {
        webSocket?.sendText(msg, true)
            ?.whenComplete { _, u ->
                if (u == null) {
                    returnResMsg("↑↑↑ Succeed:$msg\n")
                } else {
                    returnResMsg("↑↑↑ Failed:${u.message}\n")
                }
            }
    }

    fun returnResMsg(msg: String) {
        runInEdt {
            runWriteActionAndWait {
                resConsumer.accept(msg)
            }
        }
    }

    override fun dispose() {
        abortConnect()
    }
}

class WsListener(private val wsRequest: WsRequest, private val httpProcessHandler: HttpProcessHandler) :
    WebSocket.Listener {
    override fun onText(webSocket: WebSocket?, data: CharSequence?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓ text data:$data\n")
        return CompletableFuture<Void>()
    }

    override fun onBinary(webSocket: WebSocket?, data: ByteBuffer?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓ binary data:$data\n")
        return CompletableFuture<Void>()
    }

    override fun onOpen(webSocket: WebSocket?) {
        webSocket?.request(1)

        wsRequest.returnResMsg("Connect succeed\n")
    }

    override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*> {
        httpProcessHandler.destroyProcess()

        wsRequest.returnResMsg("ws connection already closed,statusCode: $statusCode, reason: $reason\n")
        return CompletableFuture<Void>()
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        httpProcessHandler.destroyProcess()

        wsRequest.returnResMsg("ws connect failed, ${error}\n")
    }
}
