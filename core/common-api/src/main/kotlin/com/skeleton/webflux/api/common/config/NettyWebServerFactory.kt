package com.skeleton.webflux.api.common.config

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.server.HttpServer
import reactor.netty.tcp.TcpServer

@Configuration
class NettyWebServerFactory {
    @Bean
    fun nettyReactiveWebServerFactory(): NettyReactiveWebServerFactory? {
        return NettyReactiveWebServerFactory()
            .apply {
                addServerCustomizers(EventLoopNettyCustomizer())
            }
    }

    private class EventLoopNettyCustomizer : NettyServerCustomizer {
        override fun apply(httpServer: HttpServer): HttpServer {
            val parentGroup: EventLoopGroup = NioEventLoopGroup()
            val childGroup: EventLoopGroup = NioEventLoopGroup()
            return httpServer.tcpConfiguration { tcpServer: TcpServer ->
                tcpServer.bootstrap { serverBootstrap: ServerBootstrap ->
                    serverBootstrap.group(parentGroup, childGroup).channel(
                        NioServerSocketChannel::class.java
                    )
                }
            }
        }
    }
}
