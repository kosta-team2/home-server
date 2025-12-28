package com.home.infrastructure.batch.trade.dto;

public record Result(long attempted, long inserted, long skipped) {
}
