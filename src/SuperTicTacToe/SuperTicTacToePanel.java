package SuperTicTacToe;

import SuperTicTacToe.AI.AI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SuperTicTacToePanel extends JFrame {
    //================================================================================
    // Instance Variables
    //================================================================================
    private JButton quitButton;
    private JPanel mainPanel;
    private JPanel gamePanel;
    private JButton undoButton;

    private JButton[][] board;
    private Cell[][] iBoard;
    private ImageIcon xIcon;
    private ImageIcon oIcon;
    private ImageIcon emptyIcon;

    private SuperTicTacToeGame game;
    private AI ai;

    //================================================================================
    // Button Action Listener
    //================================================================================
    private ActionListener buttonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String functionID = e.getActionCommand();
            switch(functionID) {
                case "fnc.Quit":
                    fncQuit();
                    break;
                case "fnc.Undo":
                    fncUndo();
                    break;
                default:
                    fncGameButton(functionID);
                    break;
            }
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public SuperTicTacToePanel(int size, int connections, char starter, boolean ai) {
        this.board = new JButton[size][size];
        this.xIcon = new ImageIcon("src/SuperTicTacToe/res/x.png");
        this.oIcon = new ImageIcon("src/SuperTicTacToe/res/o.png");
        this.emptyIcon = new ImageIcon("src/SuperTicTacToe/res/empty.png");
        this.game = new SuperTicTacToeGame(size, connections, starter);
        this.ai = new AI(this.game, starter == 'x' ? Cell.O : Cell.X);

        this.setTitle("Super TicTacToe");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(this.mainPanel);
        this.setResizable(false);

        this.gamePanel.setLayout(new GridLayout(size, size));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Border thickBorder = new LineBorder(Color.black, 1);
                this.board[i][j] = new JButton(this.emptyIcon);
                this.board[i][j].setActionCommand(i+","+j);
                this.board[i][j].setPreferredSize(new Dimension(50, 50));
                this.board[i][j].setBorder(thickBorder);
                this.board[i][j].addActionListener(this.buttonListener);
                this.gamePanel.add(this.board[i][j]);
            }
        }

        this.quitButton.addActionListener(this.buttonListener);
        this.undoButton.addActionListener(this.buttonListener);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    //================================================================================
    // Private Methods
    //================================================================================
    private void displayBoard() {
        this.iBoard = this.game.getBoard();
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                switch (this.iBoard[i][j]) {
                    case X:
                        this.board[i][j].setIcon(this.xIcon);
                        break;
                    case O:
                        this.board[i][j].setIcon(this.oIcon);
                        break;
                    default:
                        this.board[i][j].setIcon(this.emptyIcon);
                        break;
                }
            }
        }
    }

    private void finalizeGame() {
        String title;
        String msg;
        switch (this.game.getGameStatus()) {
            case CATS:
                title = "Tie";
                msg = "The game was a tie." +
                        "\nWould you like to play again?";
                break;
            case X_WON:
                title = "X Won";
                msg = "X won the game!" +
                        "\nWould you like to play again?";
                break;
            case O_WON:
                title = "O Won";
                msg = "O won the game!" +
                        "\nWould you like to play again?";
                break;
            default:
                title = "Game Error";
                msg = "There was an error processing the winner of the game." +
                        "\nWould you like ot play again?";
                break;
        }
        int userResponse = JOptionPane.showConfirmDialog(null,
                msg,
                title,
                JOptionPane.YES_NO_OPTION);

        switch (userResponse) {
            case 0:
                this.game.reset();
                if (this.ai != null)
                    this.ai.eraseMemory();
                displayBoard();
                break;
            default:
                System.exit(0);
                break;
        }
    }

    //================================================================================
    // Button Methods
    //================================================================================
    private void fncQuit() {
        int userResponse = JOptionPane.showConfirmDialog(null,
                "Are you sure you would like to quit?",
                "Are you sure?",
                JOptionPane.YES_NO_OPTION);
        if (userResponse == JOptionPane.YES_OPTION)
            System.exit(0);
    }

    private void fncUndo() {
        try {
            this.game.undo();
            displayBoard();
        } catch (UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(null,
                    "Unable to undo." +
                            "\n" + ex.getMessage(),
                    "Unable to Undo",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fncGameButton(String btnIndex) {
        try {
            String[] data = btnIndex.split(",");
            int row = Integer.parseInt(data[0]);
            int col = Integer.parseInt(data[1]);

            this.game.select(row, col);
            displayBoard();

            if (this.game.getGameStatus() != GameStatus.IN_PROGRESS) {
                finalizeGame();
            } else {
                //TEMPORARY AI TEST CODE
                if (this.ai != null) {
                    Point AIDecision = this.ai.think(this.iBoard);
                    this.game.select(AIDecision.x, AIDecision.y);
                    displayBoard();
                    if (this.game.getGameStatus() != GameStatus.IN_PROGRESS) {
                        finalizeGame();
                    }
                }
                //END TEMPORARY AI TEST CODE
            }
        } catch (UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(null,
                    "Unable to select cell." +
                            "\n" + ex.getMessage(),
                    "Selection Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(null,
                    "Unable to select cell." +
                            "\n" + ex.getMessage(),
                    "Selection Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "There was an error processing a move.",
                    "Processing Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}
