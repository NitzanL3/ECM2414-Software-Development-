package gradle;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    CardGameTest.class,
    CardTest.class,
    DeckTest.class,
    PlayerTest.class
})

public class CardGameTestSuite {
    // No methods are needed. The suite will automatically run all tests in the selected classes.
}
