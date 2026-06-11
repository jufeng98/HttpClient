package org.javamaster.httpclient.ws

import com.intellij.openapi.application.runInEdt
import com.intellij.util.application
import org.apache.commons.lang3.exception.ExceptionUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
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
    private val reqHeaderMap: LinkedMultiValueMap<String, String?>,
    private val processHandler: ProcessHandlerBase,
    private val paramMap: Map<String, String>,
) {
    private var webSocket: WebSocket? = null
    lateinit var resConsumer: Consumer<String>
    private val tabName = processHandler.tabName

    fun connect() {
        wsRunningSet.add(tabName)

        returnResMsg(NlsBundle.nls("connecting") + " ${url}\n")

        val uri = URI(url)

        val connectTimeout = paramMap[ParamEnum.CONNECT_TIMEOUT_NAME.param]?.toLong() ?: HttpConsts.CONNECT_TIMEOUT

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeout))
            .build()

        val builder = client.newWebSocketBuilder()
        reqHeaderMap.forEach {
            it.value.forEach { value ->
                builder.header(it.key, value ?: "null")
            }
        }

        val listener = WsListener(this, processHandler)

        builder.buildAsync(uri, listener)
            .whenComplete { ws, ex ->
                if (ex == null) {
                    webSocket = ws
                    return@whenComplete
                }

                wsRunningSet.remove(tabName)

                processHandler.hasError = true
                processHandler.detachProcess()

                returnResMsg(NlsBundle.nls("connected.failed") + ExceptionUtils.getStackTrace(ex) + "\n")
            }
    }

    fun abortConnect() {
        wsRunningSet.remove(tabName)

        if (webSocket == null) {
            return
        }

        webSocket!!.abort()
        returnResMsg(NlsBundle.nls("ws.disconnected") + "\n")
    }

    fun sendWsMsg(msg: String) {
        webSocket?.sendText(msg, true)
            ?.whenComplete { _, u ->
                if (u == null) {
                    returnResMsg("↑↑↑ ${NlsBundle.nls("succeed")}:$msg\n")
                } else {
                    returnResMsg("↑↑↑ ${NlsBundle.nls("failed")}:${u.message}\n")
                }
            }
    }

    fun returnResMsg(msg: String) {
        runInEdt {
            application.runWriteAction {
                resConsumer.accept(msg)
            }
        }
    }

    companion object {
        private val wsRunningSet = mutableSetOf<String>()

        fun isRunning(tabName: String): Boolean {
            return wsRunningSet.contains(tabName)
        }
    }
}

class WsListener(private val wsRequest: WsRequest, private val processHandler: ProcessHandlerBase) :
    WebSocket.Listener {
    override fun onText(webSocket: WebSocket?, data: CharSequence?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓ ${NlsBundle.nls("text.data")}:$data\n")
        return CompletableFuture<Void>()
    }

    override fun onBinary(webSocket: WebSocket?, data: ByteBuffer?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓ ${NlsBundle.nls("binary.data")}:$data\n")
        return CompletableFuture<Void>()
    }

    override fun onOpen(webSocket: WebSocket?) {
        webSocket?.request(1)

        wsRequest.returnResMsg("${NlsBundle.nls("connect.succeed")}\n")
    }

    override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*> {
        processHandler.detachProcess()

        wsRequest.returnResMsg("${NlsBundle.nls("ws.closed")},statusCode: $statusCode, reason: $reason\n")
        return CompletableFuture<Void>()
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        processHandler.detachProcess()

        wsRequest.returnResMsg("${NlsBundle.nls("ws.failed")}, ${error}\n")
    }
}
