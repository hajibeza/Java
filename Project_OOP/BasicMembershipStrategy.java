package Project_OOP;

/**
 * STRATEGY PATTERN - BasicMembershipStrategy
 *
 * Standard membership for regular members.
 *
 * Benefits:
 * - Borrow Limit: 1 items
 * - Loan Period: 14 days
 * - Late Fee Discount: None (0%)
 * - Cost: FREE
 *
 * This is the default strategy for all new members.
 */
public class BasicMembershipStrategy implements MembershipStrategy {
    private static final int BORROW_LIMIT = 1;
    private static final int LOAN_PERIOD = 14;
    private static final double MEMBERSHIP_COST = 0.0; // Free

    @Override
    public int getBorrowLimit() {
        return BORROW_LIMIT;
    }

    @Override
    public int getLoanPeriodDays() {
        return LOAN_PERIOD;
    }

    @Override
    public double applyLateFeeDiscount(double baseFee) {
        // No discount for basic members
        return baseFee;
    }

    @Override
    public String getMembershipType() {
        return "Basic Member";
    }

    @Override
    public double getMembershipCost() {
        return MEMBERSHIP_COST;
    }

    @Override
    public boolean hasUnlimitedBorrowing() {
        return false;
    }
}
