package com.campusconnect.marketplace.service;

public record RazorpayOrderResult(String orderId, long amount, String currency) {
}
