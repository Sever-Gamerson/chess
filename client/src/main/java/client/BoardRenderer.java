package client;

public class BoardRenderer {

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String LIGHT_SQUARE = "\u001B[47m";   // white background
    private static final String DARK_SQUARE = "\u001B[100m";
    // dark gray background
    private static final String WHITE_PIECE = "\u001B[97m";    // bright white text
    private static final String BLACK_PIECE = "\u001B[34m";// blue text

    private static final String BORDER_BG = "\u001B[48;5;17m";   // dark blue background
    private static final String BORDER_TEXT = "\u001B[32m";     // dark green text

    // pieces in order from row 8 to row 1 (black side to white side)
    private static final String[] BLACK_BACK_ROW = {"r", "n", "b", "q", "k", "b", "n", "r"};
    private static final String[] WHITE_BACK_ROW = {"R", "N", "B", "Q", "K", "B", "N", "R"};

    // column labels
    private static final char[] COLS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    public static void draw(boolean whiteBottom) {
        System.out.println();

        // build the board as a 2D array of pieces
        String[][] board = buildBoard();

        if (whiteBottom) {
            drawWhitePerspective(board);
        } else {
            drawBlackPerspective(board);
        }

        System.out.println();
    }


    private static String[][] buildBoard() {
        String[][] board = new String[8][8];

        // row 8 (index 0) — black back row
        for (int col = 0; col < 8; col++) {
            board[0][col] = BLACK_PIECE + BLACK_BACK_ROW[col];

        }

        // row 7 (index 1) — black pawns
        for (int col = 0; col < 8; col++) {
            board[1][col] = BLACK_PIECE + "p";
        }

        // rows 6-3 (index 2-5) — empty
        for (int row = 2; row <= 5; row++) {

            for (int col = 0; col < 8; col++) {
                board[row][col] = " ";
            }

        }

        // row 2 (index 6) — white pawns
        for (int col = 0; col < 8; col++) {
            board[6][col] = WHITE_PIECE + "P";
        }

        // row 1 (index 7) — white back row
        for (int col = 0; col < 8; col++) {

            board[7][col] = WHITE_PIECE + WHITE_BACK_ROW[col];
        }

        return board;
    }

    private static void drawWhitePerspective(String[][] board) {
        // column headers a-h
        printColHeaders(false);

        // rows 8 down to 1
        for (int row = 0; row < 8; row++) {
            int rowLabel = 8 - row;

            System.out.print(BORDER_BG + BORDER_TEXT + " " + rowLabel + " " + RESET);

            for (int col = 0; col < 8; col++) {

                boolean lightSquare = (row + col) % 2 == 0;
                printSquare(board[row][col], lightSquare);

            }

            System.out.print(BORDER_BG + BORDER_TEXT + " " + rowLabel + " " + RESET);
            System.out.println();
        }

        printColHeaders(false);
    }

    private static void drawBlackPerspective(String[][] board) {
        // column headers h-a
        printColHeaders(true);

        // rows 1 up to 8
        for (int row = 7; row >= 0; row--) {
            int rowLabel = 8 - row;
            System.out.print(BORDER_BG + BORDER_TEXT + " " + rowLabel + " " + RESET);

            for (int col = 7; col >= 0; col--) {
                boolean lightSquare = (row + col) % 2 == 0;
                printSquare(board[row][col], lightSquare);
            }

            System.out.print(BORDER_BG + BORDER_TEXT + " " + rowLabel + " " + RESET);
            System.out.println();
        }

        printColHeaders(true);
    }

    private static void printColHeaders(boolean reversed) {
        System.out.print(BORDER_BG + BORDER_TEXT + "   " + RESET);
        if (reversed) {
            for (int i = 7; i >= 0; i--) {
                System.out.print(BORDER_BG + BORDER_TEXT + " " + COLS[i] + " " + RESET);
            }
        } else {
            for (char col : COLS) {
                System.out.print(BORDER_BG + BORDER_TEXT + " " + col + " " + RESET);
            }
        }
        System.out.println(BORDER_BG + BORDER_TEXT + "   " + RESET);
    }

    private static void printSquare(String piece, boolean lightSquare) {
        String bg = lightSquare ? LIGHT_SQUARE : DARK_SQUARE;

        String cleanPiece = piece.replace(RESET, "");
        System.out.print(bg + " " + cleanPiece + " " + RESET);
    }
}
