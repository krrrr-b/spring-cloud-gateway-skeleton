package com.skeleton.webflux.common.environment

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertySource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import java.io.IOException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class YmlFileLoaderProcessor : EnvironmentPostProcessor {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication
    ) {
        val activeProfiles: List<String> = environment.activeProfiles.toList()
        var resourceLoader = application.resourceLoader
        if (null == resourceLoader) {
            resourceLoader = DefaultResourceLoader()
        }
        val resourcePatternResolver: ResourcePatternResolver =
            PathMatchingResourcePatternResolver(resourceLoader)
        try {
            val resources =
                resourcePatternResolver.getResources("classpath*:/application-*")
            val yamlPropertySourceLoader = YamlPropertySourceLoader()
            for (resource in resources) {
                val propertySources: List<PropertySource<*>> =
                    ArrayList(
                        yamlPropertySourceLoader.load(
                            resource.filename,
                            resource
                        )
                    )
                for (propertySource in propertySources) {
                    if (propertySource.source is LinkedHashMap<*, *>) {
                        val profile =
                            (propertySource.source as LinkedHashMap<*, *>)["spring.profiles"]
                        if (null != profile) {
                            if (activeProfiles.contains(profile.toString())) {
                                environment.propertySources.addLast(propertySource)
                            }
                        } else {
                            environment.propertySources.addLast(propertySource)
                        }
                    } else if (propertySource.source.javaClass == UNMODIFIABLE_MAP_CLASS) {
                        val profile = propertySource.getProperty("spring.profiles")
                        if (null != profile) {
                            if (activeProfiles.contains(profile.toString())) {
                                environment.propertySources.addLast(propertySource)
                            }
                        }
                    } else {
                        environment.propertySources.addLast(propertySource)
                    }
                }
            }
        } catch (ex: IOException) {
            logger.error("$this {}", ex.toString(), ex)
        }
    }

    companion object {
        private val UNMODIFIABLE_MAP_CLASS: Class<*> = java.util.Collections.unmodifiableMap(HashMap<Any?, Any?>()).javaClass
    }
}
