package server.game.pushing.paper.ledgervalidator.transactionchain;

public enum TransactionType {
    Create, Deposit, Withdraw, Transfer, PassTime;

    public String[] split() {
        return this.toString().split("(?=[A-Z])");
    }
}
