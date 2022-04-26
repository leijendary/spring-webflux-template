package com.leijendary.spring.webflux.template.repository

import com.leijendary.spring.webflux.template.document.SampleDocument
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository
import java.util.*

interface SampleSearchRepository : ReactiveElasticsearchRepository<SampleDocument, UUID>