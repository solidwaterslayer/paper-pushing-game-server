//package server.game.pushing.paper.processor.transaction;
//
//public class TransferProcessorTests {
//    final String CHECKING_ID_0 = "00000000";
//    final String CHECKING_ID_1 = "10000000";
//
//    final String SAVINGS_ID_0 = "00000001";
//    final String SAVINGS_ID_1 = "10000001";
//
//    final String CD_ID = "00000010";
//
//    Bank bank;
//    CommandProcessor commandProcessor;
//
//    @BeforeEach
//    void setUp() {
//        bank = new Bank();
//        commandProcessor = new CommandProcessor(bank);
//
//        commandProcessor.process("create checking 00000000 0.1");
//        commandProcessor.process("create savings 00000001 0.1");
//
//        commandProcessor.process("create checking 10000000 0.1");
//        commandProcessor.process("create savings 10000001 0.1");
//
//        commandProcessor.process("create cd 00000010 0.1 1000");
//    }
//
//    @Test
//    void bank_can_transfer_from_checking_to_checking() {
//        commandProcessor.process("deposit 00000000 200");
//        commandProcessor.process("transfer 00000000 10000000 200");
//
//        assertEquals(0, bank.getAccount(CHECKING_ID_0).getBalance());
//        assertEquals(200, bank.getAccount(CHECKING_ID_1).getBalance());
//    }
//
//    @Test
//    void bank_can_transfer_from_checking_to_savings() {
//        commandProcessor.process("deposit 00000000 200");
//        commandProcessor.process("transfer 00000000 00000001 200");
//
//        assertEquals(0, bank.getAccount(CHECKING_ID_0).getBalance());
//        assertEquals(200, bank.getAccount(SAVINGS_ID_0).getBalance());
//    }
//
//    @Test
//    void bank_can_transfer_from_savings_to_checking() {
//        commandProcessor.process("deposit 00000001 400");
//        commandProcessor.process("transfer 00000001 10000000 200");
//
//        assertEquals(400 - 200, bank.getAccount(SAVINGS_ID_0).getBalance());
//        assertEquals(200, bank.getAccount(CHECKING_ID_1).getBalance());
//    }
//
//    @Test
//    void bank_can_transfer_from_savings_to_savings() {
//        commandProcessor.process("deposit 00000001 400");
//        commandProcessor.process("transfer 00000001 10000001 200");
//
//        assertEquals(400 - 200, bank.getAccount(SAVINGS_ID_0).getBalance());
//        assertEquals(200, bank.getAccount(SAVINGS_ID_1).getBalance());
//    }
//
//    @Test
//    void bank_can_transfer_from_cd_to_savings() {
//        commandProcessor.process("transfer 00000010 00000001 1100");
//
//        assertEquals(0, bank.getAccount(CD_ID).getBalance());
//        assertEquals(1000, bank.getAccount(SAVINGS_ID_0).getBalance());
//    }
//}
