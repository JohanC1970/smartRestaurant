package com.smartRestaurant.orders.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente HTTP para comunicarse con la API de Wompi
 * Maneja creación de transacciones y reembolsos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WompiPaymentClient {

    @Value("${wompi.api.key}")
    private String wompiApiKey;

    @Value("${wompi.api.public-key}")
    private String wompiPublicKey;

    @Value("${wompi.api.environment:test}")
    private String wompiEnvironment;

    @Value("${wompi.api.url-test:https://staging.wompi.co/api}")
    private String wompiUrlTest;

    @Value("${wompi.api.url-production:https://production.wompi.co/api}")
    private String wompiUrlProduction;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Obtiene la URL base de Wompi según el ambiente
     */
    private String getBaseUrl() {
        return "test".equalsIgnoreCase(wompiEnvironment) ? wompiUrlTest : wompiUrlProduction;
    }

    /**
     * Crea una transacción en Wompi
     * @param token Token generado por el cliente (Wompi.js)
     * @param amountInCents Monto en centavos
     * @param reference ID de referencia único (ej: orderId)
     * @param customerEmail Email del cliente
     * @param customerPhone Teléfono del cliente
     * @param description Descripción del pago
     * @param metadata Datos adicionales
     * @return JsonNode con la respuesta de Wompi
     */
    public JsonNode createTransaction(
            String token,
            long amountInCents,
            String reference,
            String customerEmail,
            String customerPhone,
            String description,
            Map<String, Object> metadata) throws IOException {

        log.info(" [WOMPI] Creando transacción: reference={}, monto={}", reference, amountInCents);

        // Construir el payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount_in_cents", amountInCents);
        payload.put("currency", "COP");
        payload.put("reference", reference);
        payload.put("payment_method", buildPaymentMethod(token));
        payload.put("customer_email", customerEmail);
        payload.put("customer_phone", customerPhone);
        payload.put("description", description);
        payload.put("redirect_url", "");

        if (metadata != null) {
            payload.put("metadata", metadata);
        }

        String jsonPayload = objectMapper.writeValueAsString(payload);
        log.debug(" [WOMPI] Enviando payload: {}", jsonPayload);

        // Construir la petición HTTP
        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
        );

        String url = getBaseUrl() + "/transactions";
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + wompiApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            log.debug(" [WOMPI] Respuesta status={}, body={}", response.code(), responseBody);

            if (!response.isSuccessful()) {
                log.error(" [WOMPI] Error en transacción: status={}, body={}", response.code(), responseBody);
                throw new IOException("Wompi API error: " + response.code() + " - " + responseBody);
            }

            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            log.info(" [WOMPI] Transacción creada: status={}", jsonResponse.path("data").path("status").asText());

            return jsonResponse;
        }
    }

    /**
     * Obtiene el estado de una transacción existente
     */
    public JsonNode getTransaction(String transactionId) throws IOException {
        log.info(" [WOMPI] Obteniendo transacción: {}", transactionId);

        String url = getBaseUrl() + "/transactions/" + transactionId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + wompiApiKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                log.error(" [WOMPI] Error obteniendo transacción: {}", response.code());
                throw new IOException("Wompi API error: " + response.code());
            }

            return objectMapper.readTree(responseBody);
        }
    }

    /**
     * Crea un reembolso en Wompi
     */
    public JsonNode createRefund(String transactionId, long amountInCents) throws IOException {
        log.info(" [WOMPI] Creando reembolso: transactionId={}, monto={}", transactionId, amountInCents);

        // Construir el payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount_in_cents", amountInCents);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
        );

        String url = getBaseUrl() + "/transactions/" + transactionId + "/refunds";
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + wompiApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            log.debug(" [WOMPI] Respuesta reembolso: status={}", response.code());

            if (!response.isSuccessful()) {
                log.error(" [WOMPI] Error en reembolso: {}", response.code());
                throw new IOException("Wompi API error: " + response.code());
            }

            return objectMapper.readTree(responseBody);
        }
    }

    /**
     * Construye el objeto de método de pago para Wompi
     */
    private Map<String, Object> buildPaymentMethod(String token) {
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("type", "CARD");
        paymentMethod.put("token", token);
        return paymentMethod;
    }
}

