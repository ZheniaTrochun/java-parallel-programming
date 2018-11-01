package com.yevhenii.service.utils;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

public class ControllerUtils {

    public static <T> ResponseEntity<T> failWithLogging(Throwable e, String message, Logger log) {
        e.printStackTrace();
        log.error(message, e);
        return ResponseEntity.status(500).build();
    }
}
