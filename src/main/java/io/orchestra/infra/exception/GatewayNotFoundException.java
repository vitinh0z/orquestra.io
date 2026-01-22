package io.orchestra.infra.exception;

public class GatewayNotFoundException extends RuntimeException {
    public GatewayNotFoundException(String message) {
        super(message);
    }
}
