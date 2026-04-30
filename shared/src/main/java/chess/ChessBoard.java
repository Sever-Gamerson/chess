package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    public ChessPiece[][] gameBoard;
    public ChessBoard() {
        gameBoard= new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        //throw new RuntimeException("Not implemented");
        gameBoard[position.row-1][position.col-1]=piece;

    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        //throw new RuntimeException("Not implemented");
        return gameBoard[position.row-1][position.col-1];

    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //throw new RuntimeException("Not implemented");
        //pawns
        for(int col = 0; col <8; col++){
            gameBoard[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE,ChessPiece.PieceType.PAWN);
            gameBoard[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK,ChessPiece.PieceType.PAWN);
        }


    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(gameBoard, that.gameBoard);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(gameBoard);
    }
}
