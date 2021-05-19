package com.skeleton.webflux.api.common.filter.logger

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JsonLogger {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun log(text: String) {
        try {
            logger.info(ObjectMapper().writeValueAsString(text))
        } catch (ex: JsonProcessingException) {
            logger.error("$this ${ex.message}", ex)
        }
    }

    fun log(template: JsonTemplate) {
        try {
            logger.info(ObjectMapper().writeValueAsString(template))
        } catch (ex: JsonProcessingException) {
            logger.error("$this ${ex.message}", ex)
        }
    }
}
