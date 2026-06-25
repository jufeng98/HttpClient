package org.javamaster.httpclient.mock.support

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.mock.support.MockServerHelper.appendTime
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * @author yudong
 */
class HttpWebSocketHandler(
    private val resConsumer: Consumer<String>,
    private val request: HttpRequest,
    private val variableResolver: VariableResolver,
    private val paramMap: Map<String, String>,
) :
    SimpleChannelInboundHandler<TextWebSocketFrame>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame) {
        val reqStr = msg.text()
        resConsumer.accept("收到消息: $reqStr\n")

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

        ctx.channel().writeAndFlush(TextWebSocketFrame(bodyStr))
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        resConsumer.accept("有新客户端连接: " + ctx.channel().id() + "\n")
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        resConsumer.accept("客户端断开连接: " + ctx.channel().id() + "\n")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        resConsumer.accept("出现异常: $cause\n")
        logWarn("出现异常", cause)
        ctx.close()
    }

}