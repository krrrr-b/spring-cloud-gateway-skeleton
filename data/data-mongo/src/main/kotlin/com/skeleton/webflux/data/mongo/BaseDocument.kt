package com.skeleton.webflux.data.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

abstract class BaseDocument(
    @Id
    var id: ObjectId? = null
)
