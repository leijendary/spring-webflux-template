package com.leijendary.spring.webflux.template.core.interceptor

import com.leijendary.spring.webflux.template.core.config.properties.AuthProperties
import com.leijendary.spring.webflux.template.core.util.CONTEXT_TRACE_ID
import com.leijendary.spring.webflux.template.core.util.CONTEXT_USER_ID
import com.leijendary.spring.webflux.template.core.util.HEADER_TRACE_ID
import com.leijendary.spring.webflux.template.core.util.HEADER_USER_ID
import io.grpc.*
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.Metadata.Key.of
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import reactor.core.publisher.Mono
import java.util.UUID.randomUUID

val CONTEXT_KEY_USER_ID: Context.Key<String> = Context.key(HEADER_USER_ID)
val CONTEXT_KEY_TRACE_ID: Context.Key<String> = Context.key(HEADER_TRACE_ID)

@GrpcGlobalServerInterceptor
class GrpcMetaDataInterceptor(private val authProperties: AuthProperties) : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val userId = headers[of(HEADER_USER_ID, ASCII_STRING_MARSHALLER)] ?: authProperties.system.principal
        val traceId = headers[of(HEADER_TRACE_ID, ASCII_STRING_MARSHALLER)] ?: randomUUID().toString()

        return object : SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            override fun onMessage(message: ReqT) {
                Mono
                    .justOrEmpty(message)
                    .contextWrite {
                        it
                            .put(CONTEXT_USER_ID, userId)
                            .put(CONTEXT_TRACE_ID, traceId)
                    }
                    .doOnNext { super.onMessage(it) }
                    .subscribe()
            }
        }
    }
}