package Project_OOP;

public interface MembershipStrategy {
    int getBorrowLimit();
    int getLoanPeriodDays();
    double applyLateFeeDiscount(double baseFee);
    String getMembershipType();
    double getMembershipCost();
    boolean hasUnlimitedBorrowing();
}
