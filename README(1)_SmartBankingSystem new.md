# Smart Banking System

A simple **console-based Smart Banking System developed in Java** for managing bank accounts, customer authentication, banking transactions, transaction records, account status, interest, administrative controls, and system statistics.

## Project Description

The Smart Banking System is designed to simulate the basic operations of a banking application. It provides a menu-driven interface through which customers can create Savings or Current accounts, log in with their account number and PIN, deposit money, withdraw money, transfer funds, view transaction history, manage account details, change their PIN, view financial summaries, and calculate or credit interest.

The application also contains an administrator module. The administrator can view all accounts, search for an account, freeze or unfreeze accounts, close eligible accounts, view system statistics, and reset daily transaction limits.

The project focuses on applying Java programming concepts to a practical real-world problem. It uses Object-Oriented Programming principles, Java Collections, enums, custom exceptions, Java Serialization, and date/time classes to organize and maintain application data.

---

## Objectives

- Create and manage Savings and Current accounts.
- Provide customer login using account number and PIN.
- Allow customers to deposit money.
- Allow customers to withdraw money.
- Support transfers between bank accounts.
- Maintain complete transaction history.
- Provide a latest-five-transactions mini statement.
- Display account details.
- Generate a financial summary.
- Allow customers to change their PIN.
- Calculate and credit account interest.
- Apply a daily transaction limit.
- Generate low-balance and large-transaction alerts.
- Freeze an account after repeated incorrect PIN attempts.
- Allow administrators to manage account status.
- Generate overall system statistics.
- Reset daily transaction limits through the administrator module.
- Store account and transaction data using Java serialization.
- Validate user input.
- Handle invalid operations using custom exceptions.
- Demonstrate important Java OOP concepts.

---

## Features

### Account Creation

The system allows a new customer to create an account by entering:

- Full name
- Phone number
- Email address
- Address
- Four-digit PIN
- Account type

The supported account types are:

- `SAVINGS`
- `CURRENT`

Each account receives a generated account number.

---

### Customer Login

Customers log in using:

- Account number
- PIN

The system allows a maximum of three PIN attempts.

If the PIN is entered incorrectly three times, the account status changes to:

`FROZEN`

Frozen and closed accounts cannot perform normal banking transactions.

---

## Banking Operations

### Deposit

Customers can add money to their account.

The system validates:

- Account status
- Positive transaction amount
- Valid numerical input
- Daily transaction limit

A successful deposit updates the balance and creates a transaction record.

### Withdrawal

Customers can withdraw money when sufficient balance is available.

The system checks:

- Account status
- Positive amount
- Valid numerical value
- Daily transaction limit
- Available balance

### Fund Transfer

Customers can transfer money to another active account.

The system verifies:

- Receiver account exists
- Receiver account is active
- Sender and receiver are different
- Transfer amount is valid
- Daily transaction limit is not exceeded
- Sender has sufficient balance

The transfer is recorded for both accounts using the same transaction ID.

---

## Transaction Management

Every successful transaction records:

- Transaction ID
- Transaction type
- Amount
- Description
- Date and time
- Balance after the transaction

Supported transaction types are:

- `DEPOSIT`
- `WITHDRAWAL`
- `TRANSFER_SENT`
- `TRANSFER_RECEIVED`
- `INTEREST`

### Transaction History

Displays all transactions stored for the account.

### Mini Statement

Displays the latest five transactions.

### Financial Summary

Displays:

- Cash deposited
- Cash withdrawn
- Transfers sent
- Transfers received
- Interest earned
- Current balance

---

## Interest Management

Different interest rates are provided for the two account types.

| Account Type | Interest Rate |
|---|---:|
| Savings Account | 4.0% |
| Current Account | 2.0% |

The application calculates estimated interest from the current account balance. The customer can choose whether to credit the calculated interest.

---

## Account Alerts and Limits

The application uses these configured values:

| Rule | Value |
|---|---:|
| Low Balance Limit | Rs. 3,000 |
| Large Transaction Alert | Rs. 50,000 |
| Daily Transaction Limit | Rs. 1,00,000 |

A low-balance alert is displayed when the account balance falls below Rs. 3,000.

A large-transaction alert is displayed when a deposit, withdrawal, or transfer is Rs. 50,000 or more.

---

## Admin Module

The administrator can access a separate dashboard.

Admin operations include:

1. View all accounts
2. Search for an account
3. Freeze an account
4. Unfreeze an account
5. Close an account
6. View system statistics
7. Reset daily transaction limits
8. Logout

An account with a positive balance cannot be closed.

---

## OOP Concepts Used

### Abstraction

`BankAccount` is an abstract class containing common account information and banking operations. The interest-rate method is left for account subclasses to implement.

### Inheritance

`SavingsAccount` and `CurrentAccount` inherit common functionality from `BankAccount`.

### Encapsulation

Account number, customer information, PIN, balance, account type, status, transaction records, and other account data are maintained inside the account class using controlled access.

### Polymorphism

The `getInterestRate()` method is overridden by `SavingsAccount` and `CurrentAccount` to return their respective interest rates.

### Interface

The `BankingOperations` interface defines the common operations:

- `deposit()`
- `withdraw()`
- `transfer()`

### Exception Handling

Custom exceptions are used for invalid transaction amounts and insufficient balance conditions.

---

## Main Classes

| Class / Type | Purpose |
|---|---|
| `SmartBankingSystem` | Controls the main menu and overall application flow |
| `BankAccount` | Abstract class containing common account data and banking operations |
| `SavingsAccount` | Represents a savings account with a 4% interest rate |
| `CurrentAccount` | Represents a current account with a 2% interest rate |
| `Transaction` | Stores individual transaction information |
| `BankingOperations` | Defines common banking operations |
| `InvalidAmountException` | Handles invalid amounts and invalid transaction states |
| `InsufficientBalanceException` | Handles insufficient balance during withdrawal or transfer |
| `AccountType` | Defines SAVINGS and CURRENT account types |
| `AccountStatus` | Defines ACTIVE, FROZEN, and CLOSED account states |
| `TransactionType` | Defines supported transaction categories |

---

## Technologies Used

- **Language:** Java
- **Application:** Console Application
- **Programming Approach:** Object-Oriented Programming
- **Input:** Java Scanner
- **Collections:** HashMap, ArrayList, List
- **Error Handling:** Custom Exceptions
- **Persistence:** Java Object Serialization
- **Date and Time:** LocalDateTime
- **Unique Transaction IDs:** UUID

---

## Java Collections Used

### HashMap

Used to associate account numbers with their corresponding `BankAccount` objects.

### ArrayList

Used to store the transaction history of each account.

### List

Used as the collection type for the transaction history maintained by `BankAccount`.

---

## Input Validation

The application performs validation for several types of input.

### Amount Validation

Transaction amounts must be positive and finite numerical values.

### Phone Validation

The phone number must contain exactly 10 digits.

### Email Validation

The entered email is checked using a basic email pattern.

### PIN Validation

The PIN must contain exactly four digits.

### Menu Validation

Menu input is repeatedly requested until a valid integer is provided.

### Required Fields

Required text inputs cannot be empty.

---

## Exception Handling

The project contains the following custom exceptions:

- `InvalidAmountException`
- `InsufficientBalanceException`

These exceptions allow the application to display meaningful error messages for invalid amounts, invalid account transaction states, and insufficient balance situations.

---

## Data Persistence

Account information is stored in:

`accounts.dat`

The application uses `ObjectOutputStream` to save:

- Account collection
- Account-number counter

`ObjectInputStream` is used to load the saved information when the program starts.

If the saved data cannot be loaded correctly, the system starts with an empty account collection.

---

## Customer Workflow

```text
Start
  ↓
Main Menu
  ↓
Create Account / Customer Login
  ↓
Enter Account Number
  ↓
Enter PIN
  ↓
Verify Account Status
  ↓
Customer Dashboard
  ↓
Select Banking Operation
  ↓
Validate Operation
  ↓
Execute Operation
  ↓
Record Transaction
  ↓
Save Data
  ↓
Return to Dashboard
  ↓
Logout
```

---

## Admin Workflow

```text
Start
  ↓
Main Menu
  ↓
Admin Login
  ↓
Verify Credentials
  ↓
Admin Dashboard
  ↓
Select Management / Statistics Option
  ↓
Perform Operation
  ↓
Save Updated Data
  ↓
Logout
```

---

## Conclusion

The Smart Banking System provides a practical implementation of Java programming and OOP concepts through a real-world banking scenario. It combines account management, customer authentication, deposits, withdrawals, transfers, transaction tracking, interest handling, administrative controls, validation, alerts, exception handling, and persistent storage in one console-based application.

The project can serve as a foundation for developing a more advanced banking application with database connectivity, graphical or web interfaces, stronger authentication and security, online payment features, and additional banking services.
