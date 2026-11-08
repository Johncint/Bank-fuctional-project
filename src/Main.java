import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

// ==================== BASE CLASS ====================
class BankAccount {
    private final int accountNumber;
    private final String accountHolder;
    protected double balance;

    public BankAccount(int accountNumber, String accountHolder, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
    }

    public int getAccountNumber()    { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public double getBalance()       { return balance; }

    /** Deposit money. Returns true if successful. */
    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be greater than zero.");
            return false;
        }
        balance += amount;
        System.out.printf(Locale.US, "✅ Deposited [$%.2f]. New balance: [$%.2f]%n", amount, balance);
        return true;
    }

    /** Withdraw money. Overridden by CheckingAccount to add a fee. */
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be greater than zero.");
            return false;
        }
        if (amount > balance) {
            System.out.println("❌ Insufficient funds. Balance: [$" + String.format(Locale.US, "%.2f", balance) + "]");
            return false;
        }
        balance -= amount;
        System.out.printf(Locale.US, "✅ Withdrew [$%.2f]. New balance: [$%.2f]%n", amount, balance);
        return true;
    }

    public void displayAccountInfo() {
        System.out.printf(Locale.US,
                "Good day Sir/Ma'am %s, your account number is %d and your balance is [$%.2f]%n",
                accountHolder, accountNumber, balance);
    }

    /** Short label used in menus. */
    public String getAccountType() { return "Basic"; }
}

// ==================== SAVINGS ====================
class SavingsAccount extends BankAccount {
    private final double interestRate; // percent per application

    public SavingsAccount(int accountNumber, String accountHolder, double balance, double interestRate) {
        super(accountNumber, accountHolder, balance);
        this.interestRate = interestRate;
    }

    public double getInterestRate() { return interestRate; }

    /** Actually applies interest to the balance (mutates balance). */
    public void applyInterest() {
        double interest = balance * interestRate / 100.0;
        balance += interest;
        System.out.printf(Locale.US,
                "💰 Interest of [$%.2f] applied at %.2f%%. New balance: [$%.2f]%n",
                interest, interestRate, balance);
    }

    @Override
    public void displayAccountInfo() {
        System.out.printf(Locale.US,
                "Good day Sir/Ma'am %s, your account number is %d. " +
                        "Current balance: [$%.2f] | Interest rate: %.2f%%%n",
                getAccountHolder(), getAccountNumber(), balance, interestRate);
    }

    @Override
    public String getAccountType() { return "Savings"; }
}

// ==================== CHECKING ====================
class CheckingAccount extends BankAccount {
    private final double transactionFee;

    public CheckingAccount(int accountNumber, String accountHolder, double balance, double transactionFee) {
        super(accountNumber, accountHolder, balance);
        this.transactionFee = transactionFee;
    }

    public double getTransactionFee() { return transactionFee; }

    /** Withdraw with an extra flat fee. */
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be greater than zero.");
            return false;
        }
        double total = amount + transactionFee;
        if (total > balance) {
            System.out.printf(Locale.US,
                    "❌ Insufficient funds (amount [$%.2f] + fee [$%.2f] = [$%.2f], balance [$%.2f]).%n",
                    amount, transactionFee, total, balance);
            return false;
        }
        balance -= total;
        System.out.printf(Locale.US,
                "✅ Withdrew [$%.2f] (+ fee [$%.2f]). New balance: [$%.2f]%n",
                amount, transactionFee, balance);
        return true;
    }

    @Override
    public void displayAccountInfo() {
        System.out.printf(Locale.US,
                "Good day Sir/Ma'am %s, your account number is %d. " +
                        "Current balance: [$%.2f] | Transaction fee: [$%.2f]%n",
                getAccountHolder(), getAccountNumber(), balance, transactionFee);
    }

    @Override
    public String getAccountType() { return "Checking"; }
}

// ==================== MAIN PROGRAM ====================
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<BankAccount> accounts = new ArrayList<>();
    private static int nextAccountNumber = 1000;

    public static void main(String[] args) {
        seedDemoAccounts();   // keep the original 3 accounts as demo data

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> createAccount();
                case 2 -> depositMoney();
                case 3 -> withdrawMoney();
                case 4 -> applyInterest();
                case 5 -> viewAllAccounts();
                case 6 -> running = false;
                default -> System.out.println("❌ Invalid choice.");
            }
        }
        System.out.println("👋 Goodbye!");
        scanner.close();
    }

    // ---------- Original demo accounts from old code ----------
    private static void seedDemoAccounts() {
        accounts.add(new CheckingAccount(8593, "Johncint F Bagonoc", 50000.00, 20000.00));
        accounts.add(new SavingsAccount(3453, "James Singlatan",  30000.00,  2000.00));
        accounts.add(new BankAccount(4895, "Ken",                  1000.00));
        // Keep nextAccountNumber from colliding with demo numbers
        nextAccountNumber = 10000;
    }

    // ---------- Menu ----------
    private static void printMenu() {
        System.out.println("\n===== BANK MENU =====");
        System.out.println("1. Create a new account");
        System.out.println("2. Deposit money");
        System.out.println("3. Withdraw money");
        System.out.println("4. Apply interest (savings only)");
        System.out.println("5. View all accounts");
        System.out.println("6. Exit");
    }

    // ---------- Create account ----------
    private static void createAccount() {
        System.out.println("\n--- Create Account ---");
        System.out.println("1. Savings");
        System.out.println("2. Checking");
        System.out.println("3. Basic");
        int type = readInt("Choose account type: ");

        System.out.print("Enter account holder name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Name cannot be empty.");
            return;
        }

        double initial = readDouble("Enter initial deposit: $");
        if (initial < 0) {
            System.out.println("❌ Initial deposit cannot be negative.");
            return;
        }

        int acctNum = nextAccountNumber++;

        switch (type) {
            case 1 -> {
                double rate = readDouble("Enter interest rate (%): ");
                accounts.add(new SavingsAccount(acctNum, name, initial, rate));
                System.out.printf("✅ Savings account #%d created for %s.%n", acctNum, name);
            }
            case 2 -> {
                double fee = readDouble("Enter transaction fee: $");
                accounts.add(new CheckingAccount(acctNum, name, initial, fee));
                System.out.printf("✅ Checking account #%d created for %s.%n", acctNum, name);
            }
            case 3 -> {
                accounts.add(new BankAccount(acctNum, name, initial));
                System.out.printf("✅ Basic account #%d created for %s.%n", acctNum, name);
            }
            default -> System.out.println("❌ Invalid account type.");
        }
    }

    // ---------- Deposit ----------
    private static void depositMoney() {
        BankAccount acct = pickAccount();
        if (acct == null) return;
        double amount = readDouble("Enter amount to deposit: $");
        acct.deposit(amount);
    }

    // ---------- Withdraw ----------
    private static void withdrawMoney() {
        BankAccount acct = pickAccount();
        if (acct == null) return;
        double amount = readDouble("Enter amount to withdraw: $");
        acct.withdraw(amount);   // CheckingAccount adds the fee automatically
    }

    // ---------- Interest ----------
    private static void applyInterest() {
        BankAccount acct = pickAccount();
        if (acct == null) return;

        if (acct instanceof SavingsAccount savings) {
            savings.applyInterest();
        } else {
            System.out.println("❌ Interest only applies to savings accounts.");
        }
    }

    // ---------- View all ----------
    private static void viewAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts yet.");
            return;
        }
        System.out.println("\n--- All Accounts ---");
        for (BankAccount a : accounts) {
            System.out.printf("   [%s] ", a.getAccountType());
            a.displayAccountInfo();
        }
    }

    // ---------- Helpers ----------
    private static BankAccount pickAccount() {
        if (accounts.isEmpty()) {
            System.out.println("❌ No accounts exist yet.");
            return null;
        }
        int acctNum = readInt("Enter account number: ");
        for (BankAccount a : accounts) {
            if (a.getAccountNumber() == acctNum) return a;
        }
        System.out.println("❌ Account not found.");
        return null;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }
}