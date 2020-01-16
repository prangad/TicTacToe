package SuperTicTacToe;

import java.awt.*;
import java.util.ArrayList;

public class SuperTicTacToeGame {
    //================================================================================
    // Private Variables
    //================================================================================
    private Cell[][] board;
    private GameStatus status;
    private char currentPlayer;
    private int connections;

    private ArrayList<Point> history;

    //================================================================================
    // Constructors
    //================================================================================
    /*********************************************************************************
     * SuperTicTacToeGame - Constructor
     *********************************************************************************
     * Starts a new game of Super Tic Tac Toe using the user-selected game board size,
     * connections to win, and starting player.
     *
     * @param size - The size of the game board.
     * @param connections - The number of connections to win the game.
     * @param starter - The starting player.
     ********************************************************************************/
    public SuperTicTacToeGame(int size, int connections, char starter) {
        this.status = GameStatus.IN_PROGRESS;
        this.board = new Cell[size][size];
        this.history = new ArrayList<Point>();

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                this.board[i][j] = Cell.EMPTY;

        this.currentPlayer = starter;
        this.connections = connections;
    }

    //================================================================================
    // Public Methods
    //================================================================================
    /*********************************************************************************
     * select
     *********************************************************************************
     * Determines if the selected cell is empty, updates the status of the cell
     * based on the player, logs the move in the history queue, then calls the
     * checkGameStatus method.
     *
     * @param row - The row selected by the user.
     * @param col - The column selected by the user.
     ********************************************************************************/
    public void select(int row, int col) throws UnsupportedOperationException {
        if (this.board[row][col] != Cell.EMPTY)
            throw new UnsupportedOperationException("Cell occupied by \""+this.board[row][col]+"\".");
        if (row >= this.board.length || col >= board[row].length)
            throw new IndexOutOfBoundsException("The selected cell does not exist.");

        this.board[row][col] = this.currentPlayer == 'x' ? Cell.X : Cell.O;

        history.add(new Point(row, col));

        checkGameStatus(row, col);
        switchCurPly();
    }

    /*********************************************************************************
     * reset
     *********************************************************************************
     * Resets all cells of the game board to empty, and clears the move history queue.
     ********************************************************************************/
    public void reset() {
        this.currentPlayer = this.board[history.get(0).x][history.get(0).y] == Cell.X ? 'x' : 'o';
        this.status = GameStatus.IN_PROGRESS;
        for (int i = 0; i < this.board.length; i++)
            for (int j = 0; j < this.board.length; j++)
                this.board[i][j] = Cell.EMPTY;
        this.history.clear();
    }

    /*********************************************************************************
     * undo
     *********************************************************************************
     * Reverts the previous move if it is logged in the history queue.
     ********************************************************************************/
    public void undo() {
        if (this.history.size() <= 0)
            throw new UnsupportedOperationException("There is nothing left to undo.");
        Point lastSelection = this.history.get(history.size()-1);
        this.board[lastSelection.x][lastSelection.y] = Cell.EMPTY;
        switchCurPly();
        this.history.remove(this.history.size()-1);
    }

    //================================================================================
    // Private (Helper) Methods
    //================================================================================
    /*********************************************************************************
     * checkGameStatus
     *********************************************************************************
     * Establishes the player to check for, then checks all possible win
     * conditions for that player.
     *
     * @param row - The row selected by the user.
     * @param col - The column selected by the user.
     ********************************************************************************/
    private void checkGameStatus(int row, int col) {
        Cell condition = Cell.EMPTY;
        if (this.currentPlayer == 'x')
            condition = Cell.X;
        if (this.currentPlayer == 'o')
            condition = Cell.O;

        if (checkRowColWinConditions(condition, row, col))
            return;
        if (checkDiagWinConditions(condition, row, col))
            return;
        if (checkAntiDiagWinConditions(condition, row, col))
            return;
        checkCats();
    }

    /*********************************************************************************
     * checkRowColWinConditions
     *********************************************************************************
     * Given the user's move, it checks the possible row and column win conditions at
     * that location. (More efficient than checking the entire board.)
     *
     * @param condition - The user's cell value to check for.
     * @param row - The row selected by the user.
     * @param col - The column selected by the user.
     ********************************************************************************/
    private boolean checkRowColWinConditions(Cell condition, int row, int col) {
        //Check Row Condition
        int rowCons = 0;
        int colCons = 0;
        for (int i = 0; i < this.board.length; i++) {
            //Increment Row Connections
            if (condition == this.board[row][i])
                rowCons++;
            else
                rowCons = 0;
            //Increment Column Connections
            if (condition == this.board[i][col])
                colCons++;
            else
                colCons = 0;

            //Validate win condition.
            if (rowCons >= this.connections
                    || colCons >= this.connections) {
                setWinner(condition);
                return true;
            }
        }
        return false;
    }

    /*********************************************************************************
     * checkDiagWinConditions
     *********************************************************************************
     * Given the user's move, it checks the possible diagonal win conditions at
     * that location. (More efficient than checking the entire board.)
     *
     * @param condition - The user's cell value to check for.
     * @param row - The row selected by the user.
     * @param col - The column selected by the user.
     ********************************************************************************/
    private boolean checkDiagWinConditions(Cell condition, int row, int col) {
        int subVal = Math.min(col, row);
        //The startPos is the position at which the method begins to check
        //for possible diagonal win conditions. (As far left as possible.)
        Point startPos = new Point(row-subVal, col-subVal);
        //"Primary" refers to the maximum length diagonal.
        int distFromPrimary = startPos.x+startPos.y;

        int diagCons = 0;
        for (int i = 0; i < board.length-distFromPrimary; i++) {
            diagCons = (condition == this.board[startPos.x+i][startPos.y+i]) ? diagCons+1 : 0;

            if (diagCons >= this.connections) {
                setWinner(condition);
                return true;
            }
        }
        return false;
    }

    /*********************************************************************************
     * checkAntiDiagWinConditions
     *********************************************************************************
     * Given the user's move, it checks the possible anti diagonal win conditions at
     * that location. (More efficient than checking the entire board.)
     *
     * @param condition - The user's cell value to check for.
     * @param row - The row selected by the user.
     * @param col - The column selected by the user.
     ********************************************************************************/
    private boolean checkAntiDiagWinConditions(Cell condition, int row, int col) {
        int distFromBottom = (this.board.length-1)-row;
        //If closer to the left, validate from the left. If closer to the bottom,
        //validate from the bottom.
        int subVal = Math.min(col, distFromBottom);

        //The startPos is the position at which the method begins to check
        //for possible anti diagonal win conditions. (As far left as possible.)
        Point startPos = new Point(row+subVal, col-subVal);
        //"Primary" refers to the maximum length anti diagonal.
        int distFromPrimary = Math.abs((startPos.x+startPos.y)-(this.board.length-1));

        int diagCons = 0;
        for (int i = 0; i < board.length-distFromPrimary; i++) {
            diagCons = (condition == this.board[startPos.x-i][startPos.y+i]) ? diagCons+1 : 0;

            if (diagCons >= this.connections) {
                setWinner(condition);
                return true;
            }
        }
        return false;
    }

    /*********************************************************************************
     * checkCats
     *********************************************************************************
     * Determines whether or not the game has ended in a draw, by validating that
     * none of the cells remain unselected.
     ********************************************************************************/
    private void checkCats() {
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board.length; j++) {
                if (this.board[i][j] == Cell.EMPTY)
                    return;
            }
        }
        this.status = GameStatus.CATS;
    }

    /*********************************************************************************
     * setWinner
     *********************************************************************************
     * Updates the winner if a winning condition has been detected.
     *
     * @param val - The cell value of the user that won.
     ********************************************************************************/
    private void setWinner(Cell val) {
        this.status = val == Cell.X ? GameStatus.X_WON : GameStatus.O_WON;
    }

    /*********************************************************************************
     * switchCurPly
     *********************************************************************************
     * Switches between X and O to alternate players.
     ********************************************************************************/
    private void switchCurPly() {
        this.currentPlayer = this.currentPlayer == 'x' ? 'o' : 'x';
    }

    //================================================================================
    // Getter/Setter Methods
    //================================================================================
    /*********************************************************************************
     * getGameStatus
     *********************************************************************************
     * Returns the current status of the game.
     * @return status - The current status of the game.
     ********************************************************************************/
    public GameStatus getGameStatus() {
        return this.status;
    }

    /*********************************************************************************
     * getBoard
     *********************************************************************************
     * Returns the current board.
     * @return board - The current board.
     ********************************************************************************/
    public Cell[][] getBoard() {
        return this.board;
    }

    /*********************************************************************************
     * getConnections
     *********************************************************************************
     * Returns the connections to win.
     * @return connections - The connections to win the game.
     ********************************************************************************/
    public int getConnections() {
        return this.connections;
    }
}
