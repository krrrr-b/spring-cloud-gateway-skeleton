package com.skeleton.webflux.data.mongo.config.collections.user

import com.bol.secure.Encrypted
import com.skeleton.webflux.data.mongo.BaseTimeDocument
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("users")
data class Users @PersistenceConstructor
constructor(
    @Field("user_id")
    val userId: Long,

    @Encrypted
    @Field("name")
    val name: String,
) : BaseTimeDocument()
