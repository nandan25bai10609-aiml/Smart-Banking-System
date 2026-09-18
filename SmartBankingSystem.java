import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
interface BankingOperations {
    void deposit(double amount) throws InvalidAmountException;
    void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException;
    void transfer(BankAccount receiver, double amount) throws InvalidAmountException, InsufficientBalanceException;
}
enum AccountType {
    SAVINGS, CURRENT
}
enum AccountStatus {
    ACTIVE, FROZEN, CLOSED
}
enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER_SENT, TRANSFER_RECEIVED, INTEREST
}
class InvalidAmountException extends Exception {
    InvalidAmountException(String message) {
        super(message);
    }
}
class InsufficientBalanceException extends Exception {
    InsufficientBalanceException(String message) {
        super(message);
    }
}
class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private final String id;
    private final TransactionType type;
    private final double amount;
    private final String description;
    private final LocalDateTime dateTime;
    private final double balanceAfter;
    Transaction(String id, TransactionType type, double amount, String description, double balanceAfter) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.dateTime = LocalDateTime.now();
    }
    TransactionType getType() {
        return type;
    }
    double getAmount() {
        return amount;
    }
    @Override
    public String toString() {
        return String.format("%-12s %-18s Rs. %-10.2f %-28s Rs. %-10.2f %s", id, type, amount, description, balanceAfter, dateTime.format(DISPLAY_DATE));
    }
}
abstract class BankAccount implements BankingOperations, Serializable {
    private static final long serialVersionUID = 1L;
    static final double LOW_BALANCE_LIMIT = 3000.00;
    static final double LARGE_TRANSACTION_LIMIT = 50000.00;
    static final double DAILY_TRANSACTION_LIMIT = 100000.00;
    private final String accountNumber;
    private final String customerName;
    private String phone;
    private String email;
    private String address;
    private String pin;
    private double balance;
    private final AccountType accountType;
    private AccountStatus status = AccountStatus.ACTIVE;
    private final List<Transaction> transactions = new ArrayList<>();
    private double dailyTransactionAmount;
    BankAccount(String accountNumber, String customerName, String phone, String email, String address, String pin, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.pin = pin;
        this.accountType = accountType;
    }
    String getAccountNumber() {
        return accountNumber;
    }
    String getCustomerName() {
        return customerName;
    }
    String getPhone() {
        return phone;
    }
    String getEmail() {
        return email;
    }
    String getAddress() {
        return address;
    }
    double getBalance() {
        return balance;
    }
    AccountType getAccountType() {
        return accountType;
    }
    AccountStatus getStatus() {
        return status;
    }
    double getDailyTransactionAmount() {
        return dailyTransactionAmount;
    }
    int getTransactionCount() {
        return transactions.size();
    }
    boolean matchesPin(String candidate) {
        return pin.equals(candidate);
    }

    void changePin(String newPin) {
        pin = newPin;
    }
    void setStatus(AccountStatus newStatus) {
        status = newStatus;
    }
    void resetDailyLimit() {
        dailyTransactionAmount = 0;
    }
    abstract double getInterestRate();
    private String nextTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    private void requireActive() throws InvalidAmountException {
        if (status == AccountStatus.FROZEN) {
            throw new InvalidAmountException("This account is frozen. Transactions are not allowed.");
        }
        if (status == AccountStatus.CLOSED) {
            throw new InvalidAmountException("This account is closed. Transactions are not allowed.");
        }
    }
    private void validateTransaction(double amount) throws InvalidAmountException {
        requireActive();
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        if (!Double.isFinite(amount)) {
            throw new InvalidAmountException("Amount must be a valid number.");
        }
        if (dailyTransactionAmount + amount > DAILY_TRANSACTION_LIMIT) {
            throw new InvalidAmountException("Daily transaction limit of Rs. " + String.format("%.2f", DAILY_TRANSACTION_LIMIT) + " exceeded.");
        }
    }
    private void addTransaction(TransactionType type, double amount, String description) {
        transactions.add(new Transaction(nextTransactionId(), type, amount, description, balance));
    }

    @Override
    public void deposit(double amount) throws InvalidAmountException {
        validateTransaction(amount);
        balance += amount;
        dailyTransactionAmount += amount;
        addTransaction(TransactionType.DEPOSIT, amount, "Cash deposit");
        System.out.printf("Deposit successful. New balance: Rs. %.2f%n", balance);
        showAlerts(amount);
    }

    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        validateTransaction(amount);

        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient balance.");
        }

        balance -= amount;
        dailyTransactionAmount += amount;
        addTransaction(TransactionType.WITHDRAWAL, amount, "Cash withdrawal");
        System.out.printf("Withdrawal successful. Remaining balance: Rs. %.2f%n", balance);
        showAlerts(amount);
    }

    @Override
    public void transfer(BankAccount receiver, double amount) throws InvalidAmountException, InsufficientBalanceException {
        validateTransaction(amount);

        if (receiver == this) {
            throw new InvalidAmountException("You cannot transfer money to the same account.");
        }

        if (receiver.status != AccountStatus.ACTIVE) {
            throw new InvalidAmountException("The receiver account is not active.");
        }

        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer.");
        }

        balance -= amount;
        receiver.balance += amount;
        dailyTransactionAmount += amount;

        String transferId = nextTransactionId();

        transactions.add(new Transaction(transferId, TransactionType.TRANSFER_SENT, amount, "Transfer to " + receiver.accountNumber, balance));

        receiver.transactions.add(new Transaction(transferId, TransactionType.TRANSFER_RECEIVED, amount, "Transfer from " + accountNumber, receiver.balance));

        System.out.println("Transfer successful.");
        System.out.println("Transaction ID: " + transferId);
        System.out.printf("New balance: Rs. %.2f%n", balance);
        showAlerts(amount);
    }

    double calculateInterest() {
        return balance * getInterestRate() / 100.0;
    }

    void addInterest() throws InvalidAmountException {
        requireActive();

        double interest = calculateInterest();

        if (interest <= 0) {
            System.out.println("There is no interest to credit.");
            return;
        }

        balance += interest;
        addTransaction(TransactionType.INTEREST, interest, "Interest credited");

        System.out.printf("Interest credited: Rs. %.2f%n", interest);
    }

    private void showAlerts(double amount) {
        if (amount >= LARGE_TRANSACTION_LIMIT) {
            System.out.printf("Alert: large transaction of Rs. %.2f recorded.%n", amount);
        }

        if (balance < LOW_BALANCE_LIMIT) {
            System.out.printf("Alert: balance is below Rs. %.2f.%n", LOW_BALANCE_LIMIT);
        }
    }

    void showBalance() {
        System.out.printf("Current balance: Rs. %.2f%n", balance);

        if (balance < LOW_BALANCE_LIMIT) {
            System.out.println("Low-balance alert.");
        }
    }

    void showMiniStatement() {
        System.out.println("\n--- Mini Statement (Latest 5 Transactions) ---");

        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        int firstTransaction = Math.max(0, transactions.size() - 5);

        for (int index = firstTransaction; index < transactions.size(); index++) {
            System.out.println(transactions.get(index));
        }
    }

    void showTransactionHistory() {
        System.out.println("\n--- Transaction History ---");

        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    void showDetails() {
        System.out.println("\n--- Account Details ---");
        System.out.println("Customer name : " + customerName);
        System.out.println("Account number: " + accountNumber);
        System.out.println("Account type  : " + accountType);
        System.out.println("Phone         : " + phone);
        System.out.println("Email         : " + email);
        System.out.println("Address       : " + address);
        System.out.printf("Balance       : Rs. %.2f%n", balance);
        System.out.println("Status        : " + status);
        System.out.printf("Interest rate : %.2f%%%n", getInterestRate());
    }

    void showFinancialSummary() {
        double deposited = 0;
        double withdrawn = 0;
        double sent = 0;
        double received = 0;
        double interest = 0;

        for (Transaction transaction : transactions) {
            switch (transaction.getType()) {
                case DEPOSIT:
                    deposited += transaction.getAmount();
                    break;
                case WITHDRAWAL:
                    withdrawn += transaction.getAmount();
                    break;
                case TRANSFER_SENT:
                    sent += transaction.getAmount();
                    break;
                case TRANSFER_RECEIVED:
                    received += transaction.getAmount();
                    break;
                case INTEREST:
                    interest += transaction.getAmount();
                    break;
                default:
                    break;
            }
        }

        System.out.println("\n--- Financial Summary ---");
        System.out.printf("Cash deposited    : Rs. %.2f%n", deposited);
        System.out.printf("Cash withdrawn    : Rs. %.2f%n", withdrawn);
        System.out.printf("Transfers sent    : Rs. %.2f%n", sent);
        System.out.printf("Transfers received: Rs. %.2f%n", received);
        System.out.printf("Interest earned   : Rs. %.2f%n", interest);
        System.out.printf("Current balance   : Rs. %.2f%n", balance);
    }

    void showDailyLimit() {
        System.out.println("\n--- Daily Transaction Limit ---");
        System.out.printf("Used      : Rs. %.2f%n", dailyTransactionAmount);
        System.out.printf("Available : Rs. %.2f%n", DAILY_TRANSACTION_LIMIT - dailyTransactionAmount);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | Rs. %.2f | %s", accountNumber, customerName, accountType, balance, status);
    }
}

class SavingsAccount extends BankAccount {
    private static final long serialVersionUID = 1L;

    SavingsAccount(String accountNumber, String customerName, String phone, String email, String address, String pin) {
        super(accountNumber, customerName, phone, email, address, pin, AccountType.SAVINGS);
    }

    @Override
    double getInterestRate() {
        return 4.0;
    }
}

class CurrentAccount extends BankAccount {
    private static final long serialVersionUID = 1L;

    CurrentAccount(String accountNumber, String customerName, String phone, String email, String address, String pin) {
        super(accountNumber, customerName, phone, email, address, pin, AccountType.CURRENT);
    }

    @Override
    double getInterestRate() {
        return 2.0;
    }
}

public class SmartBankingSystem {
    private static final String DATA_FILE = "accounts.dat";
    private static final Scanner SCANNER = new Scanner(System.in);
    private static Map<String, BankAccount> accounts = new HashMap<>();
    private static int accountCounter = 10010001;

    public static void main(String[] args) {
        loadData();

        while (true) {
            printMainMenu();

            switch (readInt("Choose an option: ")) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    customerLogin();
                    break;
                case 3:
                    adminLogin();
                    break;
                case 4:
                    saveData();
                    System.out.println("Thank you for using Smart Banking System.");
                    return;
                default:
                    System.out.println("Please choose a valid option.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n=== SMART BANKING SYSTEM ===");
        System.out.println("1. Create a new account");
        System.out.println("2. Customer login");
        System.out.println("3. Admin login");
        System.out.println("4. Exit");
    }

    private static void createAccount() {
        System.out.println("\n--- Create Account ---");

        String name = readRequired("Full name: ");
        String phone = readPhone();
        String email = readEmail();
        String address = readRequired("Address: ");
        String pin = readNewPin();

        int accountChoice;

        while (true) {
            System.out.println("\nSelect Account Type:");
            System.out.println("1. Savings Account");
            System.out.println("2. Current Account");

            accountChoice = readInt("Enter your choice: ");

            if (accountChoice == 1 || accountChoice == 2) {
                break;
            }

            System.out.println("Invalid choice. Please select 1 or 2.");
        }

        String accountNumber = String.valueOf(accountCounter++);

        BankAccount account;

        if (accountChoice == 1) {
            account = new SavingsAccount(accountNumber, name, phone, email, address, pin);
        } else {
            account = new CurrentAccount(accountNumber, name, phone, email, address, pin);
        }

        accounts.put(accountNumber, account);
        saveData();

        System.out.println("\nAccount created successfully.");
        System.out.println("Account Type   : " + account.getAccountType());
        System.out.println("Account Number : " + accountNumber);
        System.out.println("Keep your account number and PIN safe.");
    }

    private static void customerLogin() {
        System.out.println("\n--- Customer Login ---");

        BankAccount account = accounts.get(readRequired("Account number: "));

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            System.out.println("This account is closed.");
            return;
        }

        if (account.getStatus() == AccountStatus.FROZEN) {
            System.out.println("This account is frozen. Contact the administrator.");
            return;
        }

        for (int attemptsLeft = 3; attemptsLeft > 0; attemptsLeft--) {
            if (account.matchesPin(readRequired("PIN: "))) {
                System.out.println("Login successful.");
                customerDashboard(account);
                return;
            }

            System.out.println("Incorrect PIN. Attempts remaining: " + (attemptsLeft - 1));
        }

        account.setStatus(AccountStatus.FROZEN);
        saveData();

        System.out.println("Too many incorrect attempts. The account has been frozen.");
    }

    private static void customerDashboard(BankAccount account) {
        while (true) {
            System.out.println("\n--- Customer Dashboard ---");
            System.out.println("Welcome, " + account.getCustomerName());
            System.out.printf("Balance: Rs. %.2f%n", account.getBalance());
            System.out.println("1. Check balance");
            System.out.println("2. Deposit money");
            System.out.println("3. Withdraw money");
            System.out.println("4. Transfer money");
            System.out.println("5. Transaction history");
            System.out.println("6. Mini statement");
            System.out.println("7. Account details");
            System.out.println("8. Change PIN");
            System.out.println("9. Financial summary");
            System.out.println("10. Daily transaction limit");
            System.out.println("11. Calculate interest");
            System.out.println("12. Logout");

            switch (readInt("Choose an option: ")) {
                case 1:
                    account.showBalance();
                    break;
                case 2:
                    depositMoney(account);
                    break;
                case 3:
                    withdrawMoney(account);
                    break;
                case 4:
                    transferMoney(account);
                    break;
                case 5:
                    account.showTransactionHistory();
                    break;
                case 6:
                    account.showMiniStatement();
                    break;
                case 7:
                    account.showDetails();
                    break;
                case 8:
                    changePin(account);
                    break;
                case 9:
                    account.showFinancialSummary();
                    break;
                case 10:
                    account.showDailyLimit();
                    break;
                case 11:
                    manageInterest(account);
                    break;
                case 12:
                    saveData();
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Please choose a valid option.");
            }
        }
    }

    private static void depositMoney(BankAccount account) {
        try {
            account.deposit(readDouble("Deposit amount: Rs. "));
            saveData();
        } catch (InvalidAmountException exception) {
            System.out.println("Deposit failed: " + exception.getMessage());
        }
    }

    private static void withdrawMoney(BankAccount account) {
        try {
            account.withdraw(readDouble("Withdrawal amount: Rs. "));
            saveData();
        } catch (InvalidAmountException | InsufficientBalanceException exception) {
            System.out.println("Withdrawal failed: " + exception.getMessage());
        }
    }

    private static void transferMoney(BankAccount sender) {
        BankAccount receiver = accounts.get(readRequired("Receiver account number: "));

        if (receiver == null) {
            System.out.println("Receiver account not found.");
            return;
        }

        try {
            sender.transfer(receiver, readDouble("Transfer amount: Rs. "));
            saveData();
        } catch (InvalidAmountException | InsufficientBalanceException exception) {
            System.out.println("Transfer failed: " + exception.getMessage());
        }
    }

    private static void changePin(BankAccount account) {
        if (!account.matchesPin(readRequired("Current PIN: "))) {
            System.out.println("Current PIN is incorrect.");
            return;
        }

        String newPin = readNewPin();
        String confirmation = readRequired("Confirm new PIN: ");

        if (!newPin.equals(confirmation)) {
            System.out.println("PIN confirmation does not match.");
            return;
        }

        account.changePin(newPin);
        saveData();

        System.out.println("PIN changed successfully.");
    }

    private static void manageInterest(BankAccount account) {
        System.out.printf("Estimated interest at %.2f%%: Rs. %.2f%n", account.getInterestRate(), account.calculateInterest());

        if (readInt("Credit this interest? (1 = yes, 2 = no): ") != 1) {
            return;
        }

        try {
            account.addInterest();
            saveData();
        } catch (InvalidAmountException exception) {
            System.out.println("Interest could not be credited: " + exception.getMessage());
        }
    }

    private static void adminLogin() {
        System.out.println("\n--- Admin Login ---");

        String username = readRequired("Username: ");
        String password = readRequired("Password: ");

        if ("admin".equals(username) && "admin123".equals(password)) {
            System.out.println("Admin login successful.");
            adminDashboard();
        } else {
            System.out.println("Invalid admin credentials.");
        }
    }

    private static void adminDashboard() {
        while (true) {
            System.out.println("\n--- Admin Dashboard ---");
            System.out.println("1. View all accounts");
            System.out.println("2. Search for an account");
            System.out.println("3. Freeze an account");
            System.out.println("4. Unfreeze an account");
            System.out.println("5. Close an account");
            System.out.println("6. View system statistics");
            System.out.println("7. Reset daily limits");
            System.out.println("8. Logout");

            switch (readInt("Choose an option: ")) {
                case 1:
                    viewAllAccounts();
                    break;
                case 2:
                    findAccount();
                    break;
                case 3:
                    updateAccountStatus(AccountStatus.FROZEN);
                    break;
                case 4:
                    updateAccountStatus(AccountStatus.ACTIVE);
                    break;
                case 5:
                    closeAccount();
                    break;
                case 6:
                    showSystemStatistics();
                    break;
                case 7:
                    resetDailyLimits();
                    break;
                case 8:
                    System.out.println("Admin logged out.");
                    return;
                default:
                    System.out.println("Please choose a valid option.");
            }
        }
    }

    private static void viewAllAccounts() {
        System.out.println("\n--- All Accounts ---");

        if (accounts.isEmpty()) {
            System.out.println("No accounts available.");
            return;
        }

        for (BankAccount account : accounts.values()) {
            System.out.println(account);
        }
    }

    private static void findAccount() {
        BankAccount account = accounts.get(readRequired("Account number: "));

        if (account == null) {
            System.out.println("Account not found.");
        } else {
            account.showDetails();
        }
    }

    private static void updateAccountStatus(AccountStatus status) {
        BankAccount account = accounts.get(readRequired("Account number: "));

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        if (account.getStatus() == AccountStatus.CLOSED && status == AccountStatus.FROZEN) {
            System.out.println("Closed account cannot be frozen.");
            return;
        }

        account.setStatus(status);
        saveData();

        System.out.println("Account status changed to " + status + ".");
    }

    private static void closeAccount() {
        BankAccount account = accounts.get(readRequired("Account number: "));

        if (account == null) {
            System.out.println("Account not found.");
        } else if (account.getStatus() == AccountStatus.CLOSED) {
            System.out.println("Account is already closed.");
        } else if (account.getBalance() > 0) {
            System.out.println("An account with a positive balance cannot be closed.");
        } else {
            account.setStatus(AccountStatus.CLOSED);
            saveData();
            System.out.println("Account closed successfully.");
        }
    }

    private static void showSystemStatistics() {
        int savings = 0;
        int current = 0;
        int active = 0;
        int frozen = 0;
        int closed = 0;
        int transactions = 0;
        double totalBalance = 0;

        for (BankAccount account : accounts.values()) {
            if (account.getAccountType() == AccountType.SAVINGS) {
                savings++;
            } else {
                current++;
            }

            if (account.getStatus() == AccountStatus.ACTIVE) {
                active++;
            } else if (account.getStatus() == AccountStatus.FROZEN) {
                frozen++;
            } else {
                closed++;
            }

            totalBalance += account.getBalance();
            transactions += account.getTransactionCount();
        }

        System.out.println("\n--- System Statistics ---");
        System.out.println("Total accounts   : " + accounts.size());
        System.out.println("Savings accounts : " + savings);
        System.out.println("Current accounts : " + current);
        System.out.println("Active accounts  : " + active);
        System.out.println("Frozen accounts  : " + frozen);
        System.out.println("Closed accounts  : " + closed);
        System.out.printf("Total balance    : Rs. %.2f%n", totalBalance);
        System.out.println("Transactions     : " + transactions);
    }

    private static void resetDailyLimits() {
        for (BankAccount account : accounts.values()) {
            account.resetDailyLimit();
        }

        saveData();

        System.out.println("Daily transaction limits have been reset.");
    }

    private static int readInt(String prompt) {
        while (true) {
            String input = readRequired(prompt);

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            String input = readRequired(prompt);

            try {
                double value = Double.parseDouble(input);

                if (!Double.isFinite(value)) {
                    throw new NumberFormatException();
                }

                return value;
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);

            String input = SCANNER.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private static String readPhone() {
        while (true) {
            String phone = readRequired("Phone number: ");

            if (phone.matches("\\d{10}")) {
                return phone;
            }

            System.out.println("Phone number must contain exactly 10 digits.");
        }
    }

    private static String readEmail() {
        while (true) {
            String email = readRequired("Email address: ");

            if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return email;
            }

            System.out.println("Please enter a valid email address.");
        }
    }

    private static String readNewPin() {
        while (true) {
            String pin = readRequired("Create a 4-digit PIN: ");

            if (pin.matches("\\d{4}")) {
                return pin;
            }

            System.out.println("PIN must contain exactly 4 digits.");
        }
    }

    private static void saveData() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            output.writeObject(new HashMap<>(accounts));
            output.writeInt(accountCounter);
        } catch (IOException exception) {
            System.out.println("Warning: data could not be saved.");
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File dataFile = new File(DATA_FILE);

        if (!dataFile.exists()) {
            return;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(dataFile))) {
            accounts = (Map<String, BankAccount>) input.readObject();
            accountCounter = input.readInt();
        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            System.out.println("Saved data could not be loaded. Starting with no accounts.");
            accounts = new HashMap<>();
            accountCounter = 10010001;
        }
    }
}