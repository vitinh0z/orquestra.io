package io.orchestra.core.application.dto;

public record CustomerRequest(

        String id,

        String email,

        String document
) {}
