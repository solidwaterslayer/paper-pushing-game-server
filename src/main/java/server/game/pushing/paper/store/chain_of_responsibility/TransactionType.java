package server.game.pushing.paper.store.chain_of_responsibility;

public enum TransactionType {
    Create, Deposit, Withdraw, Transfer, TimeTravel;

    @Override
    public String toString() {
        return String.join(" ", this.name().split("(?=[A-Z])"));
    }
}
