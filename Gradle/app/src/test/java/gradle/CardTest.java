package gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void testCardValue() {
        Card card = new Card(5);
        assertEquals(5, card.getValue(), "Card value should be 5.");
    }
}

