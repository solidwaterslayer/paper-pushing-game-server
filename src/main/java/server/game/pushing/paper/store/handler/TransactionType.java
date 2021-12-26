package server.game.pushing.paper.store.handler;

public enum TransactionType {
    Create, Deposit, Withdraw, Transfer, TimeTravel;

    @Override
    public String toString() {
        return String.join(" ", this.name().split("(?=[A-Z])"));
    }
}
