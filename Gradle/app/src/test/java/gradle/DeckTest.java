package gradle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class DeckTest {

    @Test
    void testDeckId() {
        Deck deck = new Deck(1);
        assertEquals(1, deck.getDeckId(), "Deck ID should be 1.");
    }

    @Test
    void testAddAndRemoveCard() throws InterruptedException {
        Deck deck = new Deck(2);
        Card card = new Card(5);

        deck.addCard(card);
        assertEquals(card, deck.removeCard(), "The removed card should match the added card.");
    }

    @Test
    void testWriteDeckContentsToFile() throws Exception {
        Deck deck = new Deck(3);
        Card card = new Card(7);
        deck.addCard(card);

        Method method = Deck.class.getDeclaredMethod("writeDeckContentsToFile");
        method.setAccessible(true);
        method.invoke(deck);

        // You could verify the file contents here if needed.
    }

    @Test
    void testPrivateCardsField() throws Exception {
        Deck deck = new Deck(4);

        Field cardsField = Deck.class.getDeclaredField("cards");
        cardsField.setAccessible(true);

        Queue<Card> cards = (Queue<Card>) cardsField.get(deck);
        assertNotNull(cards, "The cards field should not be null.");
    }
}
