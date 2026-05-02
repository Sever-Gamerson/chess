package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor=pieceColor;
        this.type=type;

    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        //throw new RuntimeException("Not implemented");
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        //throw new RuntimeException("Not implemented");
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //throw new RuntimeException("Not implemented");
        java.util.ArrayList<ChessMove> moves= new java.util.ArrayList<>();

        ChessPiece currentPiece= board.getPiece(myPosition);
        if(currentPiece==null){
            return moves;
        }

        int currentRow= myPosition.row;
        int currentCol= myPosition.col;
        if(type==PieceType.PAWN){

            int direction= 1;
            if(pieceColor== ChessGame.TeamColor.BLACK){direction=-1;}

            //normal move
            ChessPosition walk = new ChessPosition(currentRow+direction,currentCol);
            if(board.getPiece(walk)==null){//no peice in front
                if(walk.row==8||walk.row==1){
                    moves.add(new ChessMove(myPosition,walk,ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(myPosition,walk,ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition,walk,ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition,walk,ChessPiece.PieceType.QUEEN));
                }else{
                    moves.add(new ChessMove(myPosition,walk,null));
                }
            }
            //start jump
            if((pieceColor== ChessGame.TeamColor.BLACK && currentRow==7)||(pieceColor== ChessGame.TeamColor.WHITE && currentRow==2)){
                if(board.getPiece(walk)==null){//has to make sure there's no peice in front still
                    ChessPosition run = new ChessPosition(currentRow+direction*2,currentCol);

                    if(board.getPiece(run)==null){
                        moves.add(new ChessMove(myPosition,run,null));
                    }
                }
            }
            //take right
            ChessPosition takeRight = new ChessPosition(currentRow+direction,currentCol+1);
            if(takeRight.col>=1 && takeRight.col<=8){//make sure its in bounds
                if(board.getPiece(takeRight)!=null&& board.getPiece(takeRight).getTeamColor()!=pieceColor){//not empty and not a friend
                    if(takeRight.row==8||takeRight.row==1){
                        moves.add(new ChessMove(myPosition,takeRight,ChessPiece.PieceType.ROOK));
                        moves.add(new ChessMove(myPosition,takeRight,ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition,takeRight,ChessPiece.PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition,takeRight,ChessPiece.PieceType.QUEEN));
                    }else{
                        moves.add(new ChessMove(myPosition,takeRight,null));
                    }


                }
            }
            //take left
            ChessPosition takeLeft = new ChessPosition(currentRow+direction,currentCol-1);
            if(takeLeft.col>=1 && takeLeft.col<=8){//make sure its in bounds
                if(board.getPiece(takeLeft)!=null&& board.getPiece(takeLeft).getTeamColor()!=pieceColor){//not empty and not a friend
                    if(takeLeft.row==8||takeLeft.row==1){
                        moves.add(new ChessMove(myPosition,takeLeft,ChessPiece.PieceType.ROOK));
                        moves.add(new ChessMove(myPosition,takeLeft,ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition,takeLeft,ChessPiece.PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition,takeLeft,ChessPiece.PieceType.QUEEN));
                    }else{
                        moves.add(new ChessMove(myPosition,takeLeft,null));
                    }
                }
            }

        }else if(type==PieceType.KNIGHT) {
            ChessPosition topRight = new ChessPosition(currentRow + 2, currentCol+1);
            if(PositionGood(board,topRight)) {
                moves.add(new ChessMove(myPosition,topRight,null));
            }
            ChessPosition rightUp = new ChessPosition(currentRow + 1, currentCol+2);
            if(PositionGood(board,rightUp)) {
                moves.add(new ChessMove(myPosition,rightUp,null));
            }
            ChessPosition rightDown = new ChessPosition(currentRow - 1, currentCol+2);
            if(PositionGood(board,rightDown)) {
                moves.add(new ChessMove(myPosition,rightDown,null));
            }
            ChessPosition bottomRight = new ChessPosition(currentRow - 2, currentCol+1);
            if(PositionGood(board,bottomRight)) {
                moves.add(new ChessMove(myPosition,bottomRight,null));
            }
            //left ones
            ChessPosition topLeft = new ChessPosition(currentRow + 2, currentCol-1);
            if(PositionGood(board,topLeft)) {
                moves.add(new ChessMove(myPosition,topLeft,null));
            }
            ChessPosition leftUp = new ChessPosition(currentRow + 1, currentCol-2);
            if(PositionGood(board,leftUp)) {
                moves.add(new ChessMove(myPosition,leftUp,null));
            }
            ChessPosition leftDown = new ChessPosition(currentRow - 1, currentCol-2);
            if(PositionGood(board,leftDown)) {
                moves.add(new ChessMove(myPosition,leftDown,null));
            }
            ChessPosition bottomLeft = new ChessPosition(currentRow - 2, currentCol-1);
            if(PositionGood(board,bottomLeft)) {
                moves.add(new ChessMove(myPosition,bottomLeft,null));
            }
        }else if(type==PieceType.BISHOP){
            for(int x=1;x<8;x++){
                ChessPosition topRight = new ChessPosition(currentRow + x, currentCol+x);
                if(PositionGood(board,topRight)) {
                    moves.add(new ChessMove(myPosition,topRight,null));
                    if(board.getPiece(topRight)!=null){break;};
                }else{
                    break;
                }
            }
            for(int x=1;x<8;x++){
                ChessPosition topLeft = new ChessPosition(currentRow + x, currentCol-x);
                if(PositionGood(board,topLeft)) {
                    moves.add(new ChessMove(myPosition,topLeft,null));
                    if(board.getPiece(topLeft)!=null){break;};
                }else{
                    break;
                }
            }
            for(int x=1;x<8;x++){
                ChessPosition bottomLeft = new ChessPosition(currentRow - x, currentCol-x);
                if(PositionGood(board,bottomLeft)) {
                    moves.add(new ChessMove(myPosition,bottomLeft,null));
                    if(board.getPiece(bottomLeft)!=null){break;};
                }else{
                    break;
                }
            }
            for(int x=1;x<8;x++){
                ChessPosition bottomRight = new ChessPosition(currentRow - x, currentCol+x);
                if(PositionGood(board,bottomRight)) {
                    moves.add(new ChessMove(myPosition,bottomRight,null));
                    if(board.getPiece(bottomRight)!=null){break;};
                }else{
                    break;
                }
            }
        }

        //we about to cook
        return moves;

    }
    private boolean PositionGood(ChessBoard board,ChessPosition pos){//only use for moves that attack
        if(pos.col>=1 && pos.col<=8 && pos.row>=1 && pos.row<=8) {
            if (board.getPiece(pos) == null||board.getPiece(pos).getTeamColor()!=pieceColor) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
