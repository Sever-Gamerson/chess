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
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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
            if(board.getPiece(walk)==null){//no piece in front
                if(walk.row==8||walk.row==1){
                    addPromotionMoves(myPosition, walk, moves);
                }else{
                    moves.add(new ChessMove(myPosition,walk,null));
                }
            }
            //start jump
            if((pieceColor== ChessGame.TeamColor.BLACK && currentRow==7)||(pieceColor== ChessGame.TeamColor.WHITE && currentRow==2)){
                if(board.getPiece(walk)==null){//has to make sure there's no piece in front still
                    ChessPosition run = new ChessPosition(currentRow+direction*2,currentCol);

                    if(board.getPiece(run)==null){
                        moves.add(new ChessMove(myPosition,run,null));
                    }
                }
            }
            //take right
            ChessPosition takeRight = new ChessPosition(currentRow+direction,currentCol+1);
            tryPawnCapture(board, myPosition, takeRight, moves);
            //take left
            ChessPosition takeLeft = new ChessPosition(currentRow+direction,currentCol-1);
            tryPawnCapture(board, myPosition, takeLeft, moves);

        }else if(currentPiece.getPieceType()==PieceType.KNIGHT){
            int[][] direction={{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};
            addJumpMoves(board, myPosition, direction, moves);

        } else if (currentPiece.getPieceType() == PieceType.BISHOP) {
            slidingPiece(board, myPosition, new int[][]{{1,1},{-1,1},{-1,-1},{1,-1}}, moves);

        } else if (currentPiece.getPieceType() == PieceType.ROOK) {
            slidingPiece(board, myPosition, new int[][]{{1,0},{0,1},{-1,0},{0,-1}}, moves);

        } else if (currentPiece.getPieceType() == PieceType.QUEEN) {
            slidingPiece(board, myPosition, new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{-1,1},{-1,-1},{1,-1}}, moves);

        }else if(currentPiece.getPieceType()==PieceType.KING){
            int[][] direction={{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
            addJumpMoves(board, myPosition, direction, moves);
        }

        //we about to cook
        return moves;

    }
    private void tryPawnCapture(ChessBoard board, ChessPosition myPosition,
                                ChessPosition target, java.util.ArrayList<ChessMove> moves) {
        if (target.col < 1 || target.col > 8) {
            return;
        }
        ChessPiece piece = board.getPiece(target);
        if (piece != null && piece.getTeamColor() != pieceColor) {
            if (target.row == 8 || target.row == 1) {
                addPromotionMoves(myPosition, target, moves);
            } else {
                moves.add(new ChessMove(myPosition, target, null));
            }
        }
    }
    private void addJumpMoves(ChessBoard board, ChessPosition myPosition,
                              int[][] directions, java.util.ArrayList<ChessMove> moves) {
        int currentRow = myPosition.row;
        int currentCol = myPosition.col;
        for (int[] dir : directions) {
            ChessPosition newPos = new ChessPosition(currentRow + dir[0], currentCol + dir[1]);
            if (positionGood(board, newPos, myPosition)) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
    }
    private boolean positionGood(ChessBoard board, ChessPosition pos,ChessPosition myPosition){
        if(pos.row>0&&pos.row<9&&pos.col>0&&pos.col<9){
            return board.getPiece(pos) == null || board.getPiece(pos).pieceColor != board.getPiece(myPosition).pieceColor;
        }
        return false;
    }
    private void slidingPiece(ChessBoard board, ChessPosition myPosition,
                              int[][] directions, Collection<ChessMove> moves) {
        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                ChessPosition newPos = new ChessPosition(myPosition.row + dir[0] * i,
                        myPosition.col + dir[1] * i);
                if (positionGood(board, newPos, myPosition)) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                    if (board.getPiece(newPos) != null) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void addPromotionMoves(ChessPosition from, ChessPosition to, java.util.ArrayList<ChessMove> moves) {
        moves.add(new ChessMove(from, to, PieceType.ROOK));
        moves.add(new ChessMove(from, to, PieceType.BISHOP));
        moves.add(new ChessMove(from, to, PieceType.KNIGHT));
        moves.add(new ChessMove(from, to, PieceType.QUEEN));
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
