package gradle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;



/**
 * Represents a deck of cards with a unique identifier.
 * The deck provides synchronized methods for adding and removing cards,
 * ensuring thread safety when used in a multithreaded environment.
 */
public class Deck {

    /**
     * The unique identifier for this deck.
     */
    private final int deckId;

    /**
     * The queue that holds the cards in the deck.
     */
    private final Queue<Card> cards = new LinkedList<>();

    /**
     * Constructs a new {@code Deck} with the specified unique identifier.
     *
     * @param deckId the unique ID for this deck.
     */
    public Deck(int deckId) {
        this.deckId = deckId;
    }

    /**
     * Returns the unique identifier of this deck.
     *
     * @return the deck ID.
     */
    public int getDeckId() {
        return deckId;
    }

    /**
     * Adds a card to the deck. This method is synchronized to ensure thread safety.
     * Notifies waiting threads after a card is added.
     *
     * @param card the card to add to the deck.
     */
    public synchronized void addCard(Card card) {
        cards.add(card);
        notifyAll(); // Notify waiting threads that a card has been added
    }

    /**
     * Removes and returns a card from the deck.
     * If the deck is empty, this method will wait until a card is available.
     *
     * @return the removed card.
     * @throws InterruptedException if the thread is interrupted while waiting for a card.
     */
    public synchronized Card removeCard() throws InterruptedException {
        while (cards.isEmpty()) {
            wait(); // Wait until a card is added
        }
        return cards.poll();
    }

    /**
     * Returns a list of all cards currently in the deck.
     * The returned list is a copy to prevent external modification of the deck's internal state.
     *
     * @return a list of all cards in the deck.
     */
    public synchronized List<Card> getCards() {
        return new ArrayList<>(cards); // Return a copy of the cards to prevent external modification
    }

    /**
     * Writes the contents of the deck to a file.
     * The file is named {@code deck<deckId>_output.txt} and contains the IDs of the cards in the deck.
     * If an I/O error occurs, the exception is logged.
     */
    public synchronized void writeDeckContentsToFile() {
        String fileName = "deck" + deckId + "_output.txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("deck" + deckId + " contents: ");
            for (Card card : cards) {
                writer.write(card.getValue() + " ");
            }
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace(); // Consider logging instead
        }
    }
}
