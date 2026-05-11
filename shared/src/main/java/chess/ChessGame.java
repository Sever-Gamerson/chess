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
    public int moveNumber=0;
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
        if(moveNumber%2==0){
            return TeamColor.WHITE;
        }else{
            return TeamColor.BLACK;
        }
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

        ChessBoard gameBoard = getBoard();
        ChessPiece selectedPiece= gameBoard.getPiece(startPosition);
        if(selectedPiece == null){
            return null;
        }
        return selectedPiece.pieceMoves(gameBoard,startPosition);

    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard gameBoard = getBoard();
        ChessPiece currentPiece=gameBoard.getPiece(move.startPosition);

        if(currentPiece==null){throw new InvalidMoveException(); }//piece exists
            TeamColor teamColor=currentPiece.getTeamColor();

        if(teamColor!=getTeamTurn()){throw new InvalidMoveException(); }//correct turn

        if(!validMoves(move.startPosition).contains(move)){throw new InvalidMoveException(); }//valid move



        if(currentPiece.getPieceType()== ChessPiece.PieceType.KING){
            if(beingAttacked(move.startPosition, teamColor,gameBoard)){
                throw new InvalidMoveException();
            }
        }

        if(move.promotionPiece!=null){gameBoard.gameBoard[move.endPosition.row-1][move.endPosition.col-1]=new ChessPiece(teamColor,move.promotionPiece);}//if it promoted
        else{gameBoard.gameBoard[move.endPosition.row-1][move.endPosition.col-1]=gameBoard.getPiece(move.startPosition);}//didnt promote
        gameBoard.gameBoard[move.startPosition.row-1][move.startPosition.col-1]=null;




        ChessBoard holdGameBoard=getBoard();
        setBoard(gameBoard);

        if(isInCheck(teamColor)){
            setBoard(holdGameBoard);
            throw new InvalidMoveException();
        }

        //next turn
        moveNumber+=1;
        setTeamTurn(getTeamTurn());








    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard gameBoard = getBoard();
        ChessPosition kingPos=new ChessPosition(1,1);
        boolean found=false;
        for(int row=1;row<=8 &&!found;row++){
            for(int col=1;col<=8;col++){
                kingPos=new ChessPosition(row,col);
                ChessPiece currentPiece=gameBoard.getPiece(kingPos);
                if(currentPiece!=null){
                    if(currentPiece.getTeamColor()==teamColor&&currentPiece.getPieceType()== ChessPiece.PieceType.KING){
                        found=true;
                        break;
                    }
                }
            }
        }
        return beingAttacked(kingPos, teamColor,gameBoard);



    }
    public boolean beingAttacked(ChessPosition pos, TeamColor myTeamColor,ChessBoard gameBoard){
        for(int row=1;row<=8;row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition checkPosition = new ChessPosition(row, col);
                ChessPiece currentPiece=gameBoard.getPiece(checkPosition);
                if ( currentPiece!= null) {
                    if(currentPiece.getTeamColor()!=myTeamColor){
                        Collection<ChessMove> moves= validMoves(checkPosition);
                        for(ChessMove move:moves){
                            if(move.getEndPosition().equals(pos)){
                                return true;
                            }
                        }
                    }

                }

            }
        }
        return false;


    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
        return moveNumber == chessGame.moveNumber && teamTurn == chessGame.teamTurn && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moveNumber, teamTurn, chessBoard);
    }
}
