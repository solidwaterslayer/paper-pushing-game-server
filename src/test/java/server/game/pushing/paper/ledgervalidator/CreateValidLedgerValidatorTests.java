//package server.game.pushing.paper.ledgervalidator;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import server.game.pushing.paper.ledgervalidator.bank.Bank;
//import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class CreateValidLedgerValidatorTests {
//    protected LedgerValidator ledgerValidator;
//    protected List<String> ledger;
//    protected final String CHECKING_ID = "00000010";
//    protected final String SAVINGS_ID = "00000001";
//    protected final String CD_ID = "00000000";
//    protected final double APR = 5;
//    protected final double INITIAL_CD_BALANCE = 5000;
//
//    @BeforeEach
//    protected void setUp() {
//        ledgerValidator = new LedgerValidator(new Bank());
//        ledger = new ArrayList<>();
//    }
//
//    @Test
//    protected void ledger_validator_should_output_attributes_of_accounts_in_order() {
//        ledger.add(String.format("create checking %s %f", CHECKING_ID, APR));
//        ledger.add(String.format("create savings %s %f", SAVINGS_ID, APR));
//        ledger.add(String.format("create cd %s %f %f", CD_ID, APR, INITIAL_CD_BALANCE));
//
//        ledger = ledgerValidator.validate(ledger);
//
//        assertEquals(String.format("%s %s %.2f %.2f", AccountType.Checking, CHECKING_ID, APR, 0.0f).toLowerCase(), ledger.get(0));
//        assertEquals(String.format("%s %s %.2f %.2f", AccountType.Savings, SAVINGS_ID, APR, 0.0f).toLowerCase(), ledger.get(1));
//        assertEquals(String.format("%s %s %.2f %.2f", AccountType.CD, CD_ID, APR, INITIAL_CD_BALANCE).toLowerCase(), ledger.get(2));
//    }
//
//    @Test
//    protected void ledger_validator_when_transaction_type_create_is_missing_as_the_first_argument_should_output_invalid_inputs_in_order_last() {
//        ledger.add(String.format("create checking %s %f", CHECKING_ID, APR));
//        ledger.add(String.format("create savings %s %f", SAVINGS_ID, APR));
//        ledger.add(String.format("create cd %s %f %f", CD_ID, APR, INITIAL_CD_BALANCE));
//
//        ledger = ledgerValidator.validate(ledger);
//
//        assertEquals(String.format("%s %s %.2f %.2f", AccountType.Checking, CHECKING_ID, APR, 0.0f).toLowerCase(), ledger.get(0));
//        assertEquals(String.format("%s %s %.2f %.2f", AccountType.Savings, SAVINGS_ID, APR, 0.0f).toLowerCase(), ledger.get(1));
//        assertEquals(String.format("%s %s %.2f %.2f", AccountType.CD, CD_ID, APR, INITIAL_CD_BALANCE).toLowerCase(), ledger.get(2));
//        assertFalse(createValidator.handle(""));
//        assertFalse(createValidator.handle(" checking 00000000 0"));
//        assertFalse(createValidator.handle("nuke checking 00000000 0"));
//        assertTrue(createValidator.handle("create checking 00000000 0"));
//    }
//
//    @Test
//    protected void transaction_should_contain_a_account_type_as_the_second_argument() {
//        assertFalse(createValidator.handle("create"));
//        assertFalse(createValidator.handle("create  00000001 0.1"));
//        assertFalse(createValidator.handle("create g68G*(^ 00000001 0.1"));
//        assertTrue(createValidator.handle("create checking 00000000 0"));
//        assertTrue(createValidator.handle("create savings 00000001 0.1"));
//        assertTrue(createValidator.handle("create cd 00000010 0.2 1000"));
//    }
//
//    @Test
//    protected void transaction_should_contain_an_unique_8_digit_id_as_the_third_argument() {
//        bank.createSavings("00000000", 0);
//        assertFalse(createValidator.handle("create savings 00000000 0.1"));
//
//        assertFalse(createValidator.handle("create savings"));
//        assertFalse(createValidator.handle("create savings  0.1"));
//
//        assertFalse(createValidator.handle("create savings 48 0.1"));
//
//        assertFalse(createValidator.handle("create savings 7834972 0.1"));
//        assertTrue(createValidator.handle("create savings 05793729 0.1"));
//        assertFalse(createValidator.handle("create savings 783447992 0.1"));
//
//        assertFalse(createValidator.handle("create savings 973957845729385729375 0.1"));
//
//        assertFalse(createValidator.handle("create savings 8G73mU*) 0.1"));
//    }
//
//    @Test
//    protected void transaction_should_contain_an_apr_between_0_and_10_inclusive_as_the_fourth_argument() {
//        assertFalse(createValidator.handle("create cd 00000000  1000"));
//        assertFalse(createValidator.handle("create cd 00000000 78g& 1000"));
//
//
//        assertFalse(createValidator.handle("create cd 00000000 -10 1000"));
//
//        assertFalse(createValidator.handle("create cd 00000000 -1 1000"));
//        assertTrue(createValidator.handle("create cd 00000000 0 1000"));
//        assertTrue(createValidator.handle("create cd 00000000 1 1000"));
//
//        assertTrue(createValidator.handle("create cd 00000000 5 1000"));
//        assertTrue(createValidator.handle("create cd 00000000 6 1000"));
//
//        assertTrue(createValidator.handle("create cd 00000000 9 1000"));
//        assertTrue(createValidator.handle("create cd 00000000 10 1000"));
//        assertFalse(createValidator.handle("create cd 00000000 11 1000"));
//
//        assertFalse(createValidator.handle("create cd 00000000 20 1000"));
//    }
//
//    @Test
//    protected void transaction_when_account_type_is_cd_should_contain_an_initial_balance_between_1000_and_10000_inclusive_as_the_fifth_argument() {
//        assertFalse(createValidator.handle("create cd 00000000 0 g78*(uU"));
//
//
//        assertFalse(createValidator.handle("create cd 00000000 0 -20000"));
//        assertFalse(createValidator.handle("create cd 00000000 0 0"));
//
//
//        assertFalse(createValidator.handle("create cd 00000000 0 100"));
//
//        assertFalse(createValidator.handle("create cd 00000000 0 900"));
//        assertTrue(createValidator.handle("create cd 00000000 0 1000"));
//        assertTrue(createValidator.handle("create cd 00000000 0 1100"));
//
//        assertTrue(createValidator.handle("create cd 00000000 0 5000"));
//        assertTrue(createValidator.handle("create cd 00000000 0 6000"));
//
//        assertTrue(createValidator.handle("create cd 00000000 0 9000"));
//        assertTrue(createValidator.handle("create cd 00000000 0 10000"));
//        assertFalse(createValidator.handle("create cd 00000000 0 11000"));
//
//        assertFalse(createValidator.handle("create cd 00000000 0 20000"));
//    }
//
//    @Test
//    protected void transaction_should_be_case_insensitive() {
//        assertTrue(createValidator.handle("CrEaTe chECkIng 00000000 0"));
//        assertTrue(createValidator.handle("create saVINgs 00000001 0.1"));
//        assertTrue(createValidator.handle("creATe cd 00000010 0.2 1000"));
//    }
//
//    @Test
//    protected void transaction_should_be_possible_with_useless_additional_arguments() {
//        assertTrue(createValidator.handle("create checking 00000000 0 0 0 0 0 0 0 0 0"));
//        assertTrue(createValidator.handle("create savings 00000001 0.1 nuke now"));
//        assertTrue(createValidator.handle("create cd 00000010 0.2 1000 g7^G*8"));
//    }
//}
