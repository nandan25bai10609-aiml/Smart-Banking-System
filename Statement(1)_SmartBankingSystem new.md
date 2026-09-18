# Project Statement

## Smart Banking System

The **Smart Banking System** is a console-based Java application developed to simplify and manage basic banking activities. The system allows customers to create accounts, log in using a PIN, deposit and withdraw money, transfer funds, view transaction records, manage account details, calculate interest, and use other banking services through a simple menu-driven interface.

The project is developed using Object-Oriented Programming concepts in Java. It uses interfaces, abstraction, inheritance, encapsulation, polymorphism, custom exception handling, enums, Java Collections such as `HashMap` and `ArrayList`, and Java serialization for storing application data.

The system provides separate modules for customer banking and administrator management. Customers can create Savings or Current accounts, perform transactions, view statements, change their PIN, and manage interest. Administrators can view and search accounts, freeze or unfreeze accounts, close eligible accounts, view system statistics, and reset daily transaction limits.

During banking operations, the system validates account status, transaction amounts, available balance, receiver account status, and the daily transaction limit. It also generates alerts for large transactions and low account balances. Every successful transaction is stored with a unique transaction ID, transaction type, amount, description, date and time, and balance after the transaction.

The login system provides three PIN attempts. After three incorrect attempts, the account is automatically frozen. Account and transaction information is saved using Java object serialization so that the data can be loaded when the program starts again.

The main purpose of this project is to demonstrate how Java programming and OOP concepts can be combined to develop a practical real-world banking management application. The current version is a local console application and can be further enhanced with database connectivity, graphical or web interfaces, stronger authentication, encrypted credentials, and additional banking services.

## Technologies Used

- Java
- Object-Oriented Programming
- Interface
- Abstract Classes
- Inheritance
- Encapsulation
- Polymorphism
- Java Collections Framework
- HashMap
- ArrayList
- List
- Enum
- Exception Handling
- Custom Exceptions
- Java Serialization
- Scanner
- LocalDateTime
- UUID

## Main Classes

1. Account Creation
2. Customer Login and PIN Authentication
3. Deposit Money
4. Withdraw Money
5. Fund Transfer
6. Transaction History
7. Mini Statement
8. Account Details
9. PIN Management
10. Financial Summary
11. Interest Calculation and Credit
12. Daily Transaction Limit
13. Admin Account Management
14. System Statistics
15. Account Freeze / Unfreeze / Closure
16. Data Persistence
17. Input Validation
18. Transaction Alerts
19. Exception Handling

## Conclusion

The Smart Banking System provides a practical implementation of Java programming and OOP concepts through a real-world banking scenario. It combines account creation, customer authentication, transactions, transaction tracking, interest management, administrative controls, validation, alerts, exception handling, and persistent data storage in one console-based application. The project can serve as a foundation for developing a more advanced banking application with database connectivity, graphical or web interfaces, stronger security, and additional banking services.
