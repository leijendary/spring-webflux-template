package com.leijendary.spring.webflux.template

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK

@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureWebTestClient
class ApplicationTests
