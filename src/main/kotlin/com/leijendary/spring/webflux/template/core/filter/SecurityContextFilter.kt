package com.leijendary.spring.webflux.template.core.filter

import com.leijendary.spring.webflux.template.core.config.properties.AuthProperties
import com.leijendary.spring.webflux.template.core.security.SecurityAuthentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SecurityContextFilter(authProperties: AuthProperties) : WebFilter {
    private val anonymousUser = authProperties.anonymousUser.principal

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authentication = SecurityAuthentication(exchange.request, anonymousUser)

        return chain
            .filter(exchange)
            .contextWrite(withAuthentication(authentication))
    }
}