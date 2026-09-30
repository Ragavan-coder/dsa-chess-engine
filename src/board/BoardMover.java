// BoardMover.java
// DSA: Demonstrates Stack/Deque for move history and backtracking
// makeMove() modifies the board and pushes MoveState onto the Deque
// undoMove() pops MoveState from the Deque and restores the board
// This is the foundation of:
//   - Legal move generation (make, test, undo)
//   - AI search (make, evaluate, undo at each node)
//   - Game history (permanent undo/redo)

public class BoardMover {
    
    // Make a move on the board - modifies squares[] and BoardState
    // Pushes MoveState to the history Deque for later undo
    // Time: O(1) for most moves, O(1) for castling (constant extra work)
    public static void makeMove(Board board, Move move) {
        char[] squares = board.getSquares();
        BoardState state = board.getState();
        
        // Save current state for undo (push onto stack)
        MoveState savedState = new MoveState(move, state);
        board.getMoveHistory().push(savedState); // Deque.push() = addFirst() = stack push
        
        long hash = state.zobristHash;
        
        // Remove old en passant from hash
        if (state.enPassantSquare != Square.NONE) {
            hash = ZobristHash.updateEnPassant(hash, state.enPassantSquare, Square.NONE);
        }
        
        // Update castling hash (before state change)
        int oldCastlingIdx = castlingIndex(state);
        
        // Move the piece
        hash = ZobristHash.updatePiece(hash, squares[move.from], move.from); // remove from source
        
        switch (move.moveType) {
            case NORMAL -> {
                squares[move.to] = squares[move.from];
                squares[move.from] = '.';
                hash = ZobristHash.updatePiece(hash, squares[move.to], move.to); // add to dest
            }
            case CAPTURE -> {
                hash = ZobristHash.updatePiece(hash, squares[move.to], move.to); // remove captured
                squares[move.to] = squares[move.from];
                squares[move.from] = '.';
                hash = ZobristHash.updatePiece(hash, squares[move.to], move.to); // add to dest
            }
            case EN_PASSANT -> {
                // The captured pawn is NOT on move.to but one square behind
                int capturedPawnSquare = state.whiteToMove ? move.to - 8 : move.to + 8;
                hash = ZobristHash.updatePiece(hash, squares[capturedPawnSquare], capturedPawnSquare);
                squares[capturedPawnSquare] = '.';
                squares[move.to] = squares[move.from];
                squares[move.from] = '.';
                hash = ZobristHash.updatePiece(hash, squares[move.to], move.to);
            }
            case CASTLE_KINGSIDE -> {
                // Move king
                squares[move.to] = squares[move.from];
                squares[move.from] = '.';
                hash = ZobristHash.updatePiece(hash, squares[move.to], move.to);
                // Move rook
                boolean white = state.whiteToMove;
                int rookFrom = white ? 7 : 63;
                int rookTo   = white ? 5 : 61;
                hash = ZobristHash.updatePiece(hash, squares[rookFrom], rookFrom);
                squares[rookTo] = squares[rookFrom];
                squares[rookFrom] = '.';
                hash = ZobristHash.updatePiece(hash, squares[rookTo], rookTo);
            }
            case CASTLE_QUEENSIDE -> {
                squares[move.to] = squares[move.from];
                squares[move.from] = '.';
                hash = ZobristHash.updatePiece(hash, squares[move.to], move.to);
                boolean white = state.whiteToMove;
                int rookFrom = white ? 0 : 56;
                int rookTo   = white ? 3 : 59;
                hash = ZobristHash.updatePiece(hash, squares[rookFrom], rookFrom);
                squares[rookTo] = squares[rookFrom];
                squares[rookFrom] = '.';
                hash = ZobristHash.updatePiece(hash, squares[rookTo], rookTo);
            }
            case PROMOTION, PROMOTION_CAPTURE -> {
                if (move.moveType == MoveType.PROMOTION_CAPTURE) {
                    hash = ZobristHash.updatePiece(hash, squares[move.to], move.to);
                }
                squares[move.from] = '.';
                squares[move.to] = move.promotionPiece;
                hash = ZobristHash.updatePiece(hash, move.promotionPiece, move.to);
            }
        }
        
        // Update half-move clock
        boolean isPawnMove = Character.toLowerCase(move.movedPiece) == 'p';
        boolean isCapture = move.isCapture();
        state.halfMoveClock = (isPawnMove || isCapture) ? 0 : state.halfMoveClock + 1;
        
        // Update castling rights based on what moved
        updateCastlingRights(state, move);
        
        // Update en passant square
        state.enPassantSquare = Square.NONE;
        if (move.moveType == MoveType.NORMAL && isPawnMove) {
            int diff = move.to - move.from;
            if (Math.abs(diff) == 16) {
                // Double pawn push - set en passant square
                state.enPassantSquare = state.whiteToMove ? move.from + 8 : move.from - 8;
                hash = ZobristHash.updateEnPassant(hash, Square.NONE, state.enPassantSquare);
            }
        }
        
        // Update castling hash
        int newCastlingIdx = castlingIndex(state);
        if (oldCastlingIdx != newCastlingIdx) {
            hash = ZobristHash.updateCastling(hash, oldCastlingIdx, newCastlingIdx);
        }
        
        // Switch side to move
        state.whiteToMove = !state.whiteToMove;
        hash = ZobristHash.toggleSide(hash);
        
        // Update full move number
        if (state.whiteToMove) state.fullMoveNumber++;
        
        state.zobristHash = hash;
    }
    
    // Undo the last move - pops from the Deque stack and restores board
    // Time: O(1)
    public static void undoMove(Board board) {
        if (board.getMoveHistory().isEmpty()) return;
        
        // Pop the saved state (LIFO - last move undone first)
        MoveState saved = board.getMoveHistory().pop();
        Move move = saved.move;
        char[] squares = board.getSquares();
        BoardState state = board.getState();
        
        // Restore state fields
        state.whiteToMove = !state.whiteToMove; // Switch back
        state.whiteKingMoved = saved.whiteKingMoved;
        state.whiteKingsideRookMoved = saved.whiteKingsideRookMoved;
        state.whiteQueensideRookMoved = saved.whiteQueensideRookMoved;
        state.blackKingMoved = saved.blackKingMoved;
        state.blackKingsideRookMoved = saved.blackKingsideRookMoved;
        state.blackQueensideRookMoved = saved.blackQueensideRookMoved;
        state.enPassantSquare = saved.enPassantSquare;
        state.halfMoveClock = saved.halfMoveClock;
        state.fullMoveNumber = saved.fullMoveNumber;
        state.zobristHash = saved.zobristHash; // Restore hash directly
        
        // Reverse the move
        switch (move.moveType) {
            case NORMAL -> {
                squares[move.from] = move.movedPiece;
                squares[move.to] = '.';
            }
            case CAPTURE -> {
                squares[move.from] = move.movedPiece;
                squares[move.to] = move.capturedPiece;
            }
            case EN_PASSANT -> {
                squares[move.from] = move.movedPiece;
                squares[move.to] = '.';
                int capturedPawnSquare = state.whiteToMove ? move.to - 8 : move.to + 8;
                squares[capturedPawnSquare] = state.whiteToMove ? 'p' : 'P';
            }
            case CASTLE_KINGSIDE -> {
                squares[move.from] = move.movedPiece;
                squares[move.to] = '.';
                boolean white = state.whiteToMove;
                int rookFrom = white ? 7 : 63;
                int rookTo   = white ? 5 : 61;
                squares[rookFrom] = white ? 'R' : 'r';
                squares[rookTo] = '.';
            }
            case CASTLE_QUEENSIDE -> {
                squares[move.from] = move.movedPiece;
                squares[move.to] = '.';
                boolean white = state.whiteToMove;
                int rookFrom = white ? 0 : 56;
                int rookTo   = white ? 3 : 59;
                squares[rookFrom] = white ? 'R' : 'r';
                squares[rookTo] = '.';
            }
            case PROMOTION, PROMOTION_CAPTURE -> {
                squares[move.from] = move.movedPiece; // Restore pawn
                squares[move.to] = move.capturedPiece; // Restore captured (or '.')
            }
        }
    }
    
    private static void updateCastlingRights(BoardState state, Move move) {
        // If king moves, lose both castling rights
        if (move.movedPiece == 'K') { state.whiteKingMoved = true; }
        if (move.movedPiece == 'k') { state.blackKingMoved = true; }
        // If rook moves or is captured, update rook-moved flags
        if (move.from == 0  || move.to == 0)  state.whiteQueensideRookMoved = true;
        if (move.from == 7  || move.to == 7)  state.whiteKingsideRookMoved = true;
        if (move.from == 56 || move.to == 56) state.blackQueensideRookMoved = true;
        if (move.from == 63 || move.to == 63) state.blackKingsideRookMoved = true;
    }
    
    private static int castlingIndex(BoardState s) {
        int idx = 0;
        if (!s.whiteKingMoved && !s.whiteKingsideRookMoved) idx |= 1;
        if (!s.whiteKingMoved && !s.whiteQueensideRookMoved) idx |= 2;
        if (!s.blackKingMoved && !s.blackKingsideRookMoved) idx |= 4;
        if (!s.blackKingMoved && !s.blackQueensideRookMoved) idx |= 8;
        return idx;
    }
}
