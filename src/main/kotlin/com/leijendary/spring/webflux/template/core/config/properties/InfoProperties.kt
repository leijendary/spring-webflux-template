package com.leijendary.spring.webflux.template.core.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "info")
class InfoProperties {
    var app: App = App()

    inner class App {
        var organization: String = ""
        var group: String = ""
        var name: String = ""
        var description: String = ""
        var version: String = ""
    }
}