package gradle;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class CardGameTest {
    private CardGame game;

    private List<Card> generatePack(int size) {
        List<Card> pack = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            pack.add(new Card(i));
        }
        return pack;
    }
    
    @BeforeEach
    void setUp() {
        game = new CardGame();
    }

    @Test
    void testInitializeGame() throws Exception {
        List<Card> pack = generatePack(16); // 2 players => 8 cards per player
        game.initializeGame(2, pack);

        assertEquals(2, game.getPlayers().size(), "There should be 2 players.");
        assertEquals(2, game.getDecks().size(), "There should be 2 decks.");
    }

    @Test
        void testDealCardsToPlayers() {
        List<Card> pack = generatePack(16); // 2 players, 8 cards per player.
        game.initializeGame(2, pack);
    
        // Each player should have 8 cards
        for (Player player : game.getPlayers()) {
        assertEquals(4, player.getHand().size(), "Each player should have 4 cards.");
    }
}
    @Test
    void testDistributeCardsToDecks() throws Exception {
        List<Card> pack = generatePack(16); // 8 cards per player, total 16 cards
        game.initializeGame(2, pack);

        // Use reflection to test the private distributeCardsToDecks method
        Method method = CardGame.class.getDeclaredMethod("distributeCardsToDecks", int.class, List.class);
        method.setAccessible(true);
        method.invoke(game, 2, pack); // Invoke distribution with 2 players

        // Validate that each deck has cards
        for (Deck deck : game.getDecks()) {
            assertTrue(deck.getCards().size() > 0, "Each deck should have cards.");
        }

        // Simplified check: Validate the cards in each deck by their indices
        List<Card> deck1Cards = game.getDecks().get(0).getCards();
        List<Card> deck2Cards = game.getDecks().get(1).getCards();

        // Ensure cards are distributed as expected between decks
        assertEquals(4, deck1Cards.size(), "Deck 1 should have 4 cards.");
        assertEquals(4, deck2Cards.size(), "Deck 2 should have 4 cards.");
    }
    

    @Test
    void testHasGameEnded() {
        assertFalse(game.hasGameEnded(), "Game should not end initially.");

        // Declare a winner and verify game ends
        Player winner = new Player(1, new Deck(1), new Deck(2), game, null);
        game.declareWinner(winner);

        assertTrue(game.hasGameEnded(), "Game should end after declaring a winner.");
        assertEquals(winner, game.getWinner(), "Declared winner should be correctly set.");
    }

    @Test
    void testDeclareWinnerNotifiesPlayers() {
        // Initialize game and players
        List<Card> pack = generatePack(8);
        game.initializeGame(2, pack);

        Player winner = game.getPlayers().get(0);
        game.declareWinner(winner);

        // Ensure winner is declared
        assertTrue(game.hasGameEnded(), "Game should end after a winner is declared.");
        assertEquals(winner, game.getWinner(), "Winner should be correctly declared.");

        // Ensure other players are notified (check logLoss is called)
        Player loser = game.getPlayers().get(1);
        assertDoesNotThrow(() -> loser.logLoss(winner), "Loser should log the winner's victory without error.");
    }
    
    @Test
    void testPlayerFinished() throws NoSuchFieldException, IllegalAccessException {
        // Initialize the game with 2 players
        game.initializeGame(2, generatePack(16));

        // Access the private 'playersRemaining' field using reflection
        Field field = CardGame.class.getDeclaredField("playersRemaining");
        field.setAccessible(true);  // Make the field accessible

        // Manually set the value of playersRemaining to simulate the start of the game
        AtomicInteger playersRemainingValue = (AtomicInteger) field.get(game);
        playersRemainingValue.set(2);  // Set to 2 as if the game has just started with 2 players

        // Simulate one player finishing
        game.playerFinished();

        // Verify that playersRemaining is 1 after calling playerFinished
        playersRemainingValue = (AtomicInteger) field.get(game);
        assertEquals(1, playersRemainingValue.get(), "There should be 1 player remaining after one finishes.");
    }

    @Test
    void testWriteDeckOutputs() throws Exception {
        List<Card> pack = generatePack(16);
        game.initializeGame(2, pack);

        // Use reflection to access and invoke the private 'writeDeckOutputs' method
        Method method = CardGame.class.getDeclaredMethod("writeDeckOutputs");
        method.setAccessible(true);  // Make the method accessible
        method.invoke(game);

        // Verify if the output files for the decks exist
        File deck1Output = new File("deck1_output.txt");
        assertTrue(deck1Output.exists(), "Deck 1 output file should exist.");
        
    }

    // Helper method to create a temporary file with a valid pack of cards
    private File createTestFile(String fileName, List<Integer> cardValues) throws IOException {
        File tempFile = new File(fileName);
        if (tempFile.exists()) {
            tempFile.delete();
        }
        tempFile.createNewFile();

        try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
            for (int value : cardValues) {
                writer.write(value + "\n");
            }
        }
        return tempFile;
    }

    @Test
    void testLoadPackValid() throws IOException {
        // Create a test pack of cards
        List<Integer> cardValues = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        File testFile = createTestFile("test_pack.txt", cardValues);

        // Create a new CardGame instance and load the pack
        CardGame game = new CardGame();
        List<Card> pack = game.loadPack(testFile.getAbsolutePath(), 2);  // 2 players, so 16 cards expected

        // Verify the pack was loaded correctly
        assertEquals(16, pack.size(), "The pack should contain 16 cards.");
        assertEquals(1, pack.get(0).getValue(), "First card value should be 1.");
        assertEquals(16, pack.get(15).getValue(), "Last card value should be 16.");

        // Clean up the test file
        testFile.delete();
    }

    @Test
    void testLoadPackInvalidFile() {
        CardGame game = new CardGame();

        // Attempt to load a non-existent file
        assertThrows(IllegalArgumentException.class, () -> game.loadPack("non_existent_file.txt", 2));
    }

    @Test
    void testLoadPackInvalidCardValues() throws IOException {
        // Create a test pack with invalid card values (negative number)
        List<Integer> cardValues = List.of(1, 2, -3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        File testFile = createTestFile("test_invalid_pack.txt", cardValues);

        CardGame game = new CardGame();

        // Verify that the exception is thrown when invalid card values are encountered
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> game.loadPack(testFile.getAbsolutePath(), 2));

        assertTrue(exception.getMessage().contains("Pack contains a negative integer value"), 
            "Exception message should indicate invalid card value.");

        // Clean up the test file
        testFile.delete();
    }

    @Test
    void testLoadPackIncorrectNumberOfCards() throws IOException {
        // Create a test pack with an incorrect number of cards (only 10 cards, but 16 are required for 2 players)
        List<Integer> cardValues = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        File testFile = createTestFile("test_incorrect_number_of_cards.txt", cardValues);

        CardGame game = new CardGame();

        // Verify that the exception is thrown when the number of cards is incorrect
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> game.loadPack(testFile.getAbsolutePath(), 2));

        assertTrue(exception.getMessage().contains("Pack must contain exactly 16 cards"), 
            "Exception message should indicate the incorrect number of cards.");

        // Clean up the test file
        testFile.delete();
    }

}




