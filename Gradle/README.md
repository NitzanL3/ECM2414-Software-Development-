# Card Game Test Suite

This project contains unit tests for the **CardGame**, **Deck**, and **Player** classes. The tests are written using **JUnit 5** and managed with **Gradle**.

## Prerequisites

Before running the tests, ensure you have the following installed:

- **Java 8** or higher
- **Gradle** (either locally or using the wrapper)
- **JUnit 5** (included in the project dependencies)

## Setup

1. **Download the Project:**
    Unzip the contents of the `cardsTest.zip` file.

    run this in the terminal:

    - unzip cardsTest.zip
    
    - cd Gradle

2. **Ensure Gradle is installed:**
    You need to have Gradle installed to run the tests. You can install Gradle globally, or use the Gradle Wrapper which is included in the project.
   
    To check if Gradle is installed globally, run:
    
    - gradle - v
    
    If Gradle is not installed globally, you can use the wrapper that comes with the project.

## Running the Tests 

1. **Using Gradle in the Terminal:**
    Open a terminal in the project root directory (where the build.gradle file is located).
    
    Run the following command if you have gradle installed:

    - gradle test

    If you are using the rapper run the following on mac:

    - ./gradlew test

    Or this on windows:

    - gradlew.bat test

    These command will run all the tests in the project, including the test suite in CardGameTestSuite.

2. **Running tests on an IDE:**
    
    This project was designed on VScode:
    
    Open the test file or the test suite.
    
    Use the built-in testing features to run the tests.

    This will be different for different IDEs

## Test Results
    
1. **Successfull Result:**
    
    After running the tests, Gradle will display the results in the terminal. If all tests pass, you should see something like this:
    
    - BUILD SUCCESSFUL
    
    - 3 actionable tasks: 3 executed, 0 up-to-date

    This means all tests were executed successfully.
    
    If any tests fail, Gradle will show the details of the failures, including the failed test methods and the reasons for the failure (e.g., assertion errors).

2. **Warnings:**

    You may see warnings related to unchecked or unsafe operations during the test run.

    These warnings are often caused by the use of reflection in the code and can be safely ignored.

    The warnings are not critical and will not affect the correctness of the tests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.