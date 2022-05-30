package com.leijendary.spring.webflux.template.core.security

import com.leijendary.spring.webflux.template.core.util.HEADER_SCOPE
import com.leijendary.spring.webflux.template.core.util.HEADER_USER_ID
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class SecurityAuthentication(request: ServerHttpRequest, private val anonymousUser: String) : Authentication {
    private val userId = request.headers.getFirst(HEADER_USER_ID)
    private val scopes = request.headers.getFirst(HEADER_SCOPE)?.split(" ") ?: listOf()

    override fun getName(): String = userId ?: anonymousUser

    override fun getAuthorities(): List<GrantedAuthority> = scopes.map { GrantedAuthority { it } }

    override fun getCredentials(): Any? = userId

    override fun getDetails(): Any? = userId

    override fun getPrincipal(): Any = userId ?: anonymousUser

    override fun isAuthenticated(): Boolean = userId != null

    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.isAuthenticated = isAuthenticated
    }
}