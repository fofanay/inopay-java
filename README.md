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

## License

MIT — © Inopay
