# Microservices - Spring Boot GYGY 5.0
## Config Server + CDC (Debezium) + Kafka Ödevi

---

## 📐 Mimari

```
                    ┌─────────────────┐
                    │  config-server  │  :8888
                    │  (GitHub repo)  │
                    └────────┬────────┘
          ┌─────────┬────────┼────────┬─────────┐
          ▼         ▼        ▼        ▼         ▼
    eureka-server  gateway  order   product   user    cart
       :8761       :8086   :8085    :8082    :8081   :8084
                              │       ▲        ▲
                         OutboxMsg    │        │
                              │    Kafka    Kafka
                              │  (order-created-topic)
                              │              │
                           Debezium  ←── product-db WAL
                         (test-topic → user-service)
```

### CDC Akışı (Debezium)
```
product-service → public.outbox (INSERT) → PostgreSQL WAL
  → Debezium Connect → test-topic → user-service (TestEventConsumer)
```

### Transactional Outbox Akışı
```
order-service → customer_orders + outbox_messages (tek TX)
  → OutboxPublisher (@Scheduled) → order-created-topic
  → product-service (OrderCreatedEventConsumer) → stok düş
```

---

## 🛠️ Teknolojiler

| Teknoloji | Versiyon |
|-----------|----------|
| Spring Boot | 4.0.6 |
| Spring Cloud | 2025.1.1 |
| Java | 21 |
| Kafka | apache/kafka:4.2.0 (KRaft - Zookeeper yok!) |
| Debezium | quay.io/debezium/connect:3.4 |
| PostgreSQL | 17 |

---

## 🚀 Çalıştırma

### 1. Config Server application.yaml güncelle
`microservices/configs/config-server/src/main/resources/application.yaml` dosyasında:
```yaml
uri: https://github.com/NG720/Microservices-Spring-GYGY-5.0.git
```
kısmını kendi GitHub repo URL'inle değiştir.

### 2. Infrastructure başlat
```bash
cd docker
docker compose up -d
```

### 3. Servisleri sırasıyla başlat
```bash
# 1. Config Server
cd microservices/configs/config-server && ./mvnw spring-boot:run

# 2. Eureka
cd microservices/eureka-server && ./mvnw spring-boot:run

# 3. Gateway
cd microservices/gateway-server && ./mvnw spring-boot:run

# 4. Product Service
cd microservices/product-service && ./mvnw spring-boot:run

# 5. Order Service
cd microservices/order-service && ./mvnw spring-boot:run

# 6. User Service
cd microservices/user-service && ./mvnw spring-boot:run

# 7. Cart Service (opsiyonel)
cd microservices/cart-service && ./mvnw spring-boot:run
```

---

## 🧪 Test

### Ürün oluştur
```bash
POST http://localhost:8086/api/products
Content-Type: application/json

{"name": "Laptop", "stockQuantity": 50}
```

### Sipariş oluştur (OutboxPublisher → Kafka → product-service stok düşer)
```bash
POST http://localhost:8086/api/orders
Content-Type: application/json
Idempotency-Key: order-001

{"products": [{"productId": "<id>", "quantity": 2}]}
```

### CDC test (Debezium → Kafka → user-service)
```bash
POST http://localhost:8082/api/products?message=cdc-test
```

---

## 📊 Monitoring

| Servis | URL |
|--------|-----|
| Eureka Dashboard | http://localhost:8761 |
| Kafka UI | http://localhost:8080 |
| Debezium Connect | http://localhost:8083/connectors |
| PgAdmin | http://localhost:5050 (admin@admin.com / admin) |

---

## 📁 Proje Yapısı

```
microservices/
├── configs/
│   ├── config-server/          ← Config Server uygulaması
│   ├── application.yaml        ← Tüm servislere ortak config
│   ├── order-service/          ← order-service config (dev/prod/test)
│   ├── product-service/        ← product-service config
│   ├── user-service/           ← user-service config
│   ├── cart-service/
│   ├── eureka-server/
│   └── gateway-server/
├── eureka-server/
├── gateway-server/
├── order-service/              ← Transactional Outbox Pattern
├── product-service/            ← Debezium CDC + Kafka consumer
├── user-service/               ← FeignClient + Idempotency
└── cart-service/
docker/
├── docker-compose.yml          ← Kafka(KRaft) + PG + Debezium + PgAdmin
└── debezium/
    └── product-outbox-connector.json
docs/
├── order-product-event-flow.md
├── product-debezium-outbox.md
└── ...ders notları...
```
