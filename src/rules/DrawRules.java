// DrawRules.java - Implements all draw detection rules
import java.util.Map;

public class DrawRules {
    
    // Fifty-move rule: 100 half-moves (50 full moves) without pawn move or capture
    public static boolean isFiftyMoveRule(Board board) {
        return board.getHalfMoveClock() >= Constants.FIFTY_MOVE_LIMIT;
    }
    
    // Threefold repetition: same position (including castling rights, ep, side to move) occurred 3 times
    public static boolean isThreefoldRepetition(Map<Long, Integer> positionHistory, long currentHash) {
        return positionHistory.getOrDefault(currentHash, 0) >= 3;
    }
    
    // Insufficient material detection
    public static boolean isInsufficientMaterial(Board board) {
        char[] squares = board.getSquares();
        int whiteMinors = 0, blackMinors = 0;
        boolean whiteBishop = false, blackBishop = false;
        boolean whiteKnight = false, blackKnight = false;
        boolean whitePawnsRooksQueens = false, blackPawnsRooksQueens = false;
        
        for (char c : squares) {
            if (c == '.') continue;
            switch (c) {
                case 'P', 'R', 'Q' -> whitePawnsRooksQueens = true;
                case 'p', 'r', 'q' -> blackPawnsRooksQueens = true;
                case 'N' -> { whiteKnight = true; whiteMinors++; }
                case 'B' -> { whiteBishop = true; whiteMinors++; }
                case 'n' -> { blackKnight = true; blackMinors++; }
                case 'b' -> { blackBishop = true; blackMinors++; }
            }
        }
        
        if (whitePawnsRooksQueens || blackPawnsRooksQueens) return false;
        // K vs K
        if (whiteMinors == 0 && blackMinors == 0) return true;
        // K+B vs K or K+N vs K
        if (whiteMinors == 0 && blackMinors == 1) return true;
        if (blackMinors == 0 && whiteMinors == 1) return true;
        // K+B vs K+B (same color bishops - complex, skip for now)
        return false;
    }
}
