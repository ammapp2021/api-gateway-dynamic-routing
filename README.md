# Complete API Gateway Dynamic Routing - Class-by-Class Documentation

## Overview

This is a **Spring Cloud Gateway** application that implements **dynamic routing** where routes are stored in a MySQL database and loaded at runtime. The gateway uses **Spring WebFlux** (reactive programming) and includes JWT authentication, rate limiting, and circuit breaker support.

**Repository:** https://github.com/ammapp2021/api-gateway-dynamic-routing

---

## Architecture Flow: Request Processing Pipeline

```
Client Request
    ↓
[Port 8080] Spring Cloud Gateway
    ↓
1. CacheRequestBodyFilter (Order: -5) - Cache POST body
    ↓
2. JwtAuthenticationFilter (Order: -2) - Validate JWT token
    ↓
3. RateLimitingFilter (Order: 0) - Check rate limits
    ↓
4. Route Matching - Find matching route from database
    ↓
5. Route Filters - Apply route-specific filters
    ↓
6. HTTP Client (WebFlux HttpClient) - Forward to backend service
    ↓
Backend Service Response
    ↓
Response back to Client
```

---

## Class-by-Class Detailed Explanation

### 1. **GateewayApplication.java** (Main Application Class)

**Location:** `com.example.gateeway.GateewayApplication`

**Purpose:** 
- Main entry point of the Spring Boot application
- Bootstraps the entire Spring Cloud Gateway application

**What it does:**
- Annotated with `@SpringBootApplication` which enables auto-configuration
- `@EnableDiscoveryClient` is commented out (service discovery not used)
- Starts the Spring application context and initializes all beans

**Key Points:**
- Application runs on port 8080 (configured in `application.yaml`)
- Uses Spring Cloud Gateway with WebFlux (reactive stack)

---

### 2. **RouteEntity.java** (Database Model)

**Location:** `com.example.gateeway.model.RouteEntity`

**Purpose:** 
- JPA Entity that represents a route stored in the database
- Maps to `gateway_routes` table in MySQL

**Fields:**
- `id` (String): Unique route identifier (Primary Key)
- `uri` (String): Target backend service URI (e.g., `http://localhost:8080`)
- `predicates` (String, TEXT): JSON string containing route matching conditions
- `filters` (String, TEXT): JSON string containing route filters to apply
- `enabled` (boolean): Whether the route is active or not

**Example Database Record:**
```json
{
  "id": "user-service-route",
  "uri": "http://localhost:8080",
  "predicates": "[{\"name\":\"Path\",\"args\":{\"pattern\":\"/api/users/**\"}}]",
  "filters": "[{\"name\":\"RewritePath\",\"args\":{\"regex\":\"/api/users/(?<path>.*)\",\"replacement\":\"/users/${path}\"}}]",
  "enabled": true
}
```

**What it does:**
- Stores route configuration in database
- Allows routes to be managed without code changes
- Routes can be enabled/disabled by setting `enabled` flag

---

### 3. **RouteRepository.java** (Data Access Layer)

**Location:** `com.example.gateeway.repository.RouteRepository`

**Purpose:**
- Spring Data JPA repository interface for database operations on routes

**Methods:**
- `findByEnabledTrue()`: Custom query method that returns only enabled routes
- Inherits standard JPA methods: `save()`, `deleteById()`, `findAll()`, etc.

**What it does:**
- Provides database access layer for route entities
- Used by `RouteService` to fetch routes from database
- Filters out disabled routes automatically

---

### 4. **RouteService.java** (Service Interface)

**Location:** `com.example.gateeway.service.RouteService`

**Purpose:**
- Interface defining contract for route management operations

**Methods:**
- `Flux<RouteDefinition> getAllRoutes()`: Get all routes (reactive)
- `Mono<Void> saveRoute(Mono<RouteDefinition> routeMono)`: Save a new route
- `Mono<Void> deleteRoute(Mono<String> routeId)`: Delete a route by ID

**What it does:**
- Defines the service contract for route management
- Uses reactive types (`Flux`, `Mono`) for non-blocking operations

---

### 5. **RouteServiceImpl.java** (Service Implementation)

**Location:** `com.example.gateeway.service.RouteServiceImpl`

**Purpose:**
- Implements business logic for route management
- Converts between database entities and Spring Cloud Gateway `RouteDefinition` objects

**Key Components:**

**Constructor:**
- Injects `RouteRepository` for database access
- Injects `ApplicationEventPublisher` to publish route refresh events
- Creates `ObjectMapper` for JSON serialization/deserialization

**`getAllRoutes()` Method:**
1. Fetches all enabled routes from database using `repository.findByEnabledTrue()`
2. Converts each `RouteEntity` to `RouteDefinition` using `convert()` method
3. Returns `Flux<RouteDefinition>` (reactive stream)

**`convert()` Method (Private):**
1. Creates new `RouteDefinition` object
2. Sets route ID and URI from entity
3. Parses JSON strings to:
   - `List<PredicateDefinition>` for route matching conditions
   - `List<FilterDefinition>` for route filters
4. Returns fully configured `RouteDefinition`

**`saveRoute()` Method:**
1. Receives `RouteDefinition` as reactive `Mono`
2. Converts `RouteDefinition` to `RouteEntity`:
   - Serializes predicates and filters to JSON strings
   - Sets `enabled = true`
3. Saves entity to database
4. **Publishes `RefreshRoutesEvent`** - This triggers Spring Cloud Gateway to reload routes from database
5. Returns empty `Mono<Void>`

**`deleteRoute()` Method:**
1. Deletes route from database by ID
2. **Publishes `RefreshRoutesEvent`** to refresh routes
3. Returns empty `Mono<Void>`

**What it does:**
- **Core of dynamic routing**: This class enables routes to be added/updated/deleted at runtime
- When `RefreshRoutesEvent` is published, Spring Cloud Gateway calls `DynamicRouteRepository.getRouteDefinitions()` again
- Routes are reloaded from database without application restart

---

### 6. **DynamicRouteRepository.java** (Gateway Route Provider)

**Location:** `com.example.gateeway.config.DynamicRouteRepository`

**Purpose:**
- **CRITICAL CLASS**: Implements Spring Cloud Gateway's `RouteDefinitionRepository` interface
- This is what makes routing **truly dynamic**

**What `RouteDefinitionRepository` does:**
- Spring Cloud Gateway uses this interface to get route definitions
- By default, Gateway uses in-memory routes from configuration files
- By implementing this interface, we override that behavior to use database

**Methods:**

**`getRouteDefinitions()`:**
- Called by Spring Cloud Gateway when:
  - Application starts
  - `RefreshRoutesEvent` is published
  - Routes need to be reloaded
- Delegates to `routeService.getAllRoutes()`
- Returns `Flux<RouteDefinition>` - reactive stream of routes

**`save(Mono<RouteDefinition> route)`:**
- Called when routes are added via Gateway's actuator endpoints
- Delegates to `routeService.saveRoute()`

**`delete(Mono<String> routeId)`:**
- Called when routes are deleted via Gateway's actuator endpoints
- Delegates to `routeService.deleteRoute()`

**What it does:**
- **This is the bridge between Spring Cloud Gateway and the database**
- When Gateway needs routes, it calls this class
- This class fetches routes from database via `RouteService`
- Routes are loaded dynamically - no code changes needed

**Dynamic Routing Confirmation:**
✅ **YES, dynamic routing IS implemented and working!**
- Routes are stored in database
- `DynamicRouteRepository` provides routes to Gateway
- `RefreshRoutesEvent` triggers route reload
- Routes can be added/updated/deleted at runtime

---

### 7. **CacheRequestBodyFilter.java** (Request Body Caching)

**Location:** `com.example.gateeway.config.CacheRequestBodyFilter`

**Purpose:**
- Caches request body for POST requests so it can be read multiple times
- **Critical Issue**: Request body is a stream that can only be read once, so we cache it

**Why Streams Can Only Be Read Once:**
- In reactive programming (Spring WebFlux), request body is a `Flux<DataBuffer>` (reactive stream)
- **Streams are consumed, not copied**: When you read from a stream, the data is consumed and removed from the stream
- **One-time consumption**: Once `getBody()` is called and the stream is read, it cannot be read again
- **Problem**: If multiple filters/predicates need to read the body:
  - First filter reads body → Stream is consumed 
  - Second filter tries to read body → Stream is empty  (already consumed)
  - Result: Second filter gets empty/null body

**Solution - Caching:**
- Read the entire stream once at the beginning
- Store it in memory (as byte array and string)
- Create a new stream from cached data that can be read multiple times
- This allows unlimited reads of the same body content

**Execution Order:** `-5` (runs very early in filter chain - before any other filter that might need body)

**What it does:**

1. **Checks if request is POST:**
   - Only processes POST requests (body caching needed)

2. **Reads request body:**
   - Uses `DataBufferUtils.join()` to read entire body stream
   - Converts to byte array

3. **Caches body:**
   - Stores body as string in exchange attributes: `exchange.getAttributes().put("cachedBody", bodyString)`
   - This allows other filters/predicates to read body multiple times

4. **Recreates request:**
   - Creates `ServerHttpRequestDecorator` that wraps original request
   - Overrides `getBody()` to return cached body
   - This ensures downstream filters see the body

**Why it's needed:**
- Custom predicates (like `BodyValueRoutePredicateFactory`) need to read request body
- Body stream can only be consumed once
- By caching, multiple components can access body content

**Example - Without Caching (Problem):**
```
POST Request: {"value": 1}
    ↓
Filter 1: exchange.getRequest().getBody() → Reads: {"value": 1} 
    ↓
Filter 2: exchange.getRequest().getBody() → Reads: (empty/null) 
    ↓
Predicate: exchange.getRequest().getBody() → Reads: (empty/null) 
```
**Result**: Only first filter can read body, others get nothing!

**Example - With Caching (Solution):**
```
POST Request: {"value": 1}
    ↓
CacheRequestBodyFilter:
  - Reads body: {"value": 1}
  - Caches in memory: cachedBody = "{\"value\": 1}"
  - Creates new stream from cache
    ↓
Filter 1: exchange.getRequest().getBody() → Reads: {"value": 1} 
    ↓
Filter 2: exchange.getRequest().getBody() → Reads: {"value": 1} 
    ↓
Predicate: exchange.getAttribute("cachedBody") → Gets: "{\"value\": 1}" 
```
**Result**: All filters/predicates can access body content!

**Flow:**
```
POST Request with body: {"value": 1}
    ↓
CacheRequestBodyFilter (Order: -5):
  - Reads entire body stream
  - Converts to byte array
  - Stores as string in exchange attributes: "cachedBody"
  - Creates ServerHttpRequestDecorator with cached body
    ↓
JwtAuthenticationFilter (Order: -2):
  - Can read body if needed (from cached stream)
    ↓
RateLimitingFilter (Order: 0):
  - Can read body if needed (from cached stream)
    ↓
Route Predicates:
  - BodyValueRoutePredicateFactory reads from "cachedBody" attribute
    ↓
HTTP Client forwards request with body to backend
```

---

### 8. **BodyValueRoutePredicateFactory.java** (Custom Route Predicate)

**Location:** `com.example.gateeway.predicate.BodyValueRoutePredicateFactory`

**Purpose:**
- Custom predicate factory for route matching based on request body content
- Extends `AbstractRoutePredicateFactory` to create custom route matching logic

**What it does:**

1. **Config Class:**
   - Inner class `Config` holds predicate configuration
   - `value` field: The value to match in request body

2. **`apply(Config config)` Method:**
   - Creates a `Predicate<ServerWebExchange>` function
   - Checks if `cachedBody` attribute exists (set by `CacheRequestBodyFilter`)
   - Searches for JSON pattern: `"value":"<config.value>"`
   - Returns `true` if pattern found, `false` otherwise

**Example Usage:**
If request body is: `{"value": "1"}`
And predicate config is: `value = "1"`
Then route matches 

**Note:** This class is defined but may not be actively used since routes are stored as JSON in database. However, it demonstrates how custom predicates can be created.

---

### 9. **JwtUtil.java** (JWT Utility)

**Location:** `com.example.gateeway.security.JwtUtil`

**Purpose:**
- Utility class for JWT token generation and validation

**Key Components:**

**Secret Key:**
- Hardcoded: `"VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBmb3IgSFMyNTY="`
- Base64 encoded string
-  **Security Issue**: Should be in environment variable for production

**`generateToken(String username)` Method:**
1. Creates JWT using `Jwts.builder()`
2. Sets subject (username)
3. Sets issued at time (current time)
4. Sets expiration (1 hour from now: `1000 * 60 * 60` milliseconds)
5. Signs with HS256 algorithm
6. Returns compact token string

**`validateToken(String token)` Method:**
1. Parses token using `Jwts.parserBuilder()`
2. Validates signature using secret key
3. Checks expiration automatically
4. Returns `true` if valid, `false` if invalid/expired

**`extractUsername(String token)` Method:**
1. Parses token
2. Extracts subject (username) from claims
3. Returns username

**What it does:**
- Generates JWT tokens for authenticated users
- Validates tokens in incoming requests
- Extracts user information from tokens

---

### 10. **JwtAuthenticationFilter.java** (JWT Authentication Filter)

**Location:** `com.example.gateeway.security.JwtAuthenticationFilter`

**Purpose:**
- Global filter that validates JWT tokens on all requests
- Protects API endpoints (except `/auth/**`)

**Execution Order:** `-2` (runs after `CacheRequestBodyFilter`)

**What it does:**

1. **Checks request path:**
   - If path starts with `/auth`, allows request without authentication
   - This permits login and test endpoints

2. **Extracts Authorization header:**
   - Looks for `Authorization: Bearer <token>` header
   - If missing or doesn't start with "Bearer ", returns `401 Unauthorized`

3. **Validates token:**
   - Extracts token (removes "Bearer " prefix)
   - Calls `jwtUtil.validateToken(token)`
   - If invalid, returns `401 Unauthorized`

4. **Forwards request:**
   - If token is valid, adds token to request headers
   - Passes request to next filter in chain

**Flow:**
```
Request arrives
    ↓
Is path /auth/**? → YES → Allow (no auth needed)
    ↓ NO
Extract Authorization header
    ↓
Header missing/invalid? → YES → Return 401
    ↓ NO
Validate JWT token
    ↓
Token invalid? → YES → Return 401
    ↓ NO
Add token to request headers
    ↓
Continue to next filter
```

**What it does:**
- Enforces authentication on all non-auth endpoints
- Validates JWT tokens
- Prevents unauthorized access to backend services

---

### 11. **RateLimitingFilter.java** (Rate Limiting Filter)

**Location:** `com.example.gateeway.filter.RateLimitingFilter`

**Purpose:**
- Global filter that limits number of requests per IP address
- Prevents abuse and DDoS attacks

**Execution Order:** `0` (runs after authentication)

**What it does:**

1. **Extracts client IP:**
   - First checks `X-Forwarded-For` header (for proxy/load balancer scenarios)
   - If not present, uses `exchange.getRequest().getRemoteAddress()`
   - Handles comma-separated IPs (takes first one)

2. **Gets or creates bucket:**
   - Uses `ConcurrentHashMap` to store buckets per IP
   - Each IP gets its own `Bucket` instance
   - `createBucket()` creates bucket with:
     - **Limit**: 1 request
     - **Refill**: 1 token every 10 seconds
     - **Algorithm**: Greedy refill (immediate refill)

3. **Checks rate limit:**
   - Calls `bucket.tryConsume(1)` to consume 1 token
   - If successful (token available), allows request
   - If failed (no tokens), returns `429 Too Many Requests`

**Rate Limit Configuration:**
- **1 request per 10 seconds per IP address**
- Uses Bucket4j library (token bucket algorithm)

**Flow:**
```
Request arrives
    ↓
Extract client IP address
    ↓
Get bucket for this IP (create if doesn't exist)
    ↓
Try to consume 1 token
    ↓
Token available? → NO → Return 429 Too Many Requests
    ↓ YES
Allow request to continue
```

**What it does:**
- Protects API from abuse
- Limits requests per IP to prevent overload
- Returns HTTP 429 when limit exceeded

---

### 12. **SecurityConfig.java** (Security Configuration)

**Location:** `com.example.gateeway.config.SecurityConfig`

**Purpose:**
- Configures Spring Security for WebFlux (reactive security)

**What it does:**

1. **Disables CSRF:**
   - `csrf().disable()` - Not needed for API Gateway (stateless)

2. **Configures authorization:**
   - `/auth/**` - Permit all (no authentication required)
   - `/actuator/**` - Permit all (monitoring endpoints)
   - `.anyExchange().permitAll()` - **All other routes are permitted**
     - ⚠️ **Note**: This means security is actually handled by `JwtAuthenticationFilter`, not Spring Security
     - Spring Security is configured but not enforcing authentication
     - JWT filter is doing the actual authentication

3. **Disables HTTP Basic and Form Login:**
   - Not needed for API Gateway (JWT-based)

**Important Note:**
- Spring Security is configured but not actively blocking requests
- `JwtAuthenticationFilter` is doing the actual authentication work
- This configuration allows all requests, and JWT filter enforces auth

---

### 13. **AuthController.java** (Authentication Controller)

**Location:** `com.example.gateeway.controller.AuthController`

**Purpose:**
- REST controller for authentication endpoints

**Endpoints:**

**`POST /auth/login`**
- Parameters: `username`, `password` (form data or query params)
- **Hardcoded credentials**: `admin` / `admin123`
- If credentials match:
  - Generates JWT token using `jwtUtil.generateToken(username)`
  - Returns token in response body
  - Also sets `Authorization: Bearer <token>` header
- If credentials don't match:
  - Returns `401 Unauthorized` with message "Invalid credentials"

**`GET /auth/test`**
- Simple test endpoint
- Returns `Mono<String>` with message "JWT works!"
- Used for testing authentication flow

**What it does:**
- Provides login endpoint for clients
- Issues JWT tokens to authenticated users
- ⚠️ **Security Issue**: Hardcoded credentials should be replaced with database lookup

---

### 14. **FallbackController.java** (Fallback Controller)

**Location:** `com.example.gateeway.controller.FallbackController`

**Purpose:**
- Provides fallback response when services are unavailable

**Endpoint:**
- `GET /fallback` - Returns message: "Service temporarily unavailable. Please try later!"

**What it does:**
- Can be used as fallback URI in circuit breaker configuration
- Provides user-friendly error message when backend services fail

---

### 15. **GatewayRoutesConfig.java** (Static Routes - COMMENTED)

**Location:** `com.example.gateeway.config.GatewayRoutesConfig`

**Purpose:**
- **This entire class is commented out!**
- Shows example of static route configuration (not used)

**What it would do (if uncommented):**
- Defines routes using `RouteLocatorBuilder`
- Example routes:
  - Route based on path `/route` and POST method
  - Routes based on request body value (1, 2, or 3)
  - Different target URIs based on body content

**Why it's commented:**
- **Dynamic routing is being used instead**
- Routes come from database via `DynamicRouteRepository`
- Static configuration is not needed

**Key Point:**
- This confirms that **dynamic routing is the active approach**
- Static routes are disabled in favor of database-driven routes

---

## Complete Request Flow: Step-by-Step

### Example: Client requests `GET /api/users/123`

**Step 1: Request Arrives**
```
Client → http://localhost:8080/api/users/123
         Headers: Authorization: Bearer eyJhbGci...
```

**Step 2: CacheRequestBodyFilter (Order: -5)**
- Checks if POST request → NO (GET request)
- Skips body caching
- Passes request to next filter

**Step 3: JwtAuthenticationFilter (Order: -2)**
- Checks path: `/api/users/123` → Not `/auth/**`
- Extracts `Authorization` header → Found: `Bearer eyJhbGci...`
- Extracts token: `eyJhbGci...`
- Validates token using `JwtUtil.validateToken()`
- Token valid? → YES
- Adds token to request headers
- Passes to next filter

**Step 4: RateLimitingFilter (Order: 0)**
- Extracts client IP: `192.168.1.100`
- Gets bucket for IP (creates if needed)
- Tries to consume 1 token
- Token available? → YES (first request in 10 seconds)
- Allows request
- Passes to next filter

**Step 5: Spring Cloud Gateway Route Matching**
- Gateway calls `DynamicRouteRepository.getRouteDefinitions()`
- `DynamicRouteRepository` calls `RouteService.getAllRoutes()`
- `RouteServiceImpl` fetches routes from database:
  ```sql
  SELECT * FROM gateway_routes WHERE enabled = true
  ```
- Converts `RouteEntity` to `RouteDefinition`:
  - Parses predicates JSON: `[{"name":"Path","args":{"pattern":"/api/users/**"}}]`
  - Parses filters JSON: `[{"name":"RewritePath",...}]`
- Gateway matches request path `/api/users/123` against route predicate
- Route matches! → Route ID: `user-service-route`
- Target URI: `http://localhost:8081`

**Step 6: Apply Route Filters**
- Applies `RewritePath` filter:
  - Original: `/api/users/123`
  - Rewritten: `/users/123` (based on filter configuration)

**Step 7: HTTP Client (WebFlux HttpClient)**
- Spring Cloud Gateway uses **Reactor Netty HttpClient** (configured in `application.yaml`)
- Configuration:
  ```yaml
  httpclient:
    ssl:
      use-insecure-trust-manager: true  # Allow self-signed certificates
    connect-timeout: 5000               # 5 seconds
    response-timeout: 10000             # 10 seconds
  ```
- Creates HTTP request to backend:
  ```
  GET http://localhost:8080/users/123
  Headers: Authorization: Bearer eyJhbGci...
  ```
- Sends request using reactive HTTP client (non-blocking)
- Waits for response

**Step 8: Backend Service Response**
- Backend service processes request
- Returns response:
  ```json
  {
    "id": 123,
    "name": "John Doe",
    "email": "john@example.com"
  }
  ```

**Step 9: Response Back to Client**
- Gateway receives response from backend
- Passes response through filter chain (reverse order)
- Returns response to client:
  ```
  Status: 200 OK
  Body: {"id":123,"name":"John Doe","email":"john@example.com"}
  ```

---

## HTTP Client Usage

### How HTTP Client Works:

1. **Spring Cloud Gateway uses Reactor Netty HttpClient**
   - This is the default HTTP client for Spring WebFlux
   - Non-blocking, reactive HTTP client
   - Handles all outbound requests to backend services

2. **Configuration in application.yaml:**
   ```yaml
   spring:
     cloud:
       gateway:
         server:
           webflux:
             httpclient:
               ssl:
                 use-insecure-trust-manager: true  # For HTTPS with self-signed certs
               connect-timeout: 5000               # Connection timeout
               response-timeout: 10000            # Response timeout
   ```

3. **When HTTP Client is Used:**
   - When a route matches and needs to forward request to backend
   - Gateway creates HTTP request with:
     - Method (GET, POST, etc.)
     - URI (from route definition)
     - Headers (including forwarded Authorization header)
     - Body (if POST/PUT)
   - Sends request asynchronously (non-blocking)
   - Waits for response
   - Returns response to client

4. **Reactive Nature:**
   - Uses `Mono` and `Flux` for reactive streams
   - Non-blocking I/O
   - Can handle thousands of concurrent requests

---

## Dynamic Routing: Confirmed Working

### Evidence that Dynamic Routing is Implemented:

1. **DynamicRouteRepository implements RouteDefinitionRepository**
   - This interface is what Gateway uses to get routes
   - By implementing it, we override default static configuration

2. **Routes stored in database (RouteEntity)**
   - Routes are persisted in MySQL
   - Can be added/updated/deleted without code changes

3. **RouteService fetches from database**
   - `getAllRoutes()` queries database for enabled routes
   - Converts database entities to Gateway RouteDefinitions

4. **RefreshRoutesEvent triggers reload**
   - When routes are saved/deleted, event is published
   - Gateway automatically reloads routes from database

5. **GatewayRoutesConfig is commented out**
   - Static route configuration is disabled
   - Confirms dynamic routing is the active approach

### How to Add a Route Dynamically:

**Option 1: Via Database**
```sql
INSERT INTO gateway_routes (id, uri, predicates, filters, enabled) 
VALUES (
  'product-service',
  'http://localhost:8080',
  '[{"name":"Path","args":{"pattern":"/api/products/**"}}]',
  '[{"name":"RewritePath","args":{"regex":"/api/products/(?<path>.*)","replacement":"/products/${path}"}}]',
  true
);
```

Then trigger refresh:
```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

**Option 2: Via Actuator Endpoint**
```bash
curl -X POST http://localhost:8080/actuator/gateway/routes/product-service \
  -H "Content-Type: application/json" \
  -d '{
    "id": "product-service",
    "uri": "http://localhost:8080",
    "predicates": [{"name": "Path", "args": {"pattern": "/api/products/**"}}],
    "filters": [{"name": "RewritePath", "args": {"regex": "/api/products/(?<path>.*)", "replacement": "/products/${path}"}}]
  }'
```

---

## Summary: What Each Class Does

| Class | Responsibility | Key Function |
|-------|---------------|--------------|
| **GateewayApplication** | Bootstrap | Starts Spring Boot application |
| **RouteEntity** | Data Model | Represents route in database |
| **RouteRepository** | Data Access | Database queries for routes |
| **RouteService** | Business Logic | Route management operations |
| **RouteServiceImpl** | Implementation | Converts entities ↔ RouteDefinitions, publishes refresh events |
| **DynamicRouteRepository** | **Core Dynamic Routing** | Provides routes to Gateway from database |
| **CacheRequestBodyFilter** | Request Processing | Caches POST request body |
| **BodyValueRoutePredicateFactory** | Route Matching | Custom predicate for body-based routing |
| **JwtUtil** | Security | JWT token generation/validation |
| **JwtAuthenticationFilter** | Security | Validates JWT on all requests |
| **RateLimitingFilter** | Protection | Limits requests per IP |
| **SecurityConfig** | Security Config | Spring Security configuration (permissive) |
| **AuthController** | Authentication | Login endpoint, issues JWT tokens |
| **FallbackController** | Error Handling | Fallback response for failures |

---

## Key Takeaways

1. **Dynamic Routing IS Implemented**
   - Routes come from database via `DynamicRouteRepository`
   - Routes can be added/updated/deleted at runtime
   - No code changes needed for new routes

2. **HTTP Client IS Used**
   - Spring Cloud Gateway uses Reactor Netty HttpClient
   - Configured in `application.yaml`
   - Handles all outbound requests to backend services
   - Non-blocking, reactive HTTP client

3. **Request Flow:**
   - Request → Filters (Cache, Auth, Rate Limit) → Route Matching → HTTP Client → Backend → Response

4. **Security:**
   - JWT authentication on all routes (except `/auth/**`)
   - Rate limiting per IP address
   - Token validation before forwarding

5. **Database-Driven:**
   - All routes stored in MySQL
   - Routes loaded at startup and on refresh events
   - Dynamic updates without restart

---

**Documentation Created:** Complete class-by-class analysis  
**Repository:** https://github.com/ammapp2021/api-gateway-dynamic-routing
