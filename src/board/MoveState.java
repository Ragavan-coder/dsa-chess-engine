public class MoveState {
    public Move move;
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

    public MoveState(Move move, BoardState state) {
        this.move = move;
        this.whiteKingMoved = state.whiteKingMoved;
        this.whiteKingsideRookMoved = state.whiteKingsideRookMoved;
        this.whiteQueensideRookMoved = state.whiteQueensideRookMoved;
        this.blackKingMoved = state.blackKingMoved;
        this.blackKingsideRookMoved = state.blackKingsideRookMoved;
        this.blackQueensideRookMoved = state.blackQueensideRookMoved;
        this.enPassantSquare = state.enPassantSquare;
        this.halfMoveClock = state.halfMoveClock;
        this.fullMoveNumber = state.fullMoveNumber;
        this.zobristHash = state.zobristHash;
    }
}
