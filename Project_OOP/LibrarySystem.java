package Project_OOP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SINGLETON PATTERN - LibrarySystem
 *
 * Ensures there is only ONE instance of LibrarySystem throughout the application.
 */
public class LibrarySystem {
  // Singleton instance
  private static LibrarySystem instance;

  // Library data
  private List<LibraryItem> allItems;
  private List<Member> allMembers;
  private String libraryName;
  private String libraryLocation;

  /**
   * Private constructor - Prevents direct instantiation.
   * Only getInstance() can create the instance.
   */
  private LibrarySystem() {
    this.allItems = new ArrayList<>();
    this.allMembers = new ArrayList<>();
    this.libraryName = "City Central Library";
    this.libraryLocation = "123 Main Street, Downtown";
  }

  /**
   * SINGLETON METHOD: Thread-safe lazy initialization
   */
  public static synchronized LibrarySystem getInstance() {
    if (instance == null) {
      instance = new LibrarySystem();
    }
    return instance;
  }

  public List<LibraryItem> getAllItems() {
    return new ArrayList<>(allItems);
  }

  public List<Member> getAllMembers() {
    return new ArrayList<>(allMembers);
  }

  public void clearData() {
    allItems.clear();
    allMembers.clear();
  }

  public boolean hasAnyData() {
    return !allItems.isEmpty() || !allMembers.isEmpty();
  }

  // Library Operations
  public boolean addItem(LibraryItem item) {
    if (item == null || findItemById(item.getItemId()) != null || hasDuplicateItem(item, null)) {
      return false;
    }
    allItems.add(item);
    return true;
  }

  public boolean addMember(Member member) {
    if (member == null || findMemberById(member.getMemberId()) != null || hasDuplicateMemberName(member.getName(), null)) {
      return false;
    }
    allMembers.add(member);
    return true;
  }

  public Member authenticateUser(String name, String password) {
    Member member = findMemberByNameExact(name);
    if (member == null || password == null || !member.authenticate(password)) {
      return null;
    }
    return member;
  }

  public boolean ensureFixedLibrarianAccount() {
    Member librarian = findMemberById(Member.FIXED_LIBRARIAN_ID);
    boolean changed = false;

    if (librarian == null) {
      addMember(Member.createFixedLibrarian());
      return true;
    }

    if (!librarian.isLibrarian()) {
      librarian.setRole(Member.ROLE_LIBRARIAN);
      changed = true;
    }
    if (!(librarian.getMembershipStrategy() instanceof PremiumMembershipStrategy)) {
      librarian.setMembershipStrategy(new PremiumMembershipStrategy());
      changed = true;
    }
    if (librarian.getPassword() == null || librarian.getPassword().isBlank()) {
      librarian.setPassword(Member.DEFAULT_LIBRARIAN_PASSWORD);
      changed = true;
    }

    return changed;
  }

  public String generateNextMemberId() {
    int maxNumber = 0;

    for (Member member : allMembers) {
      String memberId = member.getMemberId();
      if (memberId == null) {
        continue;
      }

      String normalizedId = memberId.trim().toUpperCase();
      if (!normalizedId.startsWith("M")) {
        continue;
      }

      try {
        int currentNumber = Integer.parseInt(normalizedId.substring(1));
        maxNumber = Math.max(maxNumber, currentNumber);
      } catch (NumberFormatException ignored) {
      }
    }

    return String.format("M%03d", maxNumber + 1);
  }

  public String generateNextItemId(String itemType) {
    String prefix = "B";
    if ("EBook".equalsIgnoreCase(itemType)) {
      prefix = "E";
    }

    int maxNumber = 0;
    for (LibraryItem item : allItems) {
      String itemId = item.getItemId();
      if (itemId == null) {
        continue;
      }

      String normalizedId = itemId.trim().toUpperCase();
      if (!normalizedId.startsWith(prefix)) {
        continue;
      }

      try {
        int currentNumber = Integer.parseInt(normalizedId.substring(1));
        maxNumber = Math.max(maxNumber, currentNumber);
      } catch (NumberFormatException ignored) {
      }
    }

    return String.format("%s%03d", prefix, maxNumber + 1);
  }

  public int getTotalItems() {
    return allItems.size();
  }

  public int getTotalMembers() {
    return allMembers.size();
  }

  public int getAvailableItemsCount() {
    return (int) allItems.stream().filter(LibraryItem::isAvailable).count();
  }

  public int getBorrowedItemsCount() {
    return (int) allItems.stream().filter(item -> !item.isAvailable()).count();
  }

  public List<LibraryItem> getOverdueItems() {
    return allItems.stream()
        .filter(LibraryItem::isOverdue)
        .collect(Collectors.toList());
  }

  public List<LibraryItem> getMostBorrowedItems(int limit) {
    return allItems.stream()
        .sorted(Comparator.comparingInt(LibraryItem::getTimesBorrowed).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  public List<LibraryItem> getBorrowedItems() {
    return allItems.stream()
        .filter(LibraryItem::isBorrowed)
        .collect(Collectors.toList());
  }

  public List<LibraryItem> getBorrowedItemsByMember(String memberId) {
    if (memberId == null || memberId.isBlank()) {
      return new ArrayList<>();
    }

    return allItems.stream()
        .filter(LibraryItem::isBorrowed)
        .filter(item -> item.getCurrentBorrower() != null
            && item.getCurrentBorrower().getMemberId().equalsIgnoreCase(memberId))
        .collect(Collectors.toList());
  }

  /**
   * Display library statistics
   */
  public void displayStatistics() {
    System.out.println("\n" + "=".repeat(60));
    System.out.println("LIBRARY SYSTEM STATISTICS");
    System.out.println("=".repeat(60));
    System.out.println("Library: " + libraryName);
    System.out.println("Location: " + libraryLocation);
    System.out.println("STATISTICS:");
    System.out.println("Total Items: " + getTotalItems());
    System.out.println("Available Items: " + getAvailableItemsCount());
    System.out.println("Borrowed Items: " + getBorrowedItemsCount());
    System.out.println("Total Members: " + getTotalMembers());
  }

  public LibraryItem findItemById(String itemId) {
    return allItems.stream()
        .filter(item -> item.getItemId().equalsIgnoreCase(itemId))
        .findFirst()
        .orElse(null);
  }

  public LibraryItem findItemByTitle(String title) {
    return allItems.stream()
        .filter(item -> item.getTitle().equalsIgnoreCase(title))
        .findFirst()
        .orElse(null);
  }

  public List<LibraryItem> findItems(String keyword) {
    return findItems(keyword, true);
  }

  public List<LibraryItem> findItems(String keyword, boolean partialMatch) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
    if (normalizedKeyword.isEmpty()) {
      return getAllItems();
    }

    return allItems.stream()
        .filter(item -> matches(item.getItemId(), normalizedKeyword, partialMatch)
            || matches(item.getTitle(), normalizedKeyword, partialMatch)
            || matches(item.getAuthor(), normalizedKeyword, partialMatch)
            || matches(item.getIsbn(), normalizedKeyword, partialMatch))
        .collect(Collectors.toList());
  }

  /**
   * Find member by ID
   */
  public Member findMemberById(String memberId) {
    return allMembers.stream()
        .filter(member -> member.getMemberId().equals(memberId))
        .findFirst()
        .orElse(null);
  }

  public List<Member> findMembersByName(String keyword) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
    return allMembers.stream()
        .filter(member -> member.getName().toLowerCase().contains(normalizedKeyword)
            || member.getMemberId().toLowerCase().contains(normalizedKeyword))
        .collect(Collectors.toList());
  }

  public Member findMemberByNameExact(String name) {
    if (name == null) {
      return null;
    }

    String normalizedName = name.trim();
    return allMembers.stream()
        .filter(member -> member.getName().trim().equalsIgnoreCase(normalizedName))
        .findFirst()
        .orElse(null);
  }

  public boolean removeMember(String memberId) {
    Member member = findMemberById(memberId);
    if (member == null || member.getBorrowedCount() > 0) {
      return false;
    }
    return allMembers.remove(member);
  }

  public boolean removeItem(String itemId) {
    LibraryItem item = findItemById(itemId);
    if (item == null || item.isBorrowed()) {
      return false;
    }
    return allItems.remove(item);
  }

  public boolean borrowItem(String itemId, String memberId) {
    LibraryItem item = findItemById(itemId);
    Member member = findMemberById(memberId);
    if (item == null || member == null) {
      return false;
    }
    return item.checkOut(member);
  }

  public boolean hasDuplicateMemberName(String name, String excludedMemberId) {
    if (name == null || name.isBlank()) {
      return false;
    }

    return allMembers.stream()
        .filter(member -> excludedMemberId == null || !member.getMemberId().equalsIgnoreCase(excludedMemberId))
        .anyMatch(member -> member.getName().trim().equalsIgnoreCase(name.trim()));
  }

  public boolean hasDuplicateItem(LibraryItem candidate, String excludedItemId) {
    if (candidate == null) {
      return false;
    }

    return allItems.stream()
        .filter(item -> excludedItemId == null || !item.getItemId().equalsIgnoreCase(excludedItemId))
        .anyMatch(item -> isSameItemRecord(item, candidate));
  }

  private boolean isSameItemRecord(LibraryItem existingItem, LibraryItem candidate) {
    boolean sameIsbn = existingItem.getIsbn() != null
        && candidate.getIsbn() != null
        && existingItem.getIsbn().trim().equalsIgnoreCase(candidate.getIsbn().trim());

    boolean sameTitleAndAuthor = existingItem.getTitle() != null
        && candidate.getTitle() != null
        && existingItem.getAuthor() != null
        && candidate.getAuthor() != null
        && existingItem.getTitle().trim().equalsIgnoreCase(candidate.getTitle().trim())
        && existingItem.getAuthor().trim().equalsIgnoreCase(candidate.getAuthor().trim());

    return sameIsbn || sameTitleAndAuthor;
  }

  public void seedSampleData() {
    if (hasAnyData()) {
      return;
    }

    Member librarian = Member.createFixedLibrarian();
    Member basicMember = new Member("M001", "Somsak", new BasicMembershipStrategy(), 0, Member.ROLE_MEMBER, "1111");
    Member studentMember = new Member("M002", "Suda", new StudentMembershipStrategy(), 0, Member.ROLE_MEMBER, "2222");
    Member premiumMember = new Member("M003", "Somchai", new PremiumMembershipStrategy(), 0, Member.ROLE_MEMBER, "3333");

    addMember(librarian);
    addMember(basicMember);
    addMember(studentMember);
    addMember(premiumMember);

    PhysicalBook book1 = new PhysicalBook("B001", "Java Programming", "John Smith", "978-0134685991", 450.0, "A1-04");
    PhysicalBook book2 = new PhysicalBook("B002", "Clean Code", "Robert Martin", "978-0132350884", 520.0, "B2-15");
    PhysicalBook book3 = new PhysicalBook("B003", "Design Patterns", "Gang of Four", "978-0201633612", 680.0, "A3-22");
    EBook ebook1 = new EBook("E001", "Effective Java", "Joshua Bloch", "978-0134685991", 99.0,
        "https://library.ebooks.com/effective-java.pdf", 5.2);
    EBook ebook2 = new EBook("E002", "Python Crash Course", "Eric Matthes", "978-1593279288", 109.0,
        "https://library.ebooks.com/python-crash.pdf", 8.7);

    addItem(book1);
    addItem(book2);
    addItem(book3);
    addItem(ebook1);
    addItem(ebook2);

    book1.checkOut(studentMember);
    book1.setReturnDueDate(java.time.LocalDate.now().minusDays(4));
    book1.setTimesBorrowed(4);

    ebook1.checkOut(premiumMember);
    ebook1.setReturnDueDate(java.time.LocalDate.now().plusDays(6));
    ebook1.setTimesBorrowed(6);

    book2.setTimesBorrowed(2);
    book3.setTimesBorrowed(1);
    ebook2.setTimesBorrowed(3);
  }

  private boolean matches(String source, String keyword, boolean partialMatch) {
    if (source == null) {
      return false;
    }

    String normalizedSource = source.toLowerCase();
    if (partialMatch) {
      return normalizedSource.contains(keyword);
    }
    return normalizedSource.equals(keyword);
  }
}
