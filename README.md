# TradeCore — Order Matching Engine

A stock exchange order matching engine built in Java, with a REST API, PostgreSQL 
persistence, and Docker packaging.

## What it does

Accepts buy and sell orders for any stock symbol and matches them using price-time 
priority: the best price trades first, and among equal prices, the earliest order 
trades first. Supports limit orders, market orders, partial fills, and cancellations.

## How it works

Request → REST API (Spring Boot) → Service layer → Matching Engine → PostgreSQL (trades)

The matching engine itself has no dependency on Spring. It is plain Java, built and 
tested on its own, then wrapped with a REST API.

- Each stock's order book uses a `TreeMap` (sorted by price) of `ArrayDeque`s 
  (FIFO at each price level) for O(log n) price updates, with a `HashMap` for 
  O(1) order cancellation.
- Thread-safe: each order book is independently synchronized, so orders for 
  different stocks process in parallel. Verified with a concurrency test placing 
  80,000 orders across 8 threads.
- Benchmarked at [your number] orders/second on a single core.

## Tech stack

Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL 16, Docker, JUnit 5, Maven

## Running it

docker compose up --build


The API is then available at `http://localhost:8080`.

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Place a limit or market order |
| DELETE | `/api/orders/{symbol}/{orderId}` | Cancel an order |
| GET | `/api/orderbook/{symbol}` | View the current order book |
| GET | `/api/trades?symbol=X` | View recent trades |

Example:  curl -X POST http://localhost:8080/api/orders
-H “Content-Type: application/json”
-d ‘{“symbol”:”RELIANCE”,”side”:”BUY”,”type”:”LIMIT”,”price”:50200,”quantity”:30}’


## Testing
./mvnw test
Covers order matching rules, partial fills, price-time priority, validation, and 
concurrent order submission.

## What I'd add next

- Persist open orders so the order book survives a restart
- WebSocket feed for live order book updates
- Market depth and trade history charts