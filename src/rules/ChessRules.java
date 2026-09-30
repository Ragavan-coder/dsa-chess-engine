// ChessRules.java - Central rules enforcement facade
import java.util.List;
import java.util.Map;

public class ChessRules {
    
    public static boolean isLegalMove(Board board, Move move) {
        List<Move> legalMoves = LegalMoveGenerator.generateLegalMoves(board);
        for (Move legal : legalMoves) {
            if (legal.from == move.from && legal.to == move.to &&
                legal.promotionPiece == move.promotionPiece) {
                return true;
            }
        }
        return false;
    }
    
    public static GameStatus getGameStatus(Board board, Map<Long, Integer> posHistory) {
        boolean white = board.isWhiteToMove();
        boolean inCheck = CheckDetector.isInCheck(board, white);
        boolean hasMoves = LegalMoveGenerator.hasLegalMoves(board);
        
        if (!hasMoves) {
            return inCheck ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
        }
        if (inCheck) return GameStatus.CHECK;
        if (DrawRules.isFiftyMoveRule(board)) return GameStatus.DRAW;
        if (DrawRules.isThreefoldRepetition(posHistory, board.getZobristHash())) return GameStatus.DRAW;
        if (DrawRules.isInsufficientMaterial(board)) return GameStatus.DRAW;
        return GameStatus.ACTIVE;
    }
}
