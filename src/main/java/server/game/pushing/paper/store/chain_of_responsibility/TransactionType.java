package server.game.pushing.paper.store.chain_of_responsibility;

public enum TransactionType {
    Create, Deposit, Withdraw, Transfer, PassTime;

    @Override
    public String toString() {
        return String.join(" ", this.name().split("(?=[A-Z])"));
    }
}
