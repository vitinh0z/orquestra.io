package io.orchestra.cloud.infra.exception;

public class GatewayNotFoundException extends RuntimeException {
    public GatewayNotFoundException(String message) {
        super(message);
    }
}
