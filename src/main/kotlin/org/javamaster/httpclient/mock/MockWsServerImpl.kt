package org.javamaster.httpclient.mock

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.mock.support.HttpWebSocketHandler
import org.javamaster.httpclient.mock.support.MockWsServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm


/**
 * @author yudong
 */
@Suppress("unused")
class MockWsServerImpl(
    private val port: Int,
    private val path: String,
    private val httpDashboardForm: HttpDashboardForm,
) : MockWsServer {
    var channelFuture: ChannelFuture? = null

    @Suppress("DEPRECATION")
    var group: io.netty.channel.nio.NioEventLoopGroup? = null

    override fun startServer(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: LinkedMultiValueMap<String, String>,
    ) {
        @Suppress("DEPRECATION")
        group = io.netty.channel.nio.NioEventLoopGroup(3)

        val serverBootstrap = ServerBootstrap()

        serverBootstrap
            .group(group)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    val pipeline: ChannelPipeline = ch.pipeline()
                    pipeline.addLast(HttpServerCodec())
                    pipeline.addLast(HttpObjectAggregator(65536))
                    pipeline.addLast(WebSocketServerProtocolHandler(path))
                    pipeline.addLast(HttpWebSocketHandler(httpDashboardForm, request, variableResolver, paramMap))
                }
            })

        channelFuture = serverBootstrap.bind(port).sync()

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.ws.server.start", port) + "\n")
    }

    override fun stopServer() {
        channelFuture?.channel()?.close()
        group?.shutdownGracefully()

        httpDashboardForm.showMockServerLog("WebSocket Server stopped\n")
    }

}