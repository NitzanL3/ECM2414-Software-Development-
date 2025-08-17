package gradle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Represents a player in the card game. Each player interacts with two decks (left and right),
 * maintains a hand of cards, and participates in the game rounds.
 * The class implements the {@code Runnable} interface to allow concurrent execution.
 */
public class Player implements Runnable {
    private final int id;
    private final List<Card> hand = new ArrayList<>(4);
    private final Deck deckLeft;
    private final Deck deckRight;
    private volatile boolean hasWon = false;
    private final CardGame game;
    private static final int TURN_DELAY = 100;
    private final CyclicBarrier barrier; 

    /**
     * Constructs a new {@code Player}.
     *
     * @param id        the unique ID of the player.
     * @param deckLeft  the deck to the player's left.
     * @param deckRight the deck to the player's right.
     * @param game      the {@code CardGame} instance this player is part of.
     * @param barrier   the {@code CyclicBarrier} used to synchronize players between rounds.
     */
    public Player(int id, Deck deckLeft, Deck deckRight, CardGame game, CyclicBarrier barrier) {
        this.id = id;
        this.deckLeft = deckLeft;
        this.deckRight = deckRight;
        this.game = game;
        this.barrier = barrier;
    }

    /**
     * Returns the unique ID of the player.
     *
     * @return the player's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets a copy of the player's hand.
     * This returns a new list containing the same cards as the player's hand, 
     * preventing direct modification of the internal hand state.
     *
     * @return a copy of the player's hand.
     */
    public List<Card> getHand() {
        synchronized (hand) { 
            return new ArrayList<>(hand); 
        }
    }

    /**
     * Adds a card to the player's hand if it contains fewer than 4 cards.
     *
     * @param card the card to add.
     */
    public synchronized void addCardToHand(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add a null card to the hand.");
        }
    
        if (hand.size() < 4) {
            hand.add(card);
        } else {
            throw new IllegalStateException("Cannot add more cards. The hand already contains 4 cards.");
        }
    }
    

    /**
     * Checks if the player has won by having four cards of the same value in their hand.
     *
     * @return {@code true} if the player has won, {@code false} otherwise.
     */
    public boolean hasWon() {
        synchronized (hand) {
            Map<Integer, Integer> cardCounts = new HashMap<>();
            for (Card card : hand) {
                int cardValue = card.getValue();
                cardCounts.put(cardValue, cardCounts.getOrDefault(cardValue, 0) + 1);
                if (cardCounts.get(cardValue) == 4) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Logs the player's initial hand to a file.
     */
    public void logInitialHand() {
        synchronized (hand) {
            try (FileWriter writer = new FileWriter("player" + id + "_output.txt", true)) {
                writer.write("Player " + id + " initial hand: ");
                for (Card card : hand) {
                    writer.write(card.getValue() + " ");
                }
                writer.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds a card in the player's hand to discard.
     * Prefers cards that do not match the player's ID.
     *
     * @return the card to discard.
     */
    private Card findCardToDiscard() {
        synchronized (hand) {
            for (Card card : hand) {
                if (card.getValue() != id) {
                    return card;
                }
            }
            return hand.get(0); // Default to the first card if no other card is found
        }
    }

    /**
     * Executes a single turn for the player.
     * The player picks a card from the left deck, discards a card, and passes it to the right deck.
     *
     * @throws InterruptedException if the thread is interrupted during execution.
     */
    public void playTurn() throws InterruptedException {
        synchronized (game) {
            if (game.hasGameEnded()) return;
        }

        Card pickedCard;
        Card discardedCard;

        synchronized (deckLeft) {
            synchronized (hand) {
                synchronized (deckRight) {
                    pickedCard = deckLeft.removeCard();
                    hand.add(pickedCard);
                    discardedCard = findCardToDiscard();
                    hand.remove(discardedCard);
                    deckRight.addCard(discardedCard);
                    logAction(pickedCard, discardedCard);
                }
            }
        }
    }

    /**
     * Logs the player's actions during their turn to a file.
     *
     * @param pickedCard   the card picked from the left deck.
     * @param discardedCard the card discarded to the right deck.
     */
    private void logAction(Card pickedCard, Card discardedCard) {
        synchronized (hand) {
            try (FileWriter writer = new FileWriter("player" + id + "_output.txt", true)) {
                writer.write("Player " + id + " draws a " + pickedCard.getValue() + " from deck " + deckLeft.getDeckId() + "\n");
                writer.write("Player " + id + " discards a " + discardedCard.getValue() + " to deck " + deckRight.getDeckId() + "\n");
                writer.write("Player " + id + " current hand is " + handToString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a string representation of the player's hand.
     *
     * @return a string containing the card values in the player's hand.
     */
    private String handToString() {
        StringBuilder handString = new StringBuilder();
        for (Card card : hand) {
            handString.append(card.getValue()).append(" ");
        }
        return handString.toString().trim();
    }

    /**
     * Logs a win message for the player and records their final hand.
     */
    public void logWin() {
        try (FileWriter writer = new FileWriter("player" + id + "_output.txt", true)) {
            writer.write("Player " + id + " wins\n");
            writer.write("Player " + id + " exits\n");
            writer.write("Player " + id + " final hand: " + handToString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a loss message for the player, informed by the winning player.
     *
     * @param winner the player who won.
     */
    public void logLoss(Player winner) {
        synchronized (hand) {
            try (FileWriter writer = new FileWriter("player" + id + "_output.txt", true)) {
                writer.write("Player " + winner.getId() + " has informed player " + id + " that player " + winner.getId() + " has won\n");
                writer.write("Player " + id + " exits\n");
                writer.write("Player " + id + " hand: " + handToString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The main logic executed when the player thread runs.
     * Handles logging, playing rounds, and checking win conditions.
     */
    @Override
    public void run() {
        try {
            logInitialHand();
            if (checkImmediateWin()) return;

            while (!Thread.currentThread().isInterrupted() && !game.hasGameEnded()) {
                playRound();
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        } finally {
            game.playerFinished();
        }
    }

    /**
     * Checks if the player has won immediately based on their initial hand.
     *
     * @return {@code true} if the player has won, {@code false} otherwise.
     */
    private boolean checkImmediateWin() {
        if (hasWon()) {
            handleWin();
            return true;
        }
        return false;
    }

    /**
     * Executes a single round of the game for the player, including turn logic and synchronization.
     *
     * @throws InterruptedException   if the thread is interrupted during execution.
     * @throws BrokenBarrierException if the barrier is broken while waiting.
     */
    private void playRound() throws InterruptedException, BrokenBarrierException {
        playTurn();
        barrier.await();
        if (hasWon()) {
            handleWin();
        } else {
            Thread.sleep(TURN_DELAY);
        }
    }

    /**
     * Handles the actions for when the player wins the game.
     * Notifies other players and logs the win.
     */
    private void handleWin() {
        synchronized (game) {
            if (!game.hasGameEnded()) {
                game.declareWinner(this);
                logWin();
                notifyOtherPlayers();
            }
        }
    }

    /**
     * Notifies all other players that this player has won the game.
     */
    private void notifyOtherPlayers() {
        for (Player otherPlayer : game.getPlayers()) {
            if (otherPlayer != this) {
                otherPlayer.logLoss(this);
            }
        }
    }
}
