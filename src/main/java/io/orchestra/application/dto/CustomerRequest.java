package io.orchestra.application.dto;

public record CustomerRequest(

        String id,

        String email,

        String document
) {}
