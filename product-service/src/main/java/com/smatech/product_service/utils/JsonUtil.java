package com.smatech.product_service.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */

public class JsonUtil {

        private static final Logger log = (Logger) LoggerFactory.getLogger(JsonUtil.class);

        private JsonUtil() {
        }

        public static String toJson(Object item) {
            try {
                return getJacksonObjectMapper().writeValueAsString(item);
            } catch (JsonProcessingException var2) {
                log.warn("Util Exception (toJson)", var2);
                return null;
            }
        }

        public static <T> T fromJson(String string, Class<T> type) {
            try {
                return fromJsonUnCaught(replaceControlCharacters(string), type);
            } catch (IOException var3) {
                //IOException var3 = var3;
                log.warn("Util Exception (fromJson)", var3);
                throw new RuntimeException("Error processing request");
            }
        }
        public static <T> T fromJson(String string, TypeReference<T> type) {
            try {
                return fromJsonUnCaught(replaceControlCharacters(string), type);
            } catch (IOException var3) {
                log.warn("Util Exception (fromJson)", var3);
                throw new RuntimeException("Error processing request");
            }
        }

        private static <T> T fromJsonUnCaught(String string, Class<T> type) throws IOException {
            return getJacksonObjectMapper().readValue(string, type);
        }
        private static <T> T fromJsonUnCaught(String string, TypeReference<T> type) throws IOException {
            return getJacksonObjectMapper().readValue(string, type);
        }

        private static ObjectMapper getJacksonObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
            return objectMapper;
        }

        private static String replaceControlCharacters(String input) {
            return input.replaceAll("[\\p{Cntrl}]", "");
        }
    }


