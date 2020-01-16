package SuperTicTacToe;

import javax.swing.*;

public class SuperTicTacToe {
    //================================================================================
    // Static Variables
    //================================================================================
    private static int size;
    private static int connections;
    private static char starter;
    private static boolean ai;

    //================================================================================
    // Main Method
    //================================================================================
    /*********************************************************************************
     * main
     *********************************************************************************
     * Asks the user for the desired board size, number of connections needed for a
     * win, and the starting player before starting a new game of Super Tic Tac Toe.
     ********************************************************************************/
    public static void main(String[] args) {
        size = askForSize();
        connections = askForConnections();
        starter = askForStart();


        SuperTicTacToePanel game = new SuperTicTacToePanel(size, connections, starter, ai);
    }

    //================================================================================
    // Private (Helper) Methods
    //================================================================================
    /*********************************************************************************
     * askForSize
     *********************************************************************************
     * Asks the user for the desired board size, which must be greater than 2 and less
     * than 15. If the user enters an invalid value, then the user is re-prompted to
     * enter a valid value.
     *
     * @return inputSize - The user input for the size of the game board.
     ********************************************************************************/
    private static int askForSize() {
        boolean tryAgain = true;
        int inputSize = -1;
        while (tryAgain) {
            try {
                String userInput = JOptionPane.showInputDialog(null,
                        "How big would you like the game board to be?" +
                                "\n(Greater than 2 and less than 15.)",
                        "Board Size",
                        JOptionPane.QUESTION_MESSAGE);
                //When cancel/X is clicked, null response is given. (Different from empty string.)
                if (userInput == null)
                    System.exit(0);

                inputSize = Integer.parseInt(userInput);

                if (inputSize > 2 && inputSize < 15)
                    tryAgain = false;
            } catch (Exception ex) {

            }
        }

        return inputSize;
    }

    /*********************************************************************************
     * askForConnections
     *********************************************************************************
     * Asks the user for the desired number of connections needed to win, which must
     * be greater than or equal to 3, and less than or equal to the game board size.
     * If the user enters an invalid value, then they are re-prompted to enter a valid
     * value.
     *
     * @return inputConnections - The user input for the number of connections needed
     * to win the game.
     ********************************************************************************/
    private static int askForConnections() {
        boolean tryAgain = true;
        int inputConnections = -1;
        while (tryAgain) {
            try {
                int minCons = 3;
                if (size < 3)
                    minCons = size;
                int maxCons = size;

                String userInput = JOptionPane.showInputDialog(null,
                        "How many connections are required to win?" +
                                "\n(Greater than or equal to "+minCons+", and less than or equal to "+maxCons+".)",
                        "Connections to Win",
                        JOptionPane.QUESTION_MESSAGE);
                //When cancel/X is clicked, null response is given. (Different from empty string.)
                if (userInput == null)
                    System.exit(0);

                inputConnections = Integer.parseInt(userInput);

                if (inputConnections >= minCons && inputConnections <= maxCons)
                    tryAgain = false;
            } catch (Exception ex) {

            }

        }

        return inputConnections;
    }

    /*********************************************************************************
     * askForStart
     *********************************************************************************
     * Asks the user to enter the desired starting player (X or O). If the user enters
     * an invalid value, then the default value, X, is used as the starting player.
     *
     * @return userChar - The character for the starting player ('x'/'o').
     ********************************************************************************/
    private static char askForStart() {
        char userChar = ' ';
        String userInput = JOptionPane.showInputDialog(null,
                "Which player would you like to start the game?" +
                        "\n(X or O)",
                "Starting Player",
                JOptionPane.QUESTION_MESSAGE);
        //When cancel/X is clicked, null response is given. (Different from empty string.)
        if (userInput == null)
            System.exit(0);

        if (userInput.length() > 0)
            userChar = userInput.toLowerCase().charAt(0);

        if (userChar != 'x'
            && userChar != 'o') {
            JOptionPane.showMessageDialog(null,
                    "The default value, \"X\", will go first.",
                    "Default Value Used",
                    JOptionPane.ERROR_MESSAGE);
            return 'x';
        }

        return userChar;
    }
}
