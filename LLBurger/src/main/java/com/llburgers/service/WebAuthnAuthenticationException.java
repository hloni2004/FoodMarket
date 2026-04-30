package com.llburgers.service;

public class WebAuthnAuthenticationException extends IllegalArgumentException {

    public WebAuthnAuthenticationException(String message) {
        super(message);
    }
}
