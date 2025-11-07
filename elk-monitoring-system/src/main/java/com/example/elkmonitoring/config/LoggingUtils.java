package com.example.elkmonitoring.config;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;

import java.util.Map;

/**
 * 구조화된 로깅 유틸리티
 * JSON 로그에 추가 필드를 쉽게 포함시킬 수 있는 헬퍼 클래스
 */
@Slf4j
public class LoggingUtils {

    /**
     * 구조화된 정보와 함께 INFO 레벨 로그 생성
     */
    public static void logInfo(Logger logger, String message, Map<String, Object> fields) {
        if (fields != null && !fields.isEmpty()) {
            logger.info(message, StructuredArguments.entries(fields));
        } else {
            logger.info(message);
        }
    }

    /**
     * 구조화된 정보와 함께 ERROR 레벨 로그 생성
     */
    public static void logError(Logger logger, String message, Throwable throwable, Map<String, Object> fields) {
        if (fields != null && !fields.isEmpty()) {
            logger.error(message, StructuredArguments.entries(fields), throwable);
        } else {
            logger.error(message, throwable);
        }
    }

    /**
     * 구조화된 정보와 함께 WARN 레벨 로그 생성
     */
    public static void logWarn(Logger logger, String message, Map<String, Object> fields) {
        if (fields != null && !fields.isEmpty()) {
            logger.warn(message, StructuredArguments.entries(fields));
        } else {
            logger.warn(message);
        }
    }

    /**
     * 비즈니스 이벤트 로깅 (분석용)
     */
    public static void logBusinessEvent(Logger logger, String eventName, Map<String, Object> eventData) {
        eventData.put("event_type", "business_event");
        eventData.put("event_name", eventName);
        logger.info("Business Event: {}", StructuredArguments.entries(eventData));
    }

    /**
     * 성능 측정 로깅
     */
    public static void logPerformance(Logger logger, String operation, long durationMs, Map<String, Object> metadata) {
        metadata.put("operation", operation);
        metadata.put("duration_ms", durationMs);
        metadata.put("event_type", "performance");
        logger.info("Performance: {} took {} ms", operation, durationMs, StructuredArguments.entries(metadata));
    }
}
