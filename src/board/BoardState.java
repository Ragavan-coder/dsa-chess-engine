public class BoardState {
    public boolean whiteToMove;
    public boolean whiteKingMoved;
    public boolean whiteKingsideRookMoved;
    public boolean whiteQueensideRookMoved;
    public boolean blackKingMoved;
    public boolean blackKingsideRookMoved;
    public boolean blackQueensideRookMoved;
    public int enPassantSquare;
    public int halfMoveClock;
    public int fullMoveNumber;
    public long zobristHash;
    
    public BoardState() {
        enPassantSquare = Square.NONE;
        whiteToMove = true;
        fullMoveNumber = 1;
    }
}
