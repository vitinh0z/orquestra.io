package io.orchestra.cloud.infra.exception;

import java.io.Serializable;

public record StandardError(
        Long timestamp,
        Integer status,
        String error,
        String message,
        String path
) implements Serializable {
}
