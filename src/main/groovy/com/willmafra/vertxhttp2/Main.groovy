package com.willmafra.vertxhttp2

import groovy.util.logging.Slf4j
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router

/**
 * Created by will on 17/09/16.
 */
@Slf4j
class Main {

    static void main(String[] args) throws Exception {
        def vertx = Vertx.vertx()
        def options = [
                logActivity         : true,
                openSslEngineOptions: [
                        alpnAvailable      : true,
                        available          : true,
                        sessionCacheEnabled: true
                ],

                useAlpn             : true,
                ssl                 : true,
                keyStoreOptions     : [
                        password: 'vertx-http2',
                        path    : "keystore"
                ]
        ]
        def server = vertx.createHttpServer(options)

        def router = Router.router(vertx)
        router.route("/http2").handler({ routingContext ->
            def request = routingContext.request()
            def response = request.response()

// Push main.js to the client
            response.push(HttpMethod.GET, "/main.js", { ar ->
                if (ar.succeeded()) {
                    // The server is ready to push the response
                    def pushedResponse = ar.result()
                    // Send main.js response
                    pushedResponse.putHeader("content-type", "application/json").end("alert(\"Push response hello\")")
                } else {
                    println("Could not push client resource ${ar.cause()}")
                }
            })

// Send the requested resource
            response.end("<html><head><script src=\"/main.js\"></script></head><body></body></html>")
        })


        server.requestHandler(router.&accept).listen(8080)

        log.info("Server running https://localhost:8080/http2")
    }
}
