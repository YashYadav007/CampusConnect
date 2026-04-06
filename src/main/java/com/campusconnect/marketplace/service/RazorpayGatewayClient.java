package com.campusconnect.marketplace.service;

import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.config.RazorpayProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class RazorpayGatewayClient implements RazorpayGateway {

  private final RazorpayProperties razorpayProperties;

  @Override
  public RazorpayOrderResult createOrder(long amountInPaise, String receipt) {
    RestClient client = RestClient.builder()
        .baseUrl("https://api.razorpay.com/v1")
        .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth())
        .build();

    RazorpayCreateOrderResponse response = client.post()
        .uri("/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new RazorpayCreateOrderRequest(amountInPaise, "INR", receipt, 0))
        .retrieve()
        .body(RazorpayCreateOrderResponse.class);

    if (response == null || response.id() == null || response.amount() == null || response.currency() == null) {
      throw new BadRequestException("Failed to create Razorpay order");
    }

    return new RazorpayOrderResult(response.id(), response.amount(), response.currency());
  }

  private String basicAuth() {
    String auth = razorpayProperties.getKeyId() + ":" + razorpayProperties.getKeySecret();
    String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoded;
  }

  private record RazorpayCreateOrderRequest(long amount, String currency, String receipt, int payment_capture) {
  }

  private record RazorpayCreateOrderResponse(String id, Long amount, String currency) {
  }
}
