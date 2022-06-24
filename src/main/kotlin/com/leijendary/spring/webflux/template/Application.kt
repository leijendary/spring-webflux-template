package com.leijendary.spring.webflux.template

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import org.springframework.aop.SpringProxy
import org.springframework.aop.framework.Advised
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.DecoratingProxy
import org.springframework.nativex.hint.TypeHint
import org.springframework.transaction.ReactiveTransactionManager
import java.io.Serializable

@TypeHint(
    types = [
        AWSStaticCredentialsProvider::class,
        BasicAWSCredentials::class,
        ProfileCredentialsProvider::class,
        InitializingBean::class,
        ReactiveTransactionManager::class,
        Serializable::class,
        SpringProxy::class,
        Advised::class,
        DecoratingProxy::class,
    ],
    typeNames = [
        "com.amazonaws.auth.AWSStaticCredentialsProvider",
        "com.amazonaws.auth.BasicAWSCredentials",
        "com.amazonaws.auth.profile.ProfileCredentialsProvider",
        "org.springframework.beans.factory.InitializingBean",
        "org.springframework.transaction.ReactiveTransactionManager",
        "java.io.Serializable",
        "org.springframework.aop.SpringProxy",
        "org.springframework.aop.framework.Advised",
        "org.springframework.core.DecoratingProxy",
    ]
)
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
