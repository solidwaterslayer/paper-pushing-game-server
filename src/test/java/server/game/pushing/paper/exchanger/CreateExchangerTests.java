package server.game.pushing.paper.exchanger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.Exchanger;
import server.game.pushing.paper.bank.Bank;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateExchangerTests {
    protected Exchanger exchanger;
    protected List<String> ledger;

    @BeforeEach
    protected void setUp() {
        exchanger = new Exchanger(new Bank());
        ledger = new ArrayList<>();
    }

    @Test
    protected void create_command_word_is_case_insensitive() {
        ledger.add("cReATE checking 10000000 0.1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Checking 10000000 0.00 0.10", actual.get(0));
    }

    @Test
    protected void create_command_can_not_have_less_than_4_arguments() {
        ledger.add("create savings 11110000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create savings 11110000", actual.get(0));
    }

    @Test
    protected void create_command_can_not_have_more_than_5_arguments() {
        ledger.add("create savings 11110000 1 1 1 1 1 1 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create savings 11110000 1 1 1 1 1 1 1", actual.get(0));
    }

    @Test
    protected void create_checking_command_must_have_4_arguments() {
        ledger.add("create checking 11110000 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Checking 11110000 0.00 1.00", actual.get(0));
    }

    @Test
    protected void create_checking_command_can_not_have_5_arguments() {
        ledger.add("create checking 11110000 1 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create checking 11110000 1 1", actual.get(0));
    }

    @Test
    protected void create_savings_command_must_have_4_arguments() {
        ledger.add("create savings 11110000 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Savings 11110000 0.00 1.00", actual.get(0));
    }

    @Test
    protected void create_savings_command_can_not_have_5_arguments() {
        ledger.add("create savings 11110000 1 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create savings 11110000 1 1", actual.get(0));
    }

    @Test
    protected void create_cd_command_can_not_have_4_arguments() {
        ledger.add("create cd 11110000 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create cd 11110000 1", actual.get(0));
    }

    @Test
    protected void create_cd_command_must_have_5_arguments() {
        ledger.add("create cd 11110000 1 1000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Cd 11110000 1000.00 1.00", actual.get(0));
    }

    @Test
    protected void create_command_account_type_can_be_checking() {
        ledger.add("create checking 01234567 0.1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Checking 01234567 0.00 0.10", actual.get(0));
    }

    @Test
    protected void create_command_account_type_can_be_savings() {
        ledger.add("create savings 01234567 0.1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Savings 01234567 0.00 0.10", actual.get(0));
    }

    @Test
    protected void create_command_account_type_can_be_cd() {
        ledger.add("create cd 01234567 0.1 2000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Cd 01234567 2000.00 0.10", actual.get(0));
    }

    @Test
    protected void create_command_account_type_must_be_checking_or_savings_or_cd() {
        ledger.add("create G*&) 01234567 0.1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create G*&) 01234567 0.1", actual.get(0));
    }

    @Test
    protected void create_command_account_type_is_case_insensitive() {
        ledger.add("create saViNgs 01234567 0.1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Savings 01234567 0.00 0.10", actual.get(0));
    }

    @Test
    protected void create_command_id_must_be_8_digits() {
        ledger.add("create cd -car 1.9 10000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create cd -car 1.9 10000", actual.get(0));
    }

    @Test
    protected void create_command_id_can_not_be_duplicate() {
        ledger.add("create savings 01234567 1.9");
        ledger.add("create cd 01234567 1.9 10000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(2, actual.size());
        assertEquals("Savings 01234567 0.00 1.90", actual.get(0));
        assertEquals("create cd 01234567 1.9 10000", actual.get(1));
    }

    @Test
    protected void create_command_apr_must_be_a_real_number() {
        ledger.add("create savings 01234567 6fF*^(");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create savings 01234567 6fF*^(", actual.get(0));
    }

    @Test
    protected void create_command_apr_can_not_be_less_than_0() {
        ledger.add("create savings 01234567 -2");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create savings 01234567 -2", actual.get(0));
    }

    @Test
    protected void create_command_apr_can_be_0() {
        ledger.add("create savings 01234567 0");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Savings 01234567 0.00 0.00", actual.get(0));
    }

    @Test
    protected void create_command_apr_can_be_between_0_and_10() {
        ledger.add("create savings 01234567 5");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Savings 01234567 0.00 5.00", actual.get(0));
    }

    @Test
    protected void create_command_apr_can_be_10() {
        ledger.add("create savings 01234567 10");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Savings 01234567 0.00 10.00", actual.get(0));
    }

    @Test
    protected void create_command_apr_can_not_be_greater_than_10() {
        ledger.add("create savings 01234567 11");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create savings 01234567 11", actual.get(0));
    }

    @Test
    protected void create_cd_command_balance_can_not_be_less_than_1000() {
        ledger.add("create cd 00000000 9 1");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create cd 00000000 9 1", actual.get(0));
    }

    @Test
    protected void create_cd_command_balance_can_be_1000() {
        ledger.add("create cd 00000000 9 1000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Cd 00000000 1000.00 9.00", actual.get(0));
    }

    @Test
    protected void create_cd_command_balance_can_be_between_1000_and_10000() {
        ledger.add("create cd 00000000 9 1001");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Cd 00000000 1001.00 9.00", actual.get(0));
    }

    @Test
    protected void create_cd_command_balance_can_be_10000() {
        ledger.add("create cd 00000000 9 10000");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("Cd 00000000 10000.00 9.00", actual.get(0));
    }

    @Test
    protected void create_cd_command_balance_can_not_be_greater_than_10000() {
        ledger.add("create cd 00000000 9 10001");

        List<String> actual = exchanger.exchange(ledger);

        assertEquals(1, actual.size());
        assertEquals("create cd 00000000 9 10001", actual.get(0));
    }
}
