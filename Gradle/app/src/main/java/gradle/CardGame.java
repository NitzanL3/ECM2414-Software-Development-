package gradle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Represents a card game involving players and decks.
 * The game is played in turns, where players pick and discard cards
 * until a winner is determined.
 */
public class CardGame {
    private final List<Player> players = new ArrayList<>();
    final List<Deck> decks = new ArrayList<>();
    private final AtomicBoolean hasGameEnded = new AtomicBoolean(false);
    private final AtomicInteger playersRemaining = new AtomicInteger();
    private volatile Player winner = null;

    /**
     * Initializes the game with the specified number of players and card pack.
     *
     * @param n    the number of players
     * @param pack the pack of cards to be used
     */
    public void initializeGame(int n, List<Card> pack) {
        createDecks(n);
        CyclicBarrier barrier = createBarrier(n);
        createPlayers(n, barrier);
        dealCardsToPlayers(n, pack);
        distributeCardsToDecks(n, pack);
    }

    // Helper methods

    /**
     * Creates decks for the game.
     *
     * @param n the number of decks to create
     */
    private void createDecks(int n) {
        for (int i = 1; i <= n; i++) {
            decks.add(new Deck(i));
        }
    }

    /**
     * Creates a CyclicBarrier for synchronizing player turns.
     *
     * @param n the number of players
     * @return the created CyclicBarrier
     */
    private CyclicBarrier createBarrier(int n) {
        return new CyclicBarrier(n, () -> {});
    }

    /**
     * Creates players and associates them with adjacent decks.
     *
     * @param n       the number of players
     * @param barrier the CyclicBarrier for synchronization
     */
    private void createPlayers(int n, CyclicBarrier barrier) {
        for (int i = 1; i <= n; i++) {
            Deck leftDeck = decks.get((i - 1) % n);
            Deck rightDeck = decks.get(i % n);
            Player player = new Player(i, leftDeck, rightDeck, this, barrier);
            players.add(player);
        }
    }

    /**
     * Deals cards to players from the pack.
     *
     * @param n    the number of players
     * @param pack the pack of cards
     */
    private void dealCardsToPlayers(int n, List<Card> pack) {
        int playerIndex = 0;
        for (int i = 0; i < 4 * n; i++) {
            players.get(playerIndex).addCardToHand(pack.remove(0));
            playerIndex = (playerIndex + 1) % n;
        }
    }

    /**
     * @return an unmodifiable list of decks in the game
     */
    public List<Deck> getDecks() {
        return Collections.unmodifiableList(decks);
    }

    /**
     * Distributes remaining cards to the decks.
     *
     * @param n    the number of decks
     * @param pack the remaining pack of cards
     */
    private void distributeCardsToDecks(int n, List<Card> pack) {
        int deckIndex = 0;
        while (!pack.isEmpty()) {
            decks.get(deckIndex).addCard(pack.remove(pack.size() - 1));
            deckIndex = (deckIndex + 1) % n;
        }
    }

    /**
     * @return a list of players in the game
     */
    public List<Player> getPlayers() {
    return Collections.unmodifiableList(players);
    }


    /**
     * Checks if the game has ended.
     *
     * @return true if the game has ended, false otherwise
     */
    public boolean hasGameEnded() {
        return hasGameEnded.get();
    }

    /**
     * Called when a player finishes their execution.
     * Handles the end of the game if all players are finished.
     */
    public void playerFinished() {
        int remaining = playersRemaining.decrementAndGet();
        if (hasGameEnded.get() && remaining <= 0) {
            synchronized (this) {
                if (hasGameEnded.get()) {
                    writeDeckOutputs();
                }
            }
        }
    }

    /**
     * Starts the card game by running player threads.
     */
    public void startGame() {
        playersRemaining.set(players.size());
        ExecutorService executorService = Executors.newFixedThreadPool(players.size());

        for (Player player : players) {
            executorService.execute(player);
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        } finally {
            if (hasGameEnded.get()) {
                writeDeckOutputs();
            }
        }
    }

    /**
     * Loads a pack of cards from a file.
     *
     * @param filePath the path to the pack file
     * @param n        the number of players
     * @return the loaded pack of cards
     */
    public List<Card> loadPack(String filePath, int n) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        
        List<Card> pack = new ArrayList<>();
        File file = new File(filePath);

        try (Scanner scanner = new Scanner(file)) {
            int lineNumber = 0;
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    int value = Integer.parseInt(line);
                    if (value < 0) {
                        throw new IllegalArgumentException("Pack contains a negative integer value at line " + lineNumber + ".");
                    }
                    pack.add(new Card(value));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid value at line " + lineNumber + ": \"" + line + "\"");
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found: " + filePath, e);
        }

        if (pack.size() != 8 * n) {
            throw new IllegalArgumentException("Pack must contain exactly " + (8 * n) + " cards. Found " + pack.size());
        }

        return pack;
    }

    /**
     * Main method for running the game.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CardGame game = new CardGame();

        int n = 0;
        while (true) {
            try {
                System.out.print("Enter the number of players: ");
                n = Integer.parseInt(scanner.nextLine().trim());
                if (n <= 0) {
                    System.out.println("Number of players must be greater than 0.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a positive integer.");
            }
        }

        List<Card> pack = null;
        while (pack == null) {
            try {
                System.out.print("Enter the path to the pack file: ");
                String filePath = scanner.nextLine().trim();
                pack = game.loadPack(filePath, n);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please provide a valid pack file.");
            }
        }

        try {
            game.initializeGame(n, pack);
            game.startGame();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during the game: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * @return the winner of the game
     */
    public synchronized Player getWinner() {
        return winner;
    }

    /**
     * Declares the winner of the game.
     *
     * @param player the winning player
     */
    public synchronized void declareWinner(Player player) {
        if (!hasGameEnded.get()) {
            hasGameEnded.set(true);
            winner = player;
            System.out.println("Player " + player.getId() + " wins!");
            System.out.println("The game has ended.");

            for (Player p : players) {
                Thread t = new Thread(p);
                t.interrupt();
            }
        }
    }
    
    /**
     * Writes the contents of all decks to their respective files.
     */
    private void writeDeckOutputs() {
        for (Deck deck : decks) {
            deck.writeDeckContentsToFile();
        }
    }
}
