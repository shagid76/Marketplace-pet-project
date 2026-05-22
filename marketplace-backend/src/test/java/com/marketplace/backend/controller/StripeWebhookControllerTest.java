package com.marketplace.backend.controller;

import com.marketplace.backend.service.CartService;
import com.marketplace.backend.service.PaymentService;
import com.marketplace.backend.service.ProductService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StripeWebhookController.class,
		excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class StripeWebhookControllerTest {

	@Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;

	@MockitoBean PaymentService paymentService;
	@MockitoBean ProductService productService;
	@MockitoBean CartService cartService;

	@Autowired StripeWebhookController controller;

	@Test
	@WithMockUser
	void invalidSignature_returns400() throws Exception {
		ReflectionTestUtils.setField(controller, "webhookSecret", "whsec_test");

		try (MockedStatic<Webhook> mocked = mockStatic(Webhook.class)) {
			mocked.when(() -> Webhook.constructEvent(any(), any(), any()))
					.thenThrow(new SignatureVerificationException("bad sig", "sig"));

			mockMvc.perform(post("/api/payments/webhook")
							.header("Stripe-Signature", "bad")
							.content("{}"))
					.andExpect(status().isBadRequest());
		}
	}

	@Test
	@WithMockUser
	void unknownEventType_returns200_andIgnores() throws Exception {
		ReflectionTestUtils.setField(controller, "webhookSecret", "whsec_test");

		Event ev = new Event();
		ev.setType("some.unknown.event");

		try (MockedStatic<Webhook> mocked = mockStatic(Webhook.class)) {
			mocked.when(() -> Webhook.constructEvent(any(), any(), any())).thenReturn(ev);

			mockMvc.perform(post("/api/payments/webhook")
							.header("Stripe-Signature", "sig")
							.content("{}"))
					.andExpect(status().isOk());
		}
	}
}
