package com.campusconnect.marketplace;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusconnect.marketplace.entity.MarketplaceItem;
import com.campusconnect.marketplace.repository.MarketplaceItemRepository;
import com.campusconnect.marketplace.service.RazorpayGateway;
import com.campusconnect.marketplace.service.RazorpayOrderResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MarketplaceIntegrationTest {

  private static final String TEST_RAZORPAY_SECRET = "test_key_secret_123456789";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MarketplaceItemRepository marketplaceItemRepository;

  @MockBean private RazorpayGateway razorpayGateway;

  @BeforeEach
  void setUpGateway() {
    when(razorpayGateway.createOrder(anyLong(), anyString()))
        .thenAnswer(invocation -> {
          long amount = invocation.getArgument(0, Long.class);
          return new RazorpayOrderResult("order_test_" + UUID.randomUUID(), amount, "INR");
        });
  }

  @Test
  void createListingAndPublicReadWork() throws Exception {
    register("Seller One", "seller-one@example.com");
    String sellerToken = login("seller-one@example.com");

    long itemId = createListing(
        sellerToken,
        "Scientific calculator FX-991ES Plus",
        "Academics",
        "Used",
        "1200.00",
        "100.00"
    );

    mockMvc.perform(get("/api/marketplace"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value((int) itemId))
        .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));

    mockMvc.perform(get("/api/marketplace/" + itemId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title").value("Scientific calculator FX-991ES Plus"))
        .andExpect(jsonPath("$.data.tokenAmount").value(100.00));
  }

  @Test
  void sellerCannotBuyOwnItem() throws Exception {
    register("Seller Two", "seller-two@example.com");
    String sellerToken = login("seller-two@example.com");

    long itemId = createListing(sellerToken, "Wooden study desk", "Furniture", "Good", "3000.00", "300.00");

    mockMvc.perform(post("/api/marketplace/" + itemId + "/create-order")
            .header("Authorization", "Bearer " + sellerToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void orderCreationVerifySuccessAndDoubleVerifyAreSafe() throws Exception {
    register("Seller Three", "seller-three@example.com");
    register("Buyer Three", "buyer-three@example.com");

    String sellerToken = login("seller-three@example.com");
    String buyerToken = login("buyer-three@example.com");

    long itemId = createListing(sellerToken, "Engineering drawing kit", "Stationery", "Like New", "900.00", "90.00");

    String orderResp = mockMvc.perform(post("/api/marketplace/" + itemId + "/create-order")
            .header("Authorization", "Bearer " + buyerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.keyId").value("test_key_id"))
        .andExpect(jsonPath("$.data.amount").value(9000))
        .andReturn()
        .getResponse()
        .getContentAsString();

    String orderId = objectMapper.readTree(orderResp).path("data").path("orderId").asText();
    String paymentId = "pay_test_1";
    String signature = sign(orderId, paymentId);

    String verifyBody = String.format(
        """
            {
              "itemId": %d,
              "razorpayOrderId": "%s",
              "razorpayPaymentId": "%s",
              "razorpaySignature": "%s"
            }
            """,
        itemId,
        orderId,
        paymentId,
        signature
    );

    mockMvc.perform(post("/api/marketplace/payments/verify")
            .header("Authorization", "Bearer " + buyerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(verifyBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data.razorpayPaymentId").value(paymentId));

    mockMvc.perform(post("/api/marketplace/payments/verify")
            .header("Authorization", "Bearer " + buyerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(verifyBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("SUCCESS"));

    mockMvc.perform(get("/api/marketplace/" + itemId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("RESERVED"))
        .andExpect(jsonPath("$.data.reservedBy.fullName").value("Buyer Three"));
  }

  @Test
  void unavailableItemCannotBePurchasedAgainAndSellerCanMarkSold() throws Exception {
    register("Seller Four", "seller-four@example.com");
    register("Buyer Four", "buyer-four@example.com");
    register("Buyer Five", "buyer-five@example.com");

    String sellerToken = login("seller-four@example.com");
    String buyerOneToken = login("buyer-four@example.com");
    String buyerTwoToken = login("buyer-five@example.com");

    long itemId = createListing(sellerToken, "Cycle helmet with visor", "Sports", "Good", "1500.00", "150.00");

    String orderResp = mockMvc.perform(post("/api/marketplace/" + itemId + "/create-order")
            .header("Authorization", "Bearer " + buyerOneToken))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String orderId = objectMapper.readTree(orderResp).path("data").path("orderId").asText();
    String paymentId = "pay_test_2";
    String signature = sign(orderId, paymentId);

    mockMvc.perform(post("/api/marketplace/payments/verify")
            .header("Authorization", "Bearer " + buyerOneToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format(
                """
                    {
                      "itemId": %d,
                      "razorpayOrderId": "%s",
                      "razorpayPaymentId": "%s",
                      "razorpaySignature": "%s"
                    }
                    """,
                itemId,
                orderId,
                paymentId,
                signature
            )))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/marketplace/" + itemId + "/create-order")
            .header("Authorization", "Bearer " + buyerTwoToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc.perform(patch("/api/marketplace/" + itemId + "/mark-sold")
            .header("Authorization", "Bearer " + sellerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("SOLD"));

    MarketplaceItem item = marketplaceItemRepository.findById(itemId).orElseThrow();
    Assertions.assertEquals("SOLD", item.getStatus().name());
  }

  private void register(String fullName, String email) throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format(
                """
                    {
                      "fullName": "%s",
                      "email": "%s",
                      "password": "password123",
                      "course": "CSE",
                      "yearOfStudy": 3
                    }
                    """,
                fullName,
                email
            )))
        .andExpect(status().isOk());
  }

  private String login(String email) throws Exception {
    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format(
                """
                    {
                      "email": "%s",
                      "password": "password123"
                    }
                    """,
                email
            )))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode json = objectMapper.readTree(response);
    return json.path("data").path("token").asText();
  }

  private long createListing(
      String token,
      String title,
      String category,
      String conditionLabel,
      String price,
      String tokenAmount
  ) throws Exception {
    String response = mockMvc.perform(post("/api/marketplace")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format(
                """
                    {
                      "title": "%s",
                      "description": "Useful item for campus life",
                      "category": "%s",
                      "conditionLabel": "%s",
                      "price": %s,
                      "tokenAmount": %s,
                      "imageUrl": "https://example.com/item.jpg"
                    }
                    """,
                title,
                category,
                conditionLabel,
                price,
                tokenAmount
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(response).path("data").path("id").asLong();
  }

  private String sign(String orderId, String paymentId) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(TEST_RAZORPAY_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] hash = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(hash);
  }
}
