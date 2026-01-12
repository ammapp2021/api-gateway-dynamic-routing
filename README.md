
## 1. Authorization

Use the following API to log in:

POST http://localhost:8080/auth/login?username=admin&password=admin123

The response will include an **Authorization token**. Use this token for all subsequent requests.


## 2. Redis Setup (Upstash)

1. Go to [Upstash Console](https://console.upstash.com) and create a Redis database and account.
2. Configure your application to use the Redis credentials.

## 3. Create a Route

After login, insert a route using:

POST http://localhost:8080/admin/routes

**Request Body:**
```json
{
  "id": "route1",
  "uri": "https://jsonplaceholder.typicode.com",
  "predicates": [
    { "name": "Path", "args": { "pattern": "/posts/**" } },
    { "name": "BodyValue", "args": { "value": "1" } }
  ],
  "filters": [
    {
      "name": "CircuitBreaker",
      "args": {
        "name": "myCircuitBreaker",
        "fallbackUri": "forward:/fallback"
      }
    }
  ]
}
4. Delete a Route
To delete a route, use:

bash
Copy code
DELETE http://localhost:8080/admin/routes/{route-id}
5. Using the Authorization Token
Include the token in the Authorization header for all protected APIs:

Authorization: Bearer <token>
6. Posts API
Use the following API to send posts:

POST http://localhost:8080/posts
Headers:

Content-Type: application/json

Authorization: Bearer <token>

Request Body:
{
  "value": 1,
  "title": "Shahneela",
  "body": "shahneela@gmail.com",
  "userId": 1
}
7. Refresh Gateway Routes
To refresh routes in Spring Cloud Gateway:

POST http://localhost:8080/actuator/gateway/refresh
8. View Gateway Routes
Check active routes using:

GET http://localhost:8080/actuator/gateway/routes
Or open the same URL in your browser.
