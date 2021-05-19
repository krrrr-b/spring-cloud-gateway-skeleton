package com.skeleton.webflux.data.mongo.config.collections.user

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UsersRepository : ReactiveMongoRepository<Users, String> {
    fun findByUserId(userId: Long): Mono<Users>
}
