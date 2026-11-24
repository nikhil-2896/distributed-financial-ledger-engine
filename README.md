# Distributed Financial Ledger Engine

A full-stack banking ledger application built to manage customer accounts and financial transactions with a focus on **transaction integrity, consistency, and reliability**.

The system supports deposits, withdrawals, and account-to-account transfers while maintaining a double-entry ledger for traceable financial records.

## 🚀 Features

- **Customer Management** – Create and view customers.
- **Account Management** – Create accounts and retrieve account details.
- **Deposit & Withdrawal** – Process financial transactions.
- **Fund Transfers** – Transfer funds between accounts.
- **Double-Entry Ledger** – Maintain debit and credit entries for transactions.
- **Idempotency Protection** – Prevent duplicate transaction processing using idempotency keys.
- **Optimistic Locking** – Help prevent concurrent updates from causing inconsistent account balances.
- **Account Freeze/Unfreeze** – Control whether an account can perform transactions.
- **Transaction History** – View ledger entries associated with an account.
- **Error Handling** – Return meaningful responses for invalid or failed operations.

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | React, Vite, JavaScript, Axios |
| Backend | Java 21, Spring Boot |
| Database | MySQL 8 |
| ORM | Spring Data JPA, Hibernate |
| Build Tool | Maven |
| Additional Configuration | Redis |

## 🏗️ Architecture

```text
              React Frontend
                    |
                    | REST API
                    v
             Spring Boot Backend
                    |
          -----------------------
          |          |          |
      Customer    Account   Transaction
      Management  Management  Service
                               |
                               v
                         Ledger Entries
                               |
                               v
                            MySQL
```

## ⚙️ Getting Started

### Prerequisites

- Java 21
- Node.js and npm
- MySQL 8
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/nikhil-2896/distributed-financial-ledger-engine.git
cd distributed-financial-ledger-engine
```

### 2. Configure MySQL

Create the database:

```sql
CREATE DATABASE financial_ledger;
```

Configure the database credentials using environment variables:

**Windows PowerShell:**

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/financial_ledger"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
```

### 3. Run the Backend

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend runs at:

`http://localhost:8080`

### 4. Run the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at:

`http://localhost:5173`

## 🔗 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/customers` | Create customer |
| GET | `/api/customers` | Get all customers |
| POST | `/api/accounts` | Create account |
| GET | `/api/accounts/{id}` | Get account details |
| GET | `/api/accounts/customer/{customerId}` | Get customer accounts |
| PATCH | `/api/accounts/{id}/freeze` | Freeze account |
| PATCH | `/api/accounts/{id}/unfreeze` | Unfreeze account |
| POST | `/api/transactions` | Process transaction |
| GET | `/api/transactions/{reference}` | Get transaction by reference |
| GET | `/api/ledger-entries/account/{accountId}` | Get account ledger history |

## 🔐 Transaction Integrity

The project implements important financial-system concepts:

- **Double-entry bookkeeping:** Transactions maintain corresponding debit and credit ledger entries.
- **Idempotency:** Repeated requests with the same idempotency key are checked to prevent duplicate processing.
- **Optimistic locking:** Version-based concurrency control helps protect account balances during simultaneous updates.
- **Validation:** Checks account status, transaction amount, currency compatibility, and available balance.

## 📁 Project Structure

```text
distributed-financial-ledger-engine/
│
├── frontend/              # React + Vite application
│   ├── src/
│   └── package.json
│
├── src/
│   ├── main/
│   │   ├── java/           # Spring Boot application
│   │   └── resources/      # Application configuration
│   └── test/               # Backend tests
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## 🎯 Project Objective

The objective is to demonstrate how core banking operations can be implemented using a reliable backend architecture, with emphasis on data consistency, transaction safety, concurrency handling, and financial record traceability.

---

**Developed as a full-stack software engineering project using Java, Spring Boot, MySQL, and React.**
