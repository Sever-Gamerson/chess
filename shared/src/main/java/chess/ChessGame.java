package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    public TeamColor teamTurn =TeamColor.WHITE;

    private ChessBoard chessBoard;


    public ChessGame() {
        chessBoard= new ChessBoard();
        chessBoard.resetBoard();

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn=team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return new java.util.ArrayList<>();
        }//returns blank list

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> finalMoves = piece.pieceMoves(board, startPosition);

        for (ChessMove move : moves) {
            ChessPiece capturedPiece = board.getPiece(move.endPosition);
            ChessPiece movingPiece = board.getPiece(move.startPosition);

            applyMove(board, piece.getTeamColor(), move);
            boolean inCheck = isInCheck(piece.getTeamColor());

            board.gameBoard[move.startPosition.row - 1][move.startPosition.col - 1] = movingPiece;
            board.gameBoard[move.endPosition.row - 1][move.endPosition.col - 1] = capturedPiece;
            if (inCheck) {
                finalMoves.remove(move);
            }
        }

        return finalMoves;

    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard gameBoard = getBoard();

        if(gameBoard.getPiece(move.startPosition)==null){
            throw new InvalidMoveException();
        }
        TeamColor teamColor=gameBoard.getPiece(move.startPosition).getTeamColor();
        if (teamColor != getTeamTurn()) {
            throw new InvalidMoveException();
        }


        if(!validMoves(move.startPosition).contains(move)){throw new InvalidMoveException(); }//valid move

        applyMove(gameBoard,teamColor,move);


        if(getTeamTurn()==TeamColor.WHITE){
            setTeamTurn(TeamColor.BLACK);
        }else{
            setTeamTurn(TeamColor.WHITE);
        }









    }

    public void applyMove(ChessBoard gameBoard,TeamColor teamColor,ChessMove move){
        if(move.promotionPiece!=null){gameBoard.gameBoard[move.endPosition.row-1][move.endPosition.col-1]=new ChessPiece(teamColor,move.promotionPiece);}//if it promoted
        else{gameBoard.gameBoard[move.endPosition.row-1][move.endPosition.col-1]=gameBoard.getPiece(move.startPosition);}//didnt promote
        gameBoard.gameBoard[move.startPosition.row-1][move.startPosition.col-1]=null;

        setBoard(gameBoard);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard gameBoard = getBoard();
        ChessPosition kingPos=findKingPos(teamColor);

        return beingAttacked(kingPos, teamColor,gameBoard);



    }
    public boolean beingAttacked(ChessPosition pos, TeamColor myTeamColor,ChessBoard gameBoard){
        for(int row=1;row<=8;row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition checkPosition = new ChessPosition(row, col);
                ChessPiece currentPiece=gameBoard.getPiece(checkPosition);
                if (currentPiece!= null&&currentPiece.getTeamColor()!=myTeamColor) {
                    Collection<ChessMove> moves= currentPiece.pieceMoves(gameBoard,checkPosition);
                    for(ChessMove move:moves){
                        if(move.getEndPosition().equals(pos)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;


    }

    public ChessPosition findKingPos(TeamColor teamColor){
        ChessBoard gameBoard = getBoard();
        ChessPosition kingPos;
        for(int row=1;row<=8 ;row++){
            for(int col=1;col<=8;col++){
                kingPos=new ChessPosition(row,col);
                ChessPiece currentPiece=gameBoard.getPiece(kingPos);
                if(currentPiece!=null){
                    if(currentPiece.getTeamColor()==teamColor&&currentPiece.getPieceType()== ChessPiece.PieceType.KING){
                        return kingPos;
                    }
                }
            }
        }
        return null;
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessBoard gameBoard = getBoard();
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {

                ChessPosition pos = new ChessPosition(row, col);

                if(!validMoves(pos).isEmpty()&&gameBoard.getPiece(pos).getTeamColor()==teamColor){
                    return false;
                }

            }
        }

        return true;


    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessBoard gameBoard = getBoard();
        if (isInCheck(teamColor)) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {

                ChessPosition pos = new ChessPosition(row, col);
                if(!validMoves(pos).isEmpty() && gameBoard.getPiece(pos).getTeamColor()==teamColor){
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, chessBoard);
    }
}
