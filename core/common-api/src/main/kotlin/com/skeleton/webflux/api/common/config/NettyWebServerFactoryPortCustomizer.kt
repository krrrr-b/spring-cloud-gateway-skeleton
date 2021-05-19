package com.skeleton.webflux.api.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component
import reactor.netty.http.server.HttpServer

@Component
class NettyWebServerFactoryPortCustomizer(
    @Value("\${server.port:8080}") private val port: Int
) : WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    override fun customize(serverFactory: NettyReactiveWebServerFactory) {
        serverFactory.addServerCustomizers(PortCustomizer(port))
    }

    private class PortCustomizer constructor(private val port: Int) : NettyServerCustomizer {
        override fun apply(httpServer: HttpServer): HttpServer {
            return httpServer.port(port)
        }
    }
}
