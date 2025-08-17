package gradle;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerTest {
    private Player player;
    private Deck deckLeft;
    private Deck deckRight;
    private CardGame game;
    private CyclicBarrier barrier;

    @BeforeEach
    void setUp() {
        game = new CardGame();
        deckLeft = new Deck(1);
        deckRight = new Deck(2);
        barrier = new CyclicBarrier(1); // Simplified for single player test
        player = new Player(1, deckLeft, deckRight, game, barrier);
    }

    @Test
    void testAddCardToHand() {
        Card card = new Card(5);
        player.addCardToHand(card);

        assertTrue(player.getHand().contains(card), "Card should be added to player's hand.");
        assertEquals(1, player.getHand().size(), "Player's hand should have exactly one card.");
    }

    @Test
    void testAddCardToHandLimit() {
        // Add 4 cards to the hand
        for (int i = 1; i <= 4; i++) {
            player.addCardToHand(new Card(i));
        }

        // Attempt to add a 5th card and expect an exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            player.addCardToHand(new Card(5));
        });

        assertEquals("Cannot add more cards. The hand already contains 4 cards.", exception.getMessage());
    }

    @Test
    void testHasWonCondition() {
        // Add 4 identical cards to hand
        for (int i = 0; i < 4; i++) {
            player.addCardToHand(new Card(7));
        }

        assertTrue(player.hasWon(), "Player should win with 4 cards of the same value.");
    }

    @Test
    void testHasNotWonCondition() {
        // Add different cards to hand
        player.addCardToHand(new Card(1));
        player.addCardToHand(new Card(2));
        player.addCardToHand(new Card(3));
        player.addCardToHand(new Card(4));

        assertFalse(player.hasWon(), "Player should not win with different card values.");
    }

    @Test
    void testFindCardToDiscard() throws InterruptedException {
        // Add cards to the hand
        player.addCardToHand(new Card(1)); // Matches player ID
        player.addCardToHand(new Card(2));
        player.addCardToHand(new Card(3));
        player.addCardToHand(new Card(4));

        // Simulate the discard process by playing a turn
        deckLeft.addCard(new Card(5)); // Add card to left deck for the player to pick
        player.playTurn();

        assertEquals(4, player.getHand().size(), "Player's hand should still have 4 cards after a turn.");
        assertTrue(deckRight.getCards().size() > 0, "Player should have discarded a card to the right deck.");
        assertFalse(player.getHand().contains(new Card(2)), "Player should have discarded a card that doesn't match their ID.");
    }

    @Test
    void testInitialHandLogging() throws IOException {
        // Arrange: Add cards to the player's hand
        player.addCardToHand(new Card(1));
        player.addCardToHand(new Card(2));
        player.addCardToHand(new Card(3));
        player.addCardToHand(new Card(4));

        // Act: Log the initial hand
        player.logInitialHand();

        // Assert: Verify that the log file exists and contains the correct information
        File logFile = new File("player1_output.txt"); // Assuming the file is named this way
        assertTrue(logFile.exists(), "The initial hand log file should exist.");

        // Clean up the log file after the test
        logFile.delete();
    }

    @Test
    void testRunPlayer() {
        // Add cards to player's hand to simulate game start
        player.addCardToHand(new Card(7));
        player.addCardToHand(new Card(7));
        player.addCardToHand(new Card(7));
        player.addCardToHand(new Card(7));

        // Run the player logic
        Thread playerThread = new Thread(player);
        playerThread.start();

        try {
            playerThread.join();
        } catch (InterruptedException e) {
            fail("Player thread was interrupted unexpectedly.");
        }

        assertTrue(game.hasGameEnded(), "Game should end if a player wins.");
        assertEquals(player, game.getWinner(), "Player should be declared the winner.");
    }

}
