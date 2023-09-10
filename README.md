# Anti-fraud-system

This project serves as a simplified illustration of anti-fraud systems within the financial sector. It incorporates a collection of REST endpoints responsible for user interactions and an internal transaction validation mechanism driven by a series of heuristic rules.

## 💻 Skills
- Java
- Spring Boot
- Spring Security
- Spring JPA
- H2 Database

## 🔑 Key Features

### Authentication and Authorization

The system employs a role model that governs user access to various endpoints:

|                                 | Anonymous | MERCHANT | ADMINISTRATOR | SUPPORT |
| ------------------------------- | --------- | -------- | ------------- | ------- |
| POST /api/auth/user             | +         | +        | +             | +       |
| DELETE /api/auth/user           | -         | -        | +             | -       |
| GET /api/auth/list              | -         | -        | +             | +       |
| POST /api/antifraud/transaction | -         | +        | -             | -       |
| /api/antifraud/suspicious-ip    | -         | -        | -             | +       |
| /api/antifraud/stolencard       | -         | -        | -             | +       |
| GET /api/antifraud/history      | -         | -        | -             | +       |
| PUT /api/antifraud/transaction  | -         | -        | -             | +       |

- `-` indicates unauthorized access, and `+` indicates authorized access.

### 🚀 Transaction Validation

The fraud detection process is built on three fundamental rules:

1. **Card Number and IP Address Blacklisting:**

   If the credit card number or IP address used in a transaction is found on the blacklist, the transaction is marked as `PROHIBITED`.

2. **IP Address and Region Correlation:**

   For transactions involving a card number, the system analyzes the transaction history for the past hour using the following criteria:

   - If there are transactions from more than 2 regions of the world (excluding the region of the current transaction) within the last hour, the transaction is classified as `PROHIBITED`.

   - Similarly, if there are transactions from more than 2 unique IP addresses (excluding the IP of the current transaction) within the last hour, the transaction is categorized as `PROHIBITED`.

   - However, if there are transactions from 2 regions of the world (other than the current region of the transaction) within the last hour, or if there are transactions from 2 unique IP addresses (other than the IP of the current transaction) within the last hour, the transaction is sent for `MANUAL_PROCESSING`.

   Regions are identified by the following codes:

   | Code | Description                      |
   | ---- | -------------------------------- |
   | EAP  | East Asia and Pacific            |
   | ECA  | Europe and Central Asia          |
   | HIC  | High-Income countries            |
   | LAC  | Latin America and the Caribbean  |
   | MENA | The Middle East and North Africa |
   | SA   | South Asia                       |
   | SSA  | Sub-Saharan Africa               |

3. **Transaction Amount:**

   The detection limit for transaction amounts is dynamically determined based on **feedback** received from users with the `SUPPORT` role. This adaptive mechanism allows the system to adjust its fraud detection algorithms in response to changing transaction environments, including factors like economic conditions, fraudster behavior, and transaction volume.

### 📣 Feedback

Feedback is conducted manually by a `SUPPORT` specialist for completed transactions. Based on the feedback results, the system adjusts the limits of fraud detection algorithms following specific rules. The table below outlines the logic of the feedback system:

| Transaction Feedback → Transaction Validity ↓ | ALLOWED                           | MANUAL_PROCESSING | PROHIBITED                        |
| --------------------------------------------- | --------------------------------- | ----------------- | --------------------------------- |
| ALLOWED                                       | Exception                         | ↓ max ALLOWED     | ↓ max ALLOWED<br><br>↓ max MANUAL |
| MANUAL_PROCESSING                             | ↑ max ALLOWED                     | Exception         | ↓ max MANUAL                      |
| PROHIBITED                                    | ↑ max ALLOWED<br><br>↑ max MANUAL | ↑ max MANUAL      | Exception                         |

The formula for increasing the limit is:

```python
new_limit = 0.8 * current_limit + 0.2 * value_from_transaction
```

And for decreasing the limit:

```python
new_limit = 0.8 * current_limit - 0.2 * value_from_transaction
```

## 📄 License

This project is licensed under the [MIT License](https://mit-license.org/). 

## 🙏 Credits

This project was based on [this course](https://hyperskill.org/projects/232). 
