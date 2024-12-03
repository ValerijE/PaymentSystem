package com.evv.util;

public class SwaggerConstants {

    // language=JSON
    public static final String SCHEMA_EXAMPLE_401 = """
            {
              "type": "about:blank",
              "title": "Unauthorized",
              "status": 401,
              "detail": "Anonymous client ip = 0:0:0:0:0:0:0:1 attempted to access endpoint POST /api/v1/purchases without authentication",
              "instance": "/api/v1/purchases",
              "timestamp": "2024-09-27T13:38:36.849884500Z"
            }
            """;
    // language=JSON
    public static final String SCHEMA_EXAMPLE_403 = """
            {
              "type": "about:blank",
              "title": "Access denied",
              "status": 403,
              "detail": "Client id = 1 attempted to access endpoint GET /api/v1/purchases without appropriate permissions",
              "instance": "/api/v1/purchases",
              "timestamp": "2024-09-23T18:16:17.181610800Z"
            }
            """;
    // language=JSON
    public static final String SCHEMA_EXAMPLE_500 = """
            {
              "type": "about:blank",
              "title": "Internal Server Error",
              "status": 500,
              "detail": "Failed to convert 'pageable' with value: 'page=0&size=14&sort='",
              "instance": "/api/v1/purchases"
            }
            """;
    // language=JSON
    public static final String SCHEMA_EXAMPLE_PURCHASE_CREDIT_CARD = """
            {
              "id": 10,
              "client": {
                "id": 1,
                "email": "client1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1984-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 6,
                  "name": "Десктоп ПК-5000",
                  "cost": 50000
                }
              ],
              "payment": {
                "id": 10,
                "amount": 50000,
                "creditCard": {
                  "id": 1,
                  "expirationDate": "2024-12-12",
                  "balance": -6500.23,
                  "creditLimit": -150000,
                  "status": "ACTIVE",
                  "clientId": 1
                }
              }
            }
            """;
    // language=JSON
    public static final String SCHEMA_EXAMPLE_PURCHASE_ACCOUNT = """
            {
              "id": 11,
              "client": {
                "id": 1,
                "email": "client1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1984-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 6,
                  "name": "Десктоп ПК-5000",
                  "cost": 50000
                }
              ],
              "payment": {
                "id": 11,
                "amount": 50000,
                "account": {
                  "id": 1,
                  "balance": 4500.1,
                  "clientId": 1
                }
              }
            }
            """;
}
