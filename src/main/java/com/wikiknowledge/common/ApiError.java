package com.wikiknowledge.common;

public record ApiError(int status, String code, String message) {
}
