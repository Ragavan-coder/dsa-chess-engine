// CheckDetector.java - Detects check, checkmate, stalemate
public class CheckDetector {
    
    public static boolean isInCheck(Board board, boolean white) {
        int kingSquare = board.findKing(white);
        if (kingSquare == Square.NONE) return false;
        return AttackDetector.isSquareAttacked(board.getSquares(), kingSquare, !white);
    }
    
    public static boolean isInCheck(char[] squares, boolean white) {
        char king = white ? 'K' : 'k';
        int kingSquare = -1;
        for (int i = 0; i < 64; i++) if (squares[i] == king) { kingSquare = i; break; }
        if (kingSquare == -1) return false;
        return AttackDetector.isSquareAttacked(squares, kingSquare, !white);
    }
}
