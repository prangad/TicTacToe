package SuperTicTacToe.AI;

import SuperTicTacToe.Cell;
import SuperTicTacToe.SuperTicTacToeGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class AI {
    private AIStatus status;
    private Point optimalMove;

    private Cell[][] lastKnownBoard;
    private ArrayList<Point> plyPositions;
    private ArrayList<Point> aiPositions;

    private Cell aiCellValue;
    private Cell playerCellValue;
    private int connectionsToWin;

    /*********************************************************************************
     * AI Class
     *********************************************************************************
     * Initializes the relevant AI variables (status, connections to win, cell values,
     * and cell positions) and stores the current game board by calling remember.
     ********************************************************************************/
    public AI(SuperTicTacToeGame game, Cell aiValue) {
        this.status = AIStatus.WAITING_FOR_PLAYER;
        this.connectionsToWin = game.getConnections();
        this.aiCellValue = aiValue;
        this.playerCellValue = aiValue == Cell.X ? Cell.O : Cell.X;
        this.plyPositions = new ArrayList<Point>();
        this.aiPositions = new ArrayList<Point>();
        remember(game.getBoard());
    }

    /*********************************************************************************
     * think
     *********************************************************************************
     * The AI's status is updated to "thinking". At which time, the AI observes the
     * board, determines its best chance at winning, then returns the optimal move.
     * Before returning the optimal move, the player and ai position memory is cleared.
     *
     * @param board - The current game board.
     * @return optimalMove - The AI's optimal move as a Point object.
     ********************************************************************************/
    public Point think(Cell[][] board) {
        System.out.println();
        this.optimalMove = null;
        statusChange(AIStatus.THINKING);

        observeBoard(board);

        performStrategySequence();

        this.plyPositions.clear();
        this.aiPositions.clear();
        statusChange(AIStatus.WAITING_FOR_PLAYER);
        return this.optimalMove;
    }

    /*********************************************************************************
     * eraseMemory
     *********************************************************************************
     * Erases all data variables associated with the last game, and sets all variables
     * to their default value.
     ********************************************************************************/
    public void eraseMemory() {
        this.optimalMove = null;
        this.plyPositions.clear();
        this.aiPositions.clear();
        for (int i = 0; i < this.lastKnownBoard.length; i++)
            for (int j = 0; j < this.lastKnownBoard.length; j++)
                this.lastKnownBoard[i][j] = Cell.EMPTY;
    }

    /*********************************************************************************
     * statusChange
     *********************************************************************************
     * Updates the AI's status, and prints it to the console.
     * (Mostly so we can see what the AI is thinking.)
     *
     * @param status - The AI's status as an AIStatus enumeration.
     ********************************************************************************/
    private void statusChange(AIStatus status) {
        System.out.println("AI Status Changed: "+status);
        this.status = status;
    }

    /*********************************************************************************
     * observeBoard
     *********************************************************************************
     * Observes all player an AI positions, then stores the board as a whole.
     *
     * @param board - The current game board as a two dimensional array of Cell
     *              objects.
     ********************************************************************************/
    private void observeBoard(Cell[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == aiCellValue)
                    aiPositions.add(new Point(i, j));
                else if (board[i][j] == playerCellValue)
                    plyPositions.add(new Point(i, j));
            }
        }
        remember(board);
    }

    /*********************************************************************************
     * remember
     *********************************************************************************
     * Stores the current game board in the "lastKnownBoard" variable for later
     * parsing.
     *
     * @param board - The current game board.
     ********************************************************************************/
    private void remember(Cell[][] board) {
        //Methods such as Arrays.copyOf, clone(), and System.arrayCopy do not work on multi-dimensional arrays.
        //To save time, I am using a nested for loop. Probably more efficient ways of doing this.
        //Maybe using a single loop as well as Arrays.copyOf? Unsure.
        int boardSize = board.length;
        this.lastKnownBoard = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                this.lastKnownBoard[i][j] = board[i][j];
            }
        }
    }

    /*********************************************************************************
     * isValidPosition
     *********************************************************************************
     * Determines whether or not a position is a valid position by assessing the
     * game board's size.
     *
     * @param x - The row, or x position.
     * @param y - The column, or y position.
     * @return result - Whether or not the point is valid as a boolean.
     ********************************************************************************/
    private boolean isValidPosition(int x, int y) {
        return ((x >= 0 && x < this.lastKnownBoard.length)
                && (y >= 0 && y < this.lastKnownBoard.length));
    }

    /*********************************************************************************
     * performStrategySequence
     *********************************************************************************
     * The AI performs the strategy sequence by first attempting to win,
     * attempting to block the other player from winning, attempting to create
     * a fork, attempting to block a fork, then attempting to play as close to its
     * own other positions as possible to create chains.
     ********************************************************************************/
    private void performStrategySequence() {
        for (Point p : aiPositions)
            attemptEndgame(p);
        for (Point p : plyPositions)
            attemptBlock(p);
        for (Point p : aiPositions)
            attemptFork(p);
        for (Point p : plyPositions)
            attemptBlockFork(p);
        for (Point p : aiPositions)
            attemptBestAlternative(p);
        attemptRandom();
    }

    /*********************************************************************************
     * attemptEndgame
     *********************************************************************************
     * AI checks every one of its positions for row, column, diag, and antidiag win
     * conditions.
     *
     * @param posToCheck - The position it is currently checking.
     ********************************************************************************/
    private void attemptEndgame(Point posToCheck) {
        if ((this.optimalMove != null) || (this.aiPositions.size() <= 0))
            return;

        //Row endgame conditions.
        for (int i = 0; (i <= this.lastKnownBoard.length - this.connectionsToWin); i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[posToCheck.x][j] != Cell.EMPTY && this.lastKnownBoard[posToCheck.x][j] != this.aiCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[posToCheck.x][j] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(posToCheck.x, j);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.ENDGAME);
                this.optimalMove = emptyCell;
                return;
            }
        }

        //Column endgame conditions.
        for (int i = 0; (i <= this.lastKnownBoard.length - this.connectionsToWin); i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[j][posToCheck.y] != Cell.EMPTY && this.lastKnownBoard[j][posToCheck.y] != this.aiCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[j][posToCheck.y] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(j, posToCheck.y);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.ENDGAME);
                this.optimalMove = emptyCell;
                return;
            }
        }

        //Diagonal Endgame Conditions
        int diagSubVal = Math.min(posToCheck.y, posToCheck.x);
        Point diagStartPos = new Point(posToCheck.x-diagSubVal, posToCheck.y-diagSubVal);
        int distFromDiagPrimary = diagStartPos.x+diagStartPos.y;

        for (int i = 0; i < this.lastKnownBoard.length-distFromDiagPrimary-this.connectionsToWin+1; i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[diagStartPos.x+j][diagStartPos.y+j] != Cell.EMPTY && this.lastKnownBoard[diagStartPos.x+j][diagStartPos.y+j] != this.aiCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[diagStartPos.x+j][diagStartPos.y+j] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(diagStartPos.x+j, diagStartPos.y+j);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.ENDGAME);
                this.optimalMove = emptyCell;
                return;
            }
        }

        //AntiDiagonal Endgame Conditions
        int antiDiagDistFromBottom = (this.lastKnownBoard.length-1)-posToCheck.x;
        int antiDiagSubVal = Math.min(posToCheck.y, antiDiagDistFromBottom);

        Point antiDiagStartPos = new Point(posToCheck.x+antiDiagSubVal, posToCheck.y-antiDiagSubVal);
        int antiDiagDistFromPrimary = Math.abs((antiDiagStartPos.x+antiDiagStartPos.y)-(this.lastKnownBoard.length-1));

        for (int i = 0; i < this.lastKnownBoard.length-antiDiagDistFromPrimary-this.connectionsToWin+1; i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[antiDiagStartPos.x-j][antiDiagStartPos.y+j] != Cell.EMPTY && this.lastKnownBoard[antiDiagStartPos.x-j][antiDiagStartPos.y+j] != this.aiCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[antiDiagStartPos.x-j][antiDiagStartPos.y+j] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(antiDiagStartPos.x-j, antiDiagStartPos.y+j);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.ENDGAME);
                this.optimalMove = emptyCell;
                return;
            }
        }
    }

    /*********************************************************************************
     * attemptBlock
     *********************************************************************************
     * AI checks every one of the player's positions for row, column, diag, and
     * antidiagonal win conditions and ways to block them from winning.
     *
     * @param posToCheck - The player's position it's currently checking.
     ********************************************************************************/
    private void attemptBlock(Point posToCheck) {
        if (this.optimalMove != null)
            return;
        //Row block conditions.
        for (int i = 0; (i <= this.lastKnownBoard.length - this.connectionsToWin); i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[posToCheck.x][j] != Cell.EMPTY && this.lastKnownBoard[posToCheck.x][j] != this.playerCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[posToCheck.x][j] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(posToCheck.x, j);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.BLOCKING);
                this.optimalMove = emptyCell;
                return;
            }
        }

        //Column block conditions.
        for (int i = 0; (i <= this.lastKnownBoard.length - this.connectionsToWin); i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[j][posToCheck.y] != Cell.EMPTY && this.lastKnownBoard[j][posToCheck.y] != this.playerCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[j][posToCheck.y] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(j, posToCheck.y);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.BLOCKING);
                this.optimalMove = emptyCell;
                return;
            }
        }

        //Diagonal Block Conditions
        int diagSubVal = Math.min(posToCheck.y, posToCheck.x);
        Point diagStartPos = new Point(posToCheck.x-diagSubVal, posToCheck.y-diagSubVal);
        int distFromDiagPrimary = diagStartPos.x+diagStartPos.y;

        for (int i = 0; i < this.lastKnownBoard.length-distFromDiagPrimary-this.connectionsToWin+1; i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin+i; j++) {
                if (this.lastKnownBoard[diagStartPos.x+j][diagStartPos.y+j] != Cell.EMPTY && this.lastKnownBoard[diagStartPos.x+j][diagStartPos.y+j] != this.playerCellValue) {
                    emptyCell = null;
                    break;
                }
                else if (this.lastKnownBoard[diagStartPos.x+j][diagStartPos.y+j] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(diagStartPos.x+j, diagStartPos.y+j);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.BLOCKING);
                this.optimalMove = emptyCell;
                return;
            }
        }

        //AntiDiagonal Block Conditions
        int antiDiagDistFromBottom = (this.lastKnownBoard.length-1)-posToCheck.x;
        int antiDiagSubVal = Math.min(posToCheck.y, antiDiagDistFromBottom);

        Point antiDiagStartPos = new Point(posToCheck.x+antiDiagSubVal, posToCheck.y-antiDiagSubVal);
        int antiDiagDistFromPrimary = Math.abs((antiDiagStartPos.x+antiDiagStartPos.y)-(this.lastKnownBoard.length-1));

        for (int i = 0; i < this.lastKnownBoard.length-antiDiagDistFromPrimary-this.connectionsToWin+1; i++) {
            Point emptyCell = null;
            for (int j = i; j < this.connectionsToWin + i; j++) {
                if (this.lastKnownBoard[antiDiagStartPos.x - j][antiDiagStartPos.y + j] != Cell.EMPTY && this.lastKnownBoard[antiDiagStartPos.x - j][antiDiagStartPos.y + j] != this.playerCellValue) {
                    emptyCell = null;
                    break;
                } else if (this.lastKnownBoard[antiDiagStartPos.x - j][antiDiagStartPos.y + j] == Cell.EMPTY) {
                    if (emptyCell == null)
                        emptyCell = new Point(antiDiagStartPos.x - j, antiDiagStartPos.y + j);
                    else {
                        emptyCell = null;
                        break;
                    }
                }
            }
            if (emptyCell != null) {
                statusChange(AIStatus.BLOCKING);
                this.optimalMove = emptyCell;
                return;
            }
        }
    }

    /*********************************************************************************
     * attemptFork
     *********************************************************************************
     * This method has not yet been implemented.
     *
     * @param posToCheck - The position it is currently checking.
     ********************************************************************************/
    private void attemptFork(Point posToCheck) {
        if ((this.optimalMove != null) || (this.aiPositions.size() <= 0))
            return;
        //Not enough time to implement, unfortunately.
        //statusChange(AIStatus.CREATING_FORK);
    }

    /*********************************************************************************
     * attemptBlockFork
     *********************************************************************************
     * This method has not yet been implemented.
     *
     * @param posToCheck - The position it is currently checking.
     ********************************************************************************/
    private void attemptBlockFork(Point posToCheck) {
        if (this.optimalMove != null)
            return;
        //Not enough time to implement, unfortunately.
        //statusChange(AIStatus.BLOCKING_FORK);
    }

    /*********************************************************************************
     * attemptBestAlternative
     *********************************************************************************
     * If the AI was unable to win, block, create a fork, or block a fork, it attempts
     * to create chains of its own value by choosing a position relative to its
     * other positions.
     * Position Validation Cycle:
     * 8|1|2
     * 7|0|3
     * 6|5|4
     *
     * @param posToCheck - The position it is currently checking.
     ********************************************************************************/
    private void attemptBestAlternative(Point posToCheck) {
        if ((this.optimalMove != null) || (this.aiPositions.size() <= 0))
            return;
        statusChange(AIStatus.BEST_ALTERNATIVE);
        if (isValidPosition(posToCheck.x-1, posToCheck.y)
                && this.lastKnownBoard[posToCheck.x-1][posToCheck.y] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x-1, posToCheck.y);
        else if (isValidPosition(posToCheck.x-1, posToCheck.y+1)
                && this.lastKnownBoard[posToCheck.x-1][posToCheck.y+1] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x-1, posToCheck.y+1);
        else if (isValidPosition(posToCheck.x, posToCheck.y+1)
                && this.lastKnownBoard[posToCheck.x][posToCheck.y+1] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x, posToCheck.y+1);
        else if (isValidPosition(posToCheck.x+1, posToCheck.y+1)
                && this.lastKnownBoard[posToCheck.x+1][posToCheck.y+1] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x+1, posToCheck.y+1);
        else if (isValidPosition(posToCheck.x+1, posToCheck.y)
                && this.lastKnownBoard[posToCheck.x+1][posToCheck.y] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x+1, posToCheck.y);
        else if (isValidPosition(posToCheck.x-1, posToCheck.y-1)
                && this.lastKnownBoard[posToCheck.x-1][posToCheck.y-1] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x-1, posToCheck.y-1);
        else if (isValidPosition(posToCheck.x, posToCheck.y-1)
                && this.lastKnownBoard[posToCheck.x][posToCheck.y-1] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x, posToCheck.y-1);
        else if (isValidPosition(posToCheck.x+1, posToCheck.y-1)
                && this.lastKnownBoard[posToCheck.x+1][posToCheck.y-1] == Cell.EMPTY)
            this.optimalMove = new Point(posToCheck.x+1, posToCheck.y-1);
    }

    /*********************************************************************************
     * attemptRandom
     *********************************************************************************
     * If the AI has no information relevant to strategic positioning, it attempts to
     * make a random move.
     ********************************************************************************/
    private void attemptRandom() {
        if (this.optimalMove != null)
            return;
        statusChange(AIStatus.RANDOM);
        boolean valid = false;
        Random r = new Random();

        while (!valid) {
            int row = r.nextInt(this.lastKnownBoard.length);
            int col = r.nextInt(this.lastKnownBoard.length);
            if (this.lastKnownBoard[row][col] == Cell.EMPTY) {
                this.optimalMove = new Point(row, col);
                valid = true;
            }
        }
    }
}
