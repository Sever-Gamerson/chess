package client;

public class BoardRenderer {

    // pieces in order from row 8 to row 1 (black side to white side)
    private static final String[] BLACK_BACK_ROW = {"r", "n", "b", "q", "k", "b", "n", "r"};
    private static final String[] WHITE_BACK_ROW = {"R", "N", "B", "Q", "K", "B", "N", "R"};

    // ANSI color codes
    private static final String RESET = "\u001B[0m";

    private static final String LIGHT_SQUARE = "\u001B[47m";   // white background
    private static final String DARK_SQUARE = "\u001B[100m";   // dark gray background

    private static final String WHITE_PIECE = "\u001B[97m";    // bright white text
    private static final String BLACK_PIECE = "\u001B[34m";    // blue text

    private static String[][] buildBoard() {
        String[][] board = new String[8][8];

        // row 8 (index 0) — black back row
        for (int col = 0; col < 8; col++) {
            board[0][col] = BLACK_PIECE + BLACK_BACK_ROW[col] + RESET;
        }

        // row 7 index 1 — black pawn
        for (int col = 0; col < 8; col++) {

            board[1][col] = BLACK_PIECE + "p" + RESET;
        }

        // rows 6-3 index 2-5 empty
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = " ";
            }
        }
        // row 2 index 6: white pawns
        for (int col = 0; col < 8; col++) {

            board[6][col] = WHITE_PIECE + "P" + RESET;
        }

        // row 1 index 7 — white back row
        for (int col = 0; col < 8; col++) {

            board[7][col] = WHITE_PIECE + WHITE_BACK_ROW[col] + RESET;
        }

        return board;
    }
}
