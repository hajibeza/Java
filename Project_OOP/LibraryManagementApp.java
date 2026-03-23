package Project_OOP;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class LibraryManagementApp {
    private final LibrarySystem librarySystem;
    private final LibraryFileManager fileManager;
    private final Scanner scanner;
    private Member currentUser;

    public LibraryManagementApp() {
        this.librarySystem = LibrarySystem.getInstance();
        this.fileManager = new LibraryFileManager();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new LibraryManagementApp().run();
    }

    public void run() {
        initializeData();

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showAuthenticationMenu();
            } else if (currentUser.isLibrarian()) {
                running = showLibrarianMenu();
            } else {
                running = showMemberMenu();
            }
        }
    }

    private void initializeData() {
        System.out.println("============================================================");
        System.out.println("LIBRARY MANAGEMENT SYSTEM");
        System.out.println("============================================================");

        if (fileManager.dataFilesExist()) {
            try {
                fileManager.loadLibraryData(librarySystem);
                System.out.println("Loaded data from CSV files.");
            } catch (IOException exception) {
                System.out.println("Could not load CSV files: " + exception.getMessage());
                librarySystem.clearData();
                librarySystem.seedSampleData();
                System.out.println("Sample data loaded instead.");
            }
        } else {
            librarySystem.seedSampleData();
            System.out.println("Sample data loaded into memory.");
        }

        if (librarySystem.ensureFixedLibrarianAccount()) {
            autoSave("librarian account setup");
        }
        System.out.println();
    }

    private boolean showAuthenticationMenu() {
        System.out.println("\n================ AUTH MENU ================");
        System.out.println("1. Login");
        System.out.println("2. Register member");
        System.out.println("0. Exit");
        System.out.println("===========================================");
        int choice = InputHelper.readInt(scanner, "Choose menu: ", 0, 2);
        System.out.println();

        switch (choice) {
            case 1 -> login();
            case 2 -> addNewMember();
            case 0 -> {
                return exitApplication();
            }
            default -> System.out.println("Invalid menu.");
        }
        return true;
    }

    private boolean showLibrarianMenu() {
        System.out.println("\n========== LIBRARIAN MENU ==========");
        System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        System.out.println("1. Show all library items");
        System.out.println("2. Show all members");
        System.out.println("3. Add new member");
        System.out.println("4. Add new item");
        System.out.println("5. Search member/item");
        System.out.println("6. Update member/item");
        System.out.println("7. Delete member/item");
        System.out.println("8. Borrow item");
        System.out.println("9. Return item");
        System.out.println("10. Reports and file menu");
        System.out.println("11. Logout");
        System.out.println("0. Exit");
        System.out.println("====================================");
        int choice = InputHelper.readInt(scanner, "Choose menu: ", 0, 11);
        System.out.println();

        switch (choice) {
            case 1 -> showAllItems();
            case 2 -> showAllMembers();
            case 3 -> addNewMember();
            case 4 -> addNewItem();
            case 5 -> searchData();
            case 6 -> updateData();
            case 7 -> deleteData();
            case 8 -> borrowItem();
            case 9 -> returnItem();
            case 10 -> reportsAndFileMenu();
            case 11 -> logout();
            case 0 -> {
                return exitApplication();
            }
            default -> System.out.println("Invalid menu.");
        }
        return true;
    }

    private boolean showMemberMenu() {
        System.out.println("\n============ MEMBER MENU ============");
        System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        System.out.println("1. Show all library items");
        System.out.println("2. Show my profile");
        System.out.println("3. Borrow item");
        System.out.println("4. Return item");
        System.out.println("5. Reports and file menu");
        System.out.println("6. Logout");
        System.out.println("0. Exit");
        System.out.println("====================================");
        int choice = InputHelper.readInt(scanner, "Choose menu: ", 0, 6);
        System.out.println();

        switch (choice) {
            case 1 -> showAllItems();
            case 2 -> currentUser.displayMemberInfo();
            case 3 -> borrowItem();
            case 4 -> returnItem();
            case 5 -> reportsAndFileMenu();
            case 6 -> logout();
            case 0 -> {
                return exitApplication();
            }
            default -> System.out.println("Invalid menu.");
        }
        return true;
    }

    private void login() {
        System.out.println("LOGIN");
        String name = InputHelper.readNonEmptyString(scanner, "Name: ");
        String password = InputHelper.readNonEmptyString(scanner, "Password: ");
        Member user = librarySystem.authenticateUser(name, password);

        if (user == null) {
            System.out.println("Invalid name or password.");
            return;
        }

        currentUser = user;
        System.out.println("Login successful. Welcome, " + currentUser.getName() + ".");
    }

    private void logout() {
        System.out.println("Logged out from " + currentUser.getName() + ".");
        currentUser = null;
    }

    private void showAllItems() {
        List<LibraryItem> items = librarySystem.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        for (LibraryItem item : items) {
            item.displayDetails();
        }
    }

    private void showAllMembers() {
        if (!requireLibrarianAccess("show all members")) {
            return;
        }

        List<Member> members = librarySystem.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        for (Member member : members) {
            member.displayMemberInfo();
            System.out.println();
        }
    }

    private void addNewMember() {
        System.out.println("REGISTER MEMBER");
        String memberId = librarySystem.generateNextMemberId();
        System.out.println("Generated Member ID: " + memberId);
        String name = InputHelper.readNonEmptyString(scanner, "Member name: ");
        if (librarySystem.hasDuplicateMemberName(name, null)) {
            System.out.println("This member name already exists.");
            return;
        }
        String password = InputHelper.readNonEmptyString(scanner, "Password: ");
        MembershipStrategy strategy = selectMembershipStrategy();
        Member member = new Member(memberId, name, strategy, 0, Member.ROLE_MEMBER, password);

        if (librarySystem.addMember(member)) {
            System.out.println("Member added successfully with ID " + memberId + ".");
            autoSave("member registration");
        } else {
            System.out.println("Could not add member.");
        }
    }

    private void addNewItem() {
        if (!requireLibrarianAccess("add new item")) {
            return;
        }

        System.out.println("ADD NEW ITEM");
        int typeChoice = InputHelper.readInt(scanner, "1. PhysicalBook  2. EBook: ", 1, 2);
        String itemType = typeChoice == 1 ? "PhysicalBook" : "EBook";
        String itemId = librarySystem.generateNextItemId(itemType);
        System.out.println("Generated Item ID: " + itemId);

        String title = InputHelper.readNonEmptyString(scanner, "Title: ");
        String author = InputHelper.readNonEmptyString(scanner, "Author: ");
        String isbn = InputHelper.readNonEmptyString(scanner, "ISBN: ");
        double price = InputHelper.readDouble(scanner, "Price: ", 0.0);

        LibraryItem item;
        if (typeChoice == 1) {
            String shelfLocation = InputHelper.readNonEmptyString(scanner, "Shelf location: ");
            item = new PhysicalBook(itemId, title, author, isbn, price, shelfLocation);
        } else {
            String downloadUrl = InputHelper.readNonEmptyString(scanner, "Download URL: ");
            double fileSize = InputHelper.readDouble(scanner, "File size (MB): ", 0.1);
            item = new EBook(itemId, title, author, isbn, price, downloadUrl, fileSize);
        }

        if (librarySystem.hasDuplicateItem(item, null)) {
            System.out.println("This item already exists in the system.");
            return;
        }

        if (librarySystem.addItem(item)) {
            System.out.println("Item added successfully.");
            autoSave("item creation");
        } else {
            System.out.println("Could not add item.");
        }
    }

    private void searchData() {
        if (!requireLibrarianAccess("search member/item")) {
            return;
        }

        int choice = InputHelper.readInt(scanner, "1. Search member  2. Search item: ", 1, 2);
        String keyword = InputHelper.readNonEmptyString(scanner, "Keyword: ");

        if (choice == 1) {
            List<Member> members = librarySystem.findMembersByName(keyword);
            if (members.isEmpty()) {
                System.out.println("No member found.");
                return;
            }
            for (Member member : members) {
                member.displayMemberInfo();
                System.out.println();
            }
            return;
        }

        List<LibraryItem> items = librarySystem.findItems(keyword);
        if (items.isEmpty()) {
            System.out.println("No item found.");
            return;
        }
        for (LibraryItem item : items) {
            item.displayDetails();
        }
    }

    private void updateData() {
        if (!requireLibrarianAccess("update member/item")) {
            return;
        }

        int choice = InputHelper.readInt(scanner, "1. Update member  2. Update item: ", 1, 2);
        if (choice == 1) {
            updateMember();
        } else {
            updateItem();
        }
    }

    private void updateMember() {
        showMemberSummaryList();

        String memberId = InputHelper.readNonEmptyString(scanner, "Member ID to update: ");
        Member member = librarySystem.findMemberById(memberId);
        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        String updatedName = InputHelper.readOptionalString(scanner, "New member name", member.getName());
        if (librarySystem.hasDuplicateMemberName(updatedName, member.getMemberId())) {
            System.out.println("Another member already uses this name.");
            return;
        }

        member.setName(updatedName);
        if (InputHelper.confirm(scanner, "Change membership type")) {
            member.setMembershipStrategy(selectMembershipStrategy());
        }
        System.out.println("Member updated successfully.");
        autoSave("member update");
    }

    private void updateItem() {
        showItemSummaryList();

        String itemId = InputHelper.readNonEmptyString(scanner, "Item ID to update: ");
        LibraryItem item = librarySystem.findItemById(itemId);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        String updatedTitle = InputHelper.readOptionalString(scanner, "New title", item.getTitle());
        String updatedAuthor = InputHelper.readOptionalString(scanner, "New author", item.getAuthor());
        String updatedIsbn = InputHelper.readOptionalString(scanner, "New ISBN", item.getIsbn());
        double updatedPrice = InputHelper.readDouble(scanner, "New price: ", 0.0);

        String originalTitle = item.getTitle();
        String originalAuthor = item.getAuthor();
        String originalIsbn = item.getIsbn();
        double originalPrice = item.getPrice();

        item.setTitle(updatedTitle);
        item.setAuthor(updatedAuthor);
        item.setIsbn(updatedIsbn);
        item.setPrice(updatedPrice);

        if (item instanceof PhysicalBook physicalBook) {
            physicalBook.setShelflocation(InputHelper.readOptionalString(scanner, "New shelf location", physicalBook.getShelflocation()));
        } else if (item instanceof EBook eBook) {
            eBook.setDownloadUrl(InputHelper.readOptionalString(scanner, "New download URL", eBook.getDownloadUrl()));
            eBook.setFileSize(InputHelper.readDouble(scanner, "New file size (MB): ", 0.1));
        }

        if (librarySystem.hasDuplicateItem(item, item.getItemId())) {
            item.setTitle(originalTitle);
            item.setAuthor(originalAuthor);
            item.setIsbn(originalIsbn);
            item.setPrice(originalPrice);
            System.out.println("Another item already uses the same ISBN or title and author.");
            return;
        }

        System.out.println("Item updated successfully.");
        autoSave("item update");
    }

    private void showMemberSummaryList() {
        List<Member> members = librarySystem.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        System.out.println("MEMBER LIST");
        for (Member member : members) {
            System.out.println(member.getMemberId() + " - " + member.getName()
                    + " | Role: " + member.getRole()
                    + " | Type: " + member.getMembershipType());
        }
        System.out.println();
    }

    private void showItemSummaryList() {
        List<LibraryItem> items = librarySystem.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        System.out.println("ITEM LIST");
        for (LibraryItem item : items) {
            System.out.println(item.getItemId() + " - " + item.getTitle()
                    + " | Type: " + item.getItemType()
                    + " | Status: " + item.getStatus());
        }
        System.out.println();
    }

    private void deleteData() {
        if (!requireLibrarianAccess("delete member/item")) {
            return;
        }

        int choice = InputHelper.readInt(scanner, "1. Delete member  2. Delete item: ", 1, 2);
        if (choice == 1) {
            deleteMember();
        } else {
            deleteItem();
        }
    }

    private void deleteMember() {
        showMemberSummaryList();

        String memberId = InputHelper.readNonEmptyString(scanner, "Member ID to delete: ");
        if (librarySystem.removeMember(memberId)) {
            System.out.println("Member deleted successfully.");
            autoSave("member deletion");
        } else {
            System.out.println("Cannot delete member. Check ID or borrowed items.");
        }
    }

    private void deleteItem() {
        showItemSummaryList();

        String itemId = InputHelper.readNonEmptyString(scanner, "Item ID to delete: ");
        if (librarySystem.removeItem(itemId)) {
            System.out.println("Item deleted successfully.");
            autoSave("item deletion");
        } else {
            System.out.println("Cannot delete item. Check ID or borrowed status.");
        }
    }

    private void borrowItem() {
        Member member = currentUser != null && !currentUser.isLibrarian()
                ? currentUser
                : librarySystem.findMemberById(InputHelper.readNonEmptyString(scanner, "Member ID: "));
        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        showBorrowableItems();

        String itemId = InputHelper.readNonEmptyString(scanner, "Item ID: ");
        LibraryItem item = librarySystem.findItemById(itemId);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        if (!item.isAvailable()) {
            System.out.println("Item is not available.");
            return;
        }
        if (!member.canBorrow()) {
            System.out.println("Borrow limit reached for " + member.getMembershipType() + ".");
            return;
        }

        if (librarySystem.borrowItem(itemId, member.getMemberId())) {
            System.out.println("Borrow successful.");
            System.out.println("Due date: " + item.getReturnDueDate());
            autoSave("borrow transaction");
        } else {
            System.out.println("Borrow failed.");
        }
    }

    private void showBorrowableItems() {
        List<LibraryItem> items = librarySystem.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        System.out.println("AVAILABLE ITEM LIST");
        for (LibraryItem item : items) {
            System.out.println(item.getItemId() + " - " + item.getTitle()
                    + " | Type: " + item.getItemType()
                    + " | Status: " + item.getStatus());
        }
        System.out.println();
    }

    private void returnItem() {
        showReturnableItems();

        String itemId = InputHelper.readNonEmptyString(scanner, "Item ID to return: ");
        LibraryItem item = librarySystem.findItemById(itemId);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        if (item.isAvailable()) {
            System.out.println("This item is already available.");
            return;
        }

        Member borrower = item.getCurrentBorrower();
        if (currentUser != null && !currentUser.isLibrarian()
                && borrower != null
                && !borrower.getMemberId().equalsIgnoreCase(currentUser.getMemberId())) {
            System.out.println("You can return only your own borrowed items.");
            return;
        }

        int daysLate = item.getOverdueDays(LocalDate.now());
        double baseFee = item.calculateLateFee(daysLate);
        double finalFee = borrower == null ? baseFee : borrower.calculateLateFee(baseFee);

        item.returnItem();

        System.out.println("Return completed for: " + item.getTitle());
        System.out.println("Borrower: " + (borrower == null ? "-" : borrower.getName()));
        System.out.println("Days late: " + daysLate);
        System.out.println("Late fee: " + finalFee + " Baht");
        autoSave("return transaction");
    }

    private void showReturnableItems() {
        List<LibraryItem> items = currentUser != null && !currentUser.isLibrarian()
                ? librarySystem.getBorrowedItemsByMember(currentUser.getMemberId())
                : librarySystem.getBorrowedItems();

        if (items.isEmpty()) {
            System.out.println("No borrowed items available for return.");
            return;
        }

        System.out.println("RETURN ITEM LIST");
        for (LibraryItem item : items) {
            String borrowerName = item.getCurrentBorrower() == null ? "-" : item.getCurrentBorrower().getName();
            System.out.println(item.getItemId() + " - " + item.getTitle()
                    + " | Type: " + item.getItemType()
                    + " | Borrower: " + borrowerName
                    + " | Due: " + (item.getReturnDueDate() == null ? "-" : item.getReturnDueDate()));
        }
        System.out.println();
    }

    private void reportsAndFileMenu() {
        System.out.println("1. Overdue items report");
        System.out.println("2. Most borrowed items report");
        System.out.println("3. Price and tax report");
        if (currentUser != null && currentUser.isLibrarian()) {
            System.out.println("4. Save data to CSV");
            System.out.println("5. Load data from CSV");
            System.out.println("6. System statistics");
        }
        int maxChoice = currentUser != null && currentUser.isLibrarian() ? 6 : 3;
        int choice = InputHelper.readInt(scanner, "Choose option: ", 1, maxChoice);

        switch (choice) {
            case 1 -> showOverdueItems();
            case 2 -> showMostBorrowedItems();
            case 3 -> showTaxReport();
            case 4 -> saveData();
            case 5 -> loadData();
            case 6 -> librarySystem.displayStatistics();
            default -> System.out.println("Invalid report menu.");
        }
    }

    private void showOverdueItems() {
        List<LibraryItem> overdueItems = librarySystem.getOverdueItems();
        if (overdueItems.isEmpty()) {
            System.out.println("No overdue items.");
            return;
        }

        System.out.println("OVERDUE ITEMS");
        for (LibraryItem item : overdueItems) {
            System.out.println(item.getItemId() + " - " + item.getTitle()
                    + " | Borrower: " + item.getCurrentBorrower().getName()
                    + " | Due: " + item.getReturnDueDate()
                    + " | Late days: " + item.getOverdueDays(LocalDate.now()));
        }
    }

    private void showMostBorrowedItems() {
        List<LibraryItem> items = librarySystem.getMostBorrowedItems(3);
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        System.out.println("MOST BORROWED ITEMS");
        for (LibraryItem item : items) {
            System.out.println(item.getItemId() + " - " + item.getTitle()
                    + " | Times borrowed: " + item.getTimesBorrowed());
        }
    }

    private void showTaxReport() {
        List<LibraryItem> items = librarySystem.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        for (LibraryItem item : items) {
            Taxable taxable = (Taxable) item;
            double tax = taxable.calculateTax();
            System.out.printf("%s - %s | Price: %.2f | Tax: %.2f | Total: %.2f%n",
                    item.getItemId(), item.getTitle(), item.getPrice(), tax, item.getPrice() + tax);
        }
    }

    private void saveData() {
        try {
            fileManager.saveLibraryData(librarySystem);
            System.out.println("Data saved to CSV successfully.");
        } catch (IOException exception) {
            System.out.println("Save failed: " + exception.getMessage());
        }
    }

    private void autoSave(String actionName) {
        try {
            fileManager.saveLibraryData(librarySystem);
            System.out.println("Auto-saved after " + actionName + ".");
        } catch (IOException exception) {
            System.out.println("Auto-save failed after " + actionName + ": " + exception.getMessage());
        }
    }

    private void loadData() {
        try {
            fileManager.loadLibraryData(librarySystem);
            if (librarySystem.ensureFixedLibrarianAccount()) {
                fileManager.saveLibraryData(librarySystem);
            }
            if (currentUser != null) {
                currentUser = librarySystem.findMemberById(currentUser.getMemberId());
            }
            System.out.println("Data loaded from CSV successfully.");
        } catch (IOException exception) {
            System.out.println("Load failed: " + exception.getMessage());
        }
    }

    private boolean exitApplication() {
        if (InputHelper.confirm(scanner, "Save data before exit")) {
            saveData();
        }
        System.out.println("Goodbye.");
        return false;
    }

    private MembershipStrategy selectMembershipStrategy() {
        int choice = InputHelper.readInt(scanner, "1. Basic  2. Student  3. Premium: ", 1, 3);
        return switch (choice) {
            case 2 -> new StudentMembershipStrategy();
            case 3 -> new PremiumMembershipStrategy();
            default -> new BasicMembershipStrategy();
        };
    }

    private boolean requireLibrarianAccess(String action) {
        if (currentUser != null && currentUser.isLibrarian()) {
            return true;
        }
        System.out.println("Only the librarian can " + action + ".");
        return false;
    }
}
