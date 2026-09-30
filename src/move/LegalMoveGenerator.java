// LegalMoveGenerator.java
// DSA: Demonstrates backtracking - make move, test, undo if illegal
// This is the KEY algorithm for valid chess move generation:
//   1. Generate pseudo-legal moves (may leave king in check)
//   2. For each move: makeMove -> isKingInCheck -> undoMove
//   3. Only keep moves that don't leave king in check
import java.util.ArrayList;
import java.util.List;

public class LegalMoveGenerator {
    
    public static List<Move> generateLegalMoves(Board board) {
        boolean white = board.isWhiteToMove();
        // Step 1: Generate all pseudo-legal moves
        List<Move> pseudoLegal = MoveGenerator.generatePseudoLegalMoves(board, white);
        List<Move> legal = new ArrayList<>();
        
        // Step 2: Filter using backtracking
        // DSA: This is backtracking - explore, test constraint, undo
        for (Move move : pseudoLegal) {
            // Make the move
            BoardMover.makeMove(board, move);
            // Test if our king is now in check (illegal if so)
            if (!CheckDetector.isInCheck(board, white)) {
                // If it's a castling move, also check castling legality
                if (move.moveType == MoveType.CASTLE_KINGSIDE || move.moveType == MoveType.CASTLE_QUEENSIDE) {
                    // Undo the move before checking castling legality (to check on the original board state)
                    BoardMover.undoMove(board);
                    if (isCastlingLegal(board, move)) {
                        legal.add(move);
                    }
                    // Continue, since we already undid the move
                    continue;
                } else {
                    legal.add(move);
                }
            }
            // Backtrack - undo the move regardless
            BoardMover.undoMove(board);
        }
        return legal;
    }
    
    // Generate legal moves for a specific piece at a square
    public static List<Move> generateLegalMovesFrom(Board board, int fromSquare) {
        List<Move> all = generateLegalMoves(board);
        List<Move> fromSquareMoves = new ArrayList<>();
        for (Move m : all) {
            if (m.from == fromSquare) fromSquareMoves.add(m);
        }
        return fromSquareMoves;
    }
    
    public static boolean hasLegalMoves(Board board) {
        return !generateLegalMoves(board).isEmpty();
    }
    
    public static boolean isCheckmate(Board board) {
        return CheckDetector.isInCheck(board, board.isWhiteToMove()) && !hasLegalMoves(board);
    }
    
    public static boolean isStalemate(Board board) {
        return !CheckDetector.isInCheck(board, board.isWhiteToMove()) && !hasLegalMoves(board);
    }
    
    // Also check castling legality - king cannot pass through or be in check
    // King cannot castle while in check
    // King cannot pass through attacked square
    public static boolean isCastlingLegal(Board board, Move castleMove) {
        boolean white = Piece.isWhite(castleMove.movedPiece);
        char[] squares = board.getSquares();
        
        // Cannot castle while in check
        if (CheckDetector.isInCheck(board, white)) return false;
        
        // Check squares the king passes through are not attacked
        boolean kingside = castleMove.moveType == MoveType.CASTLE_KINGSIDE;
        int[] passingSquares = white ?
            (kingside ? new int[]{5, 6} : new int[]{2, 3}) :
            (kingside ? new int[]{61, 62} : new int[]{58, 59});
        
        for (int sq : passingSquares) {
            if (AttackDetector.isSquareAttacked(squares, sq, !white)) return false;
        }
        return true;
    }
}
