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
 * 支持 WebSocket请求
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
        returnResMsg("正在连接 ${url}\n")

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

                returnResMsg("连接失败:" + ExceptionUtils.getStackTrace(ex) + "\n")
            }
    }

    fun abortConnect() {
        if (webSocket == null) {
            return
        }

        webSocket!!.abort()
        returnResMsg("ws连接已断开\n")
    }

    fun sendWsMsg(msg: String) {
        webSocket?.sendText(msg, true)
            ?.whenComplete { _, u ->
                if (u == null) {
                    returnResMsg("↑↑↑成功:$msg\n")
                } else {
                    returnResMsg("↑↑↑失败:${u.message}\n")
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

        wsRequest.returnResMsg("↓↓↓文本数据:$data\n")
        return CompletableFuture<Void>()
    }

    override fun onBinary(webSocket: WebSocket?, data: ByteBuffer?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓二进制数据:$data\n")
        return CompletableFuture<Void>()
    }

    override fun onOpen(webSocket: WebSocket?) {
        webSocket?.request(1)

        wsRequest.returnResMsg("连接成功\n")
    }

    override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*> {
        httpProcessHandler.destroyProcess()

        wsRequest.returnResMsg("ws连接已关闭,statusCode:$statusCode,reason:$reason\n")
        return CompletableFuture<Void>()
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        httpProcessHandler.destroyProcess()

        wsRequest.returnResMsg("连接ws异常,${error?.message}\n")
    }
}
