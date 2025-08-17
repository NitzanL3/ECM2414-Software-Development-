package gradle;
/**
 * Represents a card with a specific value.
 * This class is immutable, meaning once a card is created, its value cannot be changed.
 */
public class Card {
    /**
     * The value of the card.
     */
    private final int value;

    /**
     * Constructs a new {@code Card} with the specified value.
     *
     * @param value the value of the card.
     */
    public Card(int value) {
        this.value = value;
    }

    /**
     * Returns the value of the card.
     *
     * @return the card's value.
     */
    public int getValue() {
        return value;
    }

}

