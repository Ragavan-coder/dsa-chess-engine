public class Move {
    public int from;
    public int to;
    public MoveType moveType;
    public char movedPiece;
    public char capturedPiece;
    public char promotionPiece;

    public Move(int from, int to, MoveType moveType, char movedPiece, char capturedPiece, char promotionPiece) {
        this.from = from;
        this.to = to;
        this.moveType = moveType;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotionPiece = promotionPiece;
    }
    
    public boolean isCapture() {
        return moveType == MoveType.CAPTURE || moveType == MoveType.EN_PASSANT || moveType == MoveType.PROMOTION_CAPTURE;
    }
}
