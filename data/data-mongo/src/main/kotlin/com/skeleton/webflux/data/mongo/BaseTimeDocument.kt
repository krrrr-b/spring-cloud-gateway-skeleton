package com.skeleton.webflux.data.mongo

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseTimeDocument(
    @Field("created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null,

    @Field("updated_at")
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,
) : BaseDocument()
