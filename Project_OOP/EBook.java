package Project_OOP;

public class EBook extends LibraryItem implements Taxable, DigitalContent {
    protected String downloadUrl;
    protected double fileSize;

    public EBook(String itemId, String title, String author, String isbn, String downloadUrl, double fileSizeMB) {
        this(itemId, title, author, isbn, 99.0, downloadUrl, fileSizeMB, "Available");
    }

    public EBook(String itemId, String title, String author, String isbn, double price, String downloadUrl, double fileSizeMB) {
        this(itemId, title, author, isbn, price, downloadUrl, fileSizeMB, "Available");
    }

    public EBook(String itemId, String title, String author, String isbn, double price, String downloadUrl, double fileSizeMB, String status) {
        super(itemId, title, author, isbn, price, status);
        this.downloadUrl = downloadUrl;
        this.fileSize = fileSizeMB;
    }

    @Override
    public void displayDetails() {
        System.out.println("E-BOOK");
        System.out.println("- ID: " + itemId);
        System.out.println("- Title: " + title);
        System.out.println("- Author: " + author);
        System.out.println("- ISBN: " + isbn);
        System.out.println("- Price: " + price + " Baht");
        System.out.println("- Download URL: " + downloadUrl);
        System.out.println("- File Size: " + fileSize + " MB");
        System.out.println("- Status: " + status);
        System.out.println("- Borrowed Times: " + timesBorrowed);
        System.out.println("- Borrower: " + (currentBorrower == null ? "-" : currentBorrower.getName()));
        System.out.println("- Return Due Date: " + (returnDueDate == null ? "-" : returnDueDate));
        System.out.println();
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public double calculateLateFee(int days) {
        return 0.0; // E-Books have no late fee
    }

    // --- DigitalContent Interface Implementation ---
    @Override
    public void streamOnline() {
        System.out.println("Streaming '" + title + "' from URL: " + downloadUrl);
        System.out.println("Starting online stream... connected!");
        System.out.println("You can now read the book online without downloading.");
    }

    @Override
    public void download() {
        System.out.println("Downloading '" + title + "' (" + fileSize + " MB)");
        System.out.println("Download link: " + downloadUrl);
        System.out.println("Download complete! File saved to your device.");
    }

    // Calculate 5% digital tax for E-Books
    @Override
    public double calculateTax() {
        return this.price * 0.05;
    }

    @Override
    public void printSummary() {
        System.out.println("EBook [" + itemId + "] " + this.title + " | Size=" + this.fileSize
                + " MB | Status=" + this.status + " | Borrowed=" + this.timesBorrowed);
    }

    @Override
    public String getItemType() {
        return "EBook";
    }
}
