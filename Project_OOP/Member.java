package Project_OOP;

/**
 * STRATEGY PATTERN - Member Class
 *
 * Member now uses MembershipStrategy to determine behavior.
 * This demonstrates the Strategy Pattern in action!
 *
 * Benefits:
 * - Can change membership type at runtime (upgrade/downgrade)
 * - No need for if-else statements based on member type
 * - Easy to add new membership types
 * - Behavior is delegated to the strategy object
 */
public class Member {
    public static final String ROLE_MEMBER = "MEMBER";
    public static final String ROLE_LIBRARIAN = "LIBRARIAN";
    public static final String FIXED_LIBRARIAN_ID = "LIB001";
    public static final String DEFAULT_LIBRARIAN_PASSWORD = "admin123";
    public static final String DEFAULT_MEMBER_PASSWORD = "1234";

    private String memberId;
    private String memberName;
    private int borrowedCount;
    private MembershipStrategy membershipStrategy; // Strategy Pattern
    private String role;
    private String password;

    /**
     * Constructor with default BasicMembershipStrategy
     */
    public Member(String memberId, String name) {
        this(memberId, name, new BasicMembershipStrategy(), 0, ROLE_MEMBER, DEFAULT_MEMBER_PASSWORD);
    }

    /**
     * Constructor with custom MembershipStrategy
     */
    public Member(String memberId, String name, MembershipStrategy strategy) {
        this(memberId, name, strategy, 0, ROLE_MEMBER, DEFAULT_MEMBER_PASSWORD);
    }

    public Member(String memberId, String name, MembershipStrategy strategy, int borrowedCount) {
        this(memberId, name, strategy, borrowedCount, ROLE_MEMBER, DEFAULT_MEMBER_PASSWORD);
    }

    public Member(String memberId, String name, MembershipStrategy strategy, int borrowedCount, String role, String password) {
        this.memberId = memberId;
        this.memberName = name;
        this.borrowedCount = Math.max(0, borrowedCount);
        this.membershipStrategy = strategy;
        this.role = normalizeRole(role);
        this.password = (password == null || password.isBlank()) ? DEFAULT_MEMBER_PASSWORD : password;
    }

    public static Member createFixedLibrarian() {
        return new Member(
                FIXED_LIBRARIAN_ID,
                "Librarian",
                new PremiumMembershipStrategy(),
                0,
                ROLE_LIBRARIAN,
                DEFAULT_LIBRARIAN_PASSWORD
        );
    }

    // --- GETTERS & SETTERS ---

    public String getMemberName() {
        return memberName;
    }

    // For consistent naming with sample output
    public String getName() {
        return memberName;
    }

    public void setName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getBorrowedCount() {
        return borrowedCount;
    }

    public void setBorrowedCount(int borrowedCount) {
        this.borrowedCount = borrowedCount;
    }

    public MembershipStrategy getMembershipStrategy() {
        return membershipStrategy;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = normalizeRole(role);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
    }

    /**
     * STRATEGY PATTERN - Change membership at runtime!
     * This allows upgrading/downgrading membership dynamically.
     */
    public void setMembershipStrategy(MembershipStrategy membershipStrategy) {
        this.membershipStrategy = membershipStrategy;
    }

    /**
     * Uses Strategy Pattern to check borrow limit
     */
    public boolean canBorrow() {
        int limit = membershipStrategy.getBorrowLimit();

        if (membershipStrategy.hasUnlimitedBorrowing()) {
            return true;
        }

        return this.borrowedCount < limit;
    }

    public void incrementBorrowed() {
        this.borrowedCount++;
    }

    public void decrementBorrowed() {
        if (this.borrowedCount > 0) {
            this.borrowedCount--;
        }
    }

    /**
     * Uses Strategy Pattern to calculate late fee with discount
     */
    public double calculateLateFee(double baseFee) {
        return membershipStrategy.applyLateFeeDiscount(baseFee);
    }

    public String getMembershipType() {
        return membershipStrategy.getMembershipType();
    }

    public boolean isLibrarian() {
        return ROLE_LIBRARIAN.equalsIgnoreCase(role);
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    /**
     * Display member information including membership type
     */
    public void displayMemberInfo() {
        System.out.println("--- MEMBER INFORMATION ---");
        System.out.println("  ID: " + memberId);
        System.out.println("  Name: " + memberName);
        System.out.println("  Role: " + role);
        System.out.println("  Membership Type: " + membershipStrategy.getMembershipType());
        System.out.println("  Membership Cost: " + membershipStrategy.getMembershipCost() + " Baht");
        System.out.println("  Borrow Limit: " + (membershipStrategy.hasUnlimitedBorrowing() ? "Unlimited" : membershipStrategy.getBorrowLimit()));
        System.out.println("  Currently Borrowed: " + borrowedCount);
        System.out.println("  Loan Period: " + membershipStrategy.getLoanPeriodDays() + " days");
    }

    private String normalizeRole(String role) {
        if (ROLE_LIBRARIAN.equalsIgnoreCase(role)) {
            return ROLE_LIBRARIAN;
        }
        return ROLE_MEMBER;
    }
}
