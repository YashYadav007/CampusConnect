package com.campusconnect.marketplace.service;

public interface RazorpayGateway {
  RazorpayOrderResult createOrder(long amountInPaise, String receipt);
}
