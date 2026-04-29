// Inopay Java SDK — synchronous client wrapping the public sandbox.
package com.inopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InopayClient {

    public static final String DEFAULT_BASE_URL = "https://api.getinopay.com/v1/sandbox";

    private final String apiKey;
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;

    private InopayClient(Builder b) {
        this.apiKey = Objects.requireNonNull(b.apiKey, "api_key is required");
        if (apiKey.isEmpty()) throw new IllegalArgumentException("api_key must not be empty");
        this.baseUrl = stripTrailingSlash(b.baseUrl != null ? b.baseUrl : DEFAULT_BASE_URL);
        this.http = b.http != null ? b.http : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.mapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private <T> T request(String method, String path, Object body, Class<T> type) {
        URI uri = URI.create(baseUrl + (path.startsWith("/") ? path : "/" + path));
        HttpRequest.Builder req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json");

        try {
            switch (method) {
                case "GET" -> req.GET();
                case "POST" -> {
                    String json = body == null ? "" : mapper.writeValueAsString(body);
                    req.header("Content-Type", "application/json")
                       .POST(HttpRequest.BodyPublishers.ofString(json));
                }
                case "DELETE" -> req.DELETE();
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            }

            HttpResponse<String> resp = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            String text = resp.body();

            if (status >= 200 && status < 300) {
                return mapper.readValue(text, type);
            }

            String code = "http_" + status;
            String detail = text;
            try {
                Map<?, ?> err = mapper.readValue(text, Map.class);
                if (err.get("error") != null) code = err.get("error").toString();
                if (err.get("detail") != null) detail = err.get("detail").toString();
            } catch (Exception ignore) {
                // not a JSON error payload
            }
            throw new InopayException(status, code, detail);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new InopayException(-1, "transport", e.getMessage());
        }
    }

    // ── Endpoints ────────────────────────────────────────────────

    public HealthResult health() {
        return request("GET", "/health", null, HealthResult.class);
    }

    public InstrumentList instrumentsList() {
        return request("GET", "/instruments", null, InstrumentList.class);
    }

    public SgiList sgisList() {
        return request("GET", "/sgis", null, SgiList.class);
    }

    public OrderResponse orderCreate(String symbol, String side, int qty, String sgiId) {
        Map<String, Object> body = new HashMap<>();
        body.put("symbol", symbol);
        body.put("side", side);
        body.put("qty", qty);
        if (sgiId != null) body.put("sgi_id", sgiId);
        return request("POST", "/orders", body, OrderResponse.class);
    }

    public OrderResponse orderCreate(String symbol, String side, int qty) {
        return orderCreate(symbol, side, qty, null);
    }

    public OrderResponse orderGet(String orderId) {
        return request("GET", "/orders/" + orderId, null, OrderResponse.class);
    }

    public KycResponse kycFetch(String userId) {
        return request("GET", "/kyc/" + userId, null, KycResponse.class);
    }

    public SandboxResetResult sandboxReset() {
        return request("POST", "/sandbox/reset", null, SandboxResetResult.class);
    }

    // ── Builder ──────────────────────────────────────────────────

    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private HttpClient http;

        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder httpClient(HttpClient http) { this.http = http; return this; }
        public InopayClient build() { return new InopayClient(this); }
    }

    // ── DTOs ─────────────────────────────────────────────────────

    public record HealthResult(
            boolean sandbox,
            String status,
            @JsonProperty("demo_key") String demoKey,
            @JsonProperty("rate_limit") String rateLimit
    ) {}

    public record Instrument(
            String symbol,
            String name,
            String market,
            String currency,
            @JsonProperty("last_price") double lastPrice,
            @JsonProperty("change_pct") double changePct
    ) {}

    public record InstrumentList(
            Boolean sandbox,
            @JsonProperty("as_of") String asOf,
            List<Instrument> instruments
    ) {}

    public record Sgi(
            String id,
            String name,
            String market,
            @JsonProperty("fill_rate") double fillRate
    ) {}

    public record SgiList(boolean sandbox, List<Sgi> sgis) {}

    public record Order(
            String id,
            String symbol,
            String side,
            int qty,
            @JsonProperty("sgi_id") String sgiId,
            String status,
            @JsonProperty("avg_price") double avgPrice,
            @JsonProperty("filled_qty") int filledQty,
            @JsonProperty("filled_at") String filledAt,
            @JsonProperty("settlement_date") String settlementDate,
            @JsonProperty("settlement_currency") String settlementCurrency
    ) {}

    public record OrderResponse(boolean sandbox, Order order, String note) {}

    public record KycAttestation(
            String schema,
            @JsonProperty("user_id") String userId,
            String issuer,
            String level,
            @JsonProperty("issued_at") String issuedAt,
            @JsonProperty("expires_at") String expiresAt,
            @JsonProperty("key_id") String keyId,
            @JsonProperty("ed25519_signature") String ed25519Signature
    ) {}

    public record KycResponse(boolean sandbox, KycAttestation attestation, String note) {}

    public record SandboxResetResult(
            boolean sandbox,
            @JsonProperty("reset_at") String resetAt,
            @JsonProperty("wallet_credit_cents") long walletCreditCents,
            String message
    ) {}
}
