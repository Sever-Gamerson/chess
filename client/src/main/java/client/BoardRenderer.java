package client;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoardRenderer {

    // border colors
    private static final String BORDER_BG   = "\u001B[48;5;17m";
    private static final String BORDER_TEXT = "\u001B[32m";

    // square colors
    private static final String LIGHT_SQUARE     = "\u001B[47m";
    private static final String DARK_SQUARE      = "\u001B[100m";
    private static final String LIGHT_HIGHLIGHT  = "\u001B[48;5;226m";  // yellow
    private static final String DARK_HIGHLIGHT   = "\u001B[48;5;220m";  // darker yellow

    // piece colors
    private static final String WHITE_PIECE = "\u001B[97m";
    private static final String BLACK_PIECE = "\u001B[34m";

    private static final String RESET = "\u001B[0m";

    private static final char[] COLS = {'a','b','c','d','e','f','g','h'};

    // draws the board — pass null for highlights if you don't want any
    public static void draw(boolean whiteBottom, ChessGame game,
                            Collection<ChessMove> highlights) {
        System.out.println();

        // collect just the end positions so we can check them quickly
        Set<ChessPosition> highlightedSquares = new HashSet<>();
        if (highlights != null) {
            for (ChessMove move : highlights) {

                highlightedSquares.add(move.getEndPosition());
            }

        }

        if (whiteBottom) {

            drawWhitePerspective(game.getBoard(), highlightedSquares);
        } else {
            drawBlackPerspective(game.getBoard(), highlightedSquares);
        }

        System.out.println();
    }


    private static void drawWhitePerspective(ChessBoard board, Set<ChessPosition> highlights) {
        printColHeaders(false);

        // row 8 at top down to row 1 at bottom
        for (int row = 8; row >= 1; row--) {
            printRowLabel(row);


            for (int col = 1; col <= 8; col++) {

                ChessPosition pos = new ChessPosition(row, col);
                boolean light = (row + col) % 2 != 0;

                boolean highlighted = highlights.contains(pos);
                printSquare(board.getPiece(pos), light, highlighted);

            }

            printRowLabel(row);
            System.out.println();
        }

        printColHeaders(false);
    }

    private static void drawBlackPerspective(ChessBoard board, Set<ChessPosition> highlights) {
        printColHeaders(true);

        // row 1 at top up to row 8 at bottom
        for (int row = 1; row <= 8; row++) {
            printRowLabel(row);

            for (int col = 8; col >= 1; col--) {
                ChessPosition pos = new ChessPosition(row, col);
                boolean light = (row + col) % 2 != 0;
                boolean highlighted = highlights.contains(pos);
                printSquare(board.getPiece(pos), light, highlighted);
            }

            printRowLabel(row);
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

    private static void printRowLabel(int row) {
        System.out.print(BORDER_BG + BORDER_TEXT + " " + row + " " + RESET);

    }


    private static void printSquare(ChessPiece piece, boolean light, boolean highlighted) {
        String bg;
        if (highlighted) {
            bg = light ? LIGHT_HIGHLIGHT : DARK_HIGHLIGHT;
        } else {
            bg = light ? LIGHT_SQUARE : DARK_SQUARE;
        }


        String content;
        if (piece == null) {
            content = "  ";

        } else {

            String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_PIECE : BLACK_PIECE;
            content = color + getPieceLetter(piece.getPieceType());
        }


        System.out.print(bg + " " + content + " " + RESET);
    }

    private static String getPieceLetter(ChessPiece.PieceType type) {
        return switch (type) {

            case KING   -> "K";
            case QUEEN  -> "Q";
            case ROOK   -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN   -> "P";
        };
    }
}