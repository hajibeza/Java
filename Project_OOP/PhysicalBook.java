package Project_OOP;

public class PhysicalBook extends LibraryItem implements Taxable {
    private String shelflocation;

    public PhysicalBook(String itemId, String title, String author, String isbn, double price, String shelflocation) {
        super(itemId, title, author, isbn, price, "Available");
        this.shelflocation = shelflocation;
    }

    public PhysicalBook(String itemId, String title, String author, String isbn, double price, String shelflocation, String status) {
        super(itemId, title, author, isbn, price, status);
        this.shelflocation = shelflocation;
    }

    @Override
    public void displayDetails() {
        System.out.println("PHYSICAL BOOK");
        System.out.println("- ID: " + itemId);
        System.out.println("- Title: " + title);
        System.out.println("- Author: " + author);
        System.out.println("- ISBN: " + isbn);
        System.out.println("- Price: " + price + " Baht");
        System.out.println("- Shelf Location: " + shelflocation);
        System.out.println("- Status: " + status);
        System.out.println("- Borrowed Times: " + timesBorrowed);
        System.out.println("- Borrower: " + (currentBorrower == null ? "-" : currentBorrower.getName()));
        System.out.println("- Return Due Date: " + (returnDueDate == null ? "-" : returnDueDate));
        System.out.println();
    }

    public String getShelflocation() {
        return shelflocation;
    }

    public void setShelflocation(String shelflocation) {
        this.shelflocation = shelflocation;
    }

    @Override
    public double calculateLateFee(int days) {
        return days * 5.0;
    }

    @Override
    public void printSummary() {
        System.out.println("PhysicalBook [" + itemId + "] " + this.title + " | Shelf=" + this.shelflocation
                + " | Status=" + this.status + " | Borrowed=" + this.timesBorrowed);
    }

    // Calculate 7% tax for Physical items
    @Override
    public double calculateTax() {
        return this.price * 0.07;
    }

    @Override
    public String getItemType() {
        return "PhysicalBook";
    }
}
