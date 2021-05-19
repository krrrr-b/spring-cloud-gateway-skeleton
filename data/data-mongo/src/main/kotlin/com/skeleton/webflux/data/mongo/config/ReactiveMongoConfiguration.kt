//package com.skeleton.webflux.data.mongo.config
//
//import com.mongodb.MongoClientSettings
//import com.mongodb.MongoCredential
//import com.mongodb.ServerAddress
//import com.mongodb.reactivestreams.client.MongoClient
//import com.mongodb.reactivestreams.client.MongoClients
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
//import org.springframework.data.mongodb.ReactiveMongoTransactionManager
//import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
//import org.springframework.data.mongodb.core.ReactiveMongoTemplate
//import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
//import org.springframework.data.mongodb.core.convert.MappingMongoConverter
//import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
//import org.springframework.transaction.ReactiveTransactionManager
//import org.springframework.transaction.reactive.TransactionalOperator
//import java.util.concurrent.TimeUnit
//import com.bol.secure.CachedEncryptionEventListener
//import com.bol.crypt.CryptVault
//
//@Configuration
//@EnableReactiveMongoRepositories(basePackages = ["com.skeleton.webflux.data.mongo"])
//class ReactiveMongoConfiguration(
//    private val hosts: String,
//    private val username: String,
//    private val password: String,
//    private val database: String,
//) : AbstractReactiveMongoConfiguration() {
//    private val authenticationDatabase: String = "userauth"
//
//    companion object {
//        const val MAX_POOL_SIZE = 250
//        const val MAX_WAIT_MINUTE = 3L
//        const val READ_TIMEOUT_SECOND = 5
//        const val CONNECTION_IDLE_SECOND = 0L
//        const val CONNECTION_TIMEOUT_SECOND = 10
//        const val SERVER_SELECTION_TIMEOUT_SECOND = 15L
//
//        const val REACTIVE_MONGO_TEMPLATE = "REACTIVE_MONGO_TEMPLATE"
//        const val REACTIVE_MONGO_FACTORY = "REACTIVE_MONGO_FACTORY"
//    }
//
//    override fun getDatabaseName(): String = database
//
//    @Bean(REACTIVE_MONGO_TEMPLATE)
//    override fun reactiveMongoTemplate(
//        databaseFactory: ReactiveMongoDatabaseFactory,
//        mongoConverter: MappingMongoConverter,
//    ): ReactiveMongoTemplate {
//        return ReactiveMongoTemplate(databaseFactory, mongoConverter)
//    }
//
//    @Bean
//    fun cryptVault(): CryptVault {
//        return CryptVault()
//            .with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(1, "key")
//            .withDefaultKeyVersion(1)
//    }
//
//    @Bean
//    fun encryptionEventListener(cryptVault: CryptVault): CachedEncryptionEventListener {
//        return CachedEncryptionEventListener(cryptVault)
//    }
//
//    @Primary
//    @Bean(REACTIVE_MONGO_FACTORY)
//    override fun reactiveMongoDbFactory() =
//        SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), databaseName)
//
//    @Bean
//    override fun reactiveMongoClient(): MongoClient {
//        return MongoClients.create(
//            MongoClientSettings.builder()
//                .applyToClusterSettings {
//                    it.hosts(
//                        hosts.split(",")
//                            .map { h -> ServerAddress(h) }
//                            .toList()
//                    )
//                }
//                .credential(
//                    MongoCredential.createCredential(
//                        username,
//                        authenticationDatabase,
//                        password.toCharArray()
//                    )
//                )
//                .applyToConnectionPoolSettings { builder ->
//                    builder.maxSize(MAX_POOL_SIZE)
//                }
//                .applyToConnectionPoolSettings { builder ->
//                    builder.maxConnectionIdleTime(CONNECTION_IDLE_SECOND, TimeUnit.SECONDS)
//                }
//                .applyToConnectionPoolSettings { builder ->
//                    builder.maxWaitTime(MAX_WAIT_MINUTE, TimeUnit.MINUTES)
//                }
//                .applyToClusterSettings { builder ->
//                    builder.serverSelectionTimeout(SERVER_SELECTION_TIMEOUT_SECOND, TimeUnit.SECONDS)
//                }
//                .applyToSocketSettings { builder ->
//                    builder.connectTimeout(CONNECTION_TIMEOUT_SECOND, TimeUnit.SECONDS)
//                }
//                .applyToSocketSettings { builder ->
//                    builder.readTimeout(READ_TIMEOUT_SECOND, TimeUnit.SECONDS)
//                }
//                .build()
//        )
//    }
//
//    @Bean
//    fun transactionalOperator(reactiveTransactionManager: ReactiveTransactionManager) =
//        TransactionalOperator.create(reactiveTransactionManager)
//
//    @Bean
//    fun reactiveTransactionManager(reactiveMongoDatabaseFactory: ReactiveMongoDatabaseFactory) =
//        ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory)
//}
