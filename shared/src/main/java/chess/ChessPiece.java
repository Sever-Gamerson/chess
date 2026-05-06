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
            int[][] directions ={ {2,1}, {1,2}, {-1,2}, {-2,1},{2,-1}, {1,-2}, {-1,-2}, {-2,-1}};
            for(int i=0;i<directions.length ;i++){
                ChessPosition pos = new ChessPosition(myPosition.row+directions[i][0], myPosition.col+directions[i][1]);
                if(PositionGood(board,pos)) {
                    moves.add(new ChessMove(myPosition,pos,null));
                }
            }
        }else if(type==PieceType.BISHOP){

            int[][] positiveDirections ={{1,1},{1,-1}};

            for(int i=0;i<positiveDirections.length ;i++){
                sliddingMovesPositive(moves,board,myPosition,positiveDirections[i][0],positiveDirections[i][1]);
            }

        }else if(type==PieceType.ROOK){
            int[][] positiveDirections ={{0,1},{1,0}};

            for(int i=0;i<positiveDirections.length ;i++){
                sliddingMovesPositive(moves,board,myPosition,positiveDirections[i][0],positiveDirections[i][1]);

            }
        }else if(type==PieceType.QUEEN){
            int[][] positiveDirections ={{1,1},{1,-1},{0,1},{1,0}};

            for(int i=0;i<positiveDirections.length ;i++){
                sliddingMovesPositive(moves,board,myPosition,positiveDirections[i][0],positiveDirections[i][1]);

            }
        }else if(type==PieceType.KING){
            int[][] directions ={{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
            for(int i=0;i<directions.length ;i++){
                ChessPosition kingPos = new ChessPosition(myPosition.row+directions[i][0], myPosition.col+directions[i][1]);
                if(PositionGood(board,kingPos)) {
                    moves.add(new ChessMove(myPosition,kingPos,null));
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
    private void sliddingMovesPositive(Collection<ChessMove> moves,ChessBoard board, ChessPosition pos, int dRow, int dCol){

        for(int x=1;x<8;x++){
            ChessPosition checkPositive = new ChessPosition(pos.row +dRow*x, pos.col+dCol*x);
            if(!PositionGood(board,checkPositive)) {break;}

            moves.add(new ChessMove(pos,checkPositive,null));
            if(board.getPiece(checkPositive)!=null){break;};
        }
        //do the same backwards
        for(int x=1;x<8;x++){
            ChessPosition checkNegitive = new ChessPosition(pos.row -dRow*x, pos.col-dCol*x);
            if(!PositionGood(board,checkNegitive)) {break;}


            moves.add(new ChessMove(pos,checkNegitive,null));
            if(board.getPiece(checkNegitive)!=null){break;};
        }
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
