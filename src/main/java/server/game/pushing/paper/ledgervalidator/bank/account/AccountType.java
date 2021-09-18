package server.game.pushing.paper.ledgervalidator.bank.account;

public enum AccountType {
    Checking, Savings, CD;

    public static AccountType parseAccountType(String string) {
        for (AccountType accountType : AccountType.values()) {
            if (accountType.name().equalsIgnoreCase(string)) {
                return accountType;
            }
        }

        return null;
    }
}
