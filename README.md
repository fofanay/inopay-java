# inopay-java

[![JitPack](https://img.shields.io/jitpack/v/github/fofanay/inopay-java.svg)](https://jitpack.io/#fofanay/inopay-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Java SDK — synchronous client for the Inopay African capital markets infrastructure (BRVM, BVMAC, GSE) via the public sandbox.

## Status

`v0.1.0-alpha.2` — public alpha. Wraps `https://api.getinopay.com/v1/sandbox/*`. Maven Central publication coming next.

## Install

### Gradle (KTS)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.fofanay:inopay-java:v0.1.0-alpha.2")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.fofanay</groupId>
    <artifactId>inopay-java</artifactId>
    <version>v0.1.0-alpha.2</version>
</dependency>
```

## Quick start

```java
import com.inopay.InopayClient;

public class Main {
    public static void main(String[] args) {
        InopayClient inopay = InopayClient.builder()
                .apiKey("sk_test_demo_inopay_2026")
                .build();

        var health = inopay.health();
        System.out.println("status: " + health.status() + " · " + health.rateLimit());

        var list = inopay.instrumentsList();
        list.instruments().forEach(i ->
            System.out.println(i.symbol() + ": " + i.lastPrice() + " " + i.currency()));

        var order = inopay.orderCreate("SNTS.BRVM", "buy", 10);
        System.out.println("Order " + order.order().id() + " status: " + order.order().status());

        var kyc = inopay.kycFetch("usr_demo_42");
        System.out.println("Attestation: " + kyc.attestation().issuedAt());
    }
}
```

## API surface

| Method | Description |
|---|---|
| `health()` | Sandbox status |
| `instrumentsList()` | List BRVM / BVMAC / GSE instruments |
| `sgisList()` | List partner SGIs |
| `orderCreate(symbol, side, qty)` / `orderCreate(symbol, side, qty, sgiId)` | Place a simulated order |
| `orderGet(id)` | Read back an order |
| `kycFetch(userId)` | Mock Ed25519-signed KYC attestation |
| `sandboxReset()` | Reset the demo wallet |

## Rate limit

Public demo key `sk_test_demo_inopay_2026` is rate-limited to **60 requests per minute per IP**.
For private quotas request a sandbox key at <https://getinopay.com/fr/developers/sandbox>.

## Requirements

- Java 17+
- Jackson 2.16+ (transitively included)
- Uses the JDK's built-in `java.net.http.HttpClient` — no OkHttp dependency

## Why Inopay

Inopay is the [investment infrastructure for African capital markets](https://getinopay.com/fr/why-inopay) — BRVM (WAEMU), BVMAC (CEMAC), GSE (Ghana). Mobile Money operators, banks and licensed SGIs embed the regional exchanges into their apps via this SDK.

- Use case **Mobile Money operators** → see [Pour opérateurs MoMo](https://getinopay.com/fr/momo)
- Use case **Banks** → see [Pour banques](https://getinopay.com/fr/banks)
- Use case **SGI** → see [Pour SGI](https://getinopay.com/fr/sgi)
- White-label deployment → see [White-label](https://getinopay.com/fr/white-label)

## Regulatory framework

Inopay is a technical intermediation provider. Orders are executed exclusively by [AMF-UMOA-licensed SGIs](https://getinopay.com/fr/legal/regulatory-references). The KYC framework aligns with BCEAO Instruction No. 003-03-2025.

- [Compliance & doctrine (AMF-UMOA, COSUMAF, SEC Ghana)](https://getinopay.com/fr/compliance)
- [Public regulatory references](https://getinopay.com/fr/legal/regulatory-references)
- [Trust center & data residency](https://getinopay.com/fr/trust)
- [Contractual SLA](https://getinopay.com/fr/sla)
- [Public audit chain](https://getinopay.com/fr/audit)

## Other Inopay SDKs

The Inopay SDK family — same API surface, five native platforms:

- [`@inopay/web`](https://github.com/fofanay/inopay-web) — TypeScript / Web
- [`InopaySDK`](https://github.com/fofanay/inopay-ios) — Swift / iOS / macOS
- [`inopay-android`](https://github.com/fofanay/inopay-android) — Kotlin / Android / JVM
- [`inopay`](https://github.com/fofanay/inopay-python) — Python (sync, requests-based)
- [`inopay-java`](https://github.com/fofanay/inopay-java) — Java (sync, java.net.http + Jackson)

## Documentation & support

- [Developer portal](https://getinopay.com/fr/developers) — API, webhooks, sandbox
- [API reference (OpenAPI 3.1)](https://api.getinopay.com/v1/openapi.json)
- [Sandbox console](https://getinopay.com/fr/developers/sandbox) — public demo key + 7 endpoints
- [Portable KYC spec](https://getinopay.com/fr/developers/kyc) — Ed25519, offline-verifiable
- [Webhooks reference](https://getinopay.com/fr/developers/webhooks)
- [Changelog](https://getinopay.com/fr/developers/changelog)
- [Press kit](https://getinopay.com/fr/press-kit) — logo, boilerplates, fact sheet

Need integration help? Email <partner@getinopay.com>.

## License

MIT — © Inopay
