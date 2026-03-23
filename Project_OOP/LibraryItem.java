package Project_OOP;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class LibraryItem {
    protected String itemId;
    protected String title;
    protected String author;
    protected String isbn;
    protected double price;
    protected String status;
    protected LocalDate returnDueDate;
    protected Member currentBorrower;
    protected int timesBorrowed;

    public LibraryItem(String itemId, String title, String author, String isbn, double price) {
        this(itemId, title, author, isbn, price, "Available");
    }

    public LibraryItem(String itemId, String title, String author, String isbn, double price, String status) {
        this.itemId = itemId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.status = normalizeStatus(status);
        this.returnDueDate = null;
        this.currentBorrower = null;
        this.timesBorrowed = 0;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public LocalDate getReturnDueDate() {
        return returnDueDate;
    }

    public void setReturnDueDate(LocalDate returnDueDate) {
        this.returnDueDate = returnDueDate;
    }

    public Member getCurrentBorrower() {
        return currentBorrower;
    }

    public void setCurrentBorrower(Member currentBorrower) {
        this.currentBorrower = currentBorrower;
    }

    public int getTimesBorrowed() {
        return timesBorrowed;
    }

    public void setTimesBorrowed(int timesBorrowed) {
        this.timesBorrowed = Math.max(0, timesBorrowed);
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(this.status);
    }

    public boolean isBorrowed() {
        return "Borrowed".equalsIgnoreCase(this.status);
    }

    public boolean isOverdue() {
        return isBorrowed() && returnDueDate != null && returnDueDate.isBefore(LocalDate.now());
    }

    public int getOverdueDays(LocalDate today) {
        if (returnDueDate == null || !returnDueDate.isBefore(today)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(returnDueDate, today);
    }

    public boolean checkOut(Member borrower) {
        if (borrower == null || !isAvailable() || !borrower.canBorrow()) {
            return false;
        }

        this.status = "Borrowed";
        this.currentBorrower = borrower;
        this.returnDueDate = LocalDate.now().plusDays(borrower.getMembershipStrategy().getLoanPeriodDays());
        this.timesBorrowed++;
        borrower.incrementBorrowed();
        return true;
    }

    public void restoreBorrowState(Member borrower, LocalDate dueDate) {
        this.status = "Borrowed";
        this.currentBorrower = borrower;
        this.returnDueDate = dueDate;
    }

    public void returnItem() {
        if (currentBorrower != null) {
            currentBorrower.decrementBorrowed();
        }
        this.status = "Available";
        this.returnDueDate = null;
        this.currentBorrower = null;
    }

    public void printSummary() {
        String borrowerId = currentBorrower == null ? "-" : currentBorrower.getMemberId();
        System.out.println(getItemType() + " [" + itemId + "] " + title
                + " | Status: " + status
                + " | Borrower: " + borrowerId
                + " | Times Borrowed: " + timesBorrowed);
    }

    public String title() {
        return title;
    }

    protected String normalizeStatus(String value) {
        if ("Borrowed".equalsIgnoreCase(value)) {
            return "Borrowed";
        }
        return "Available";
    }

    public abstract String getItemType();

    public abstract void displayDetails();

    public abstract double calculateLateFee(int days);
}
