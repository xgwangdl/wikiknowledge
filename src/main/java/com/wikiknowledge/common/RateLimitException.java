package com.wikiknowledge.common;

/** 限流异常，命中限流时抛出 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
