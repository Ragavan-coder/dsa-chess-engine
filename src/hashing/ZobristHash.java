// ZobristHash.java - Incremental position hashing using Zobrist technique
// DSA: Demonstrates hashing and XOR properties for O(1) hash updates
// Key insight: XOR is its own inverse - XOR the same value twice = original value
// This allows O(1) incremental updates instead of O(64) full recomputation
import java.util.Random;

public class ZobristHash {
    // pieceSquareKeys[pieceIndex][square] = random 64-bit value
    // Piece indices: 0=WP,1=WN,2=WB,3=WR,4=WQ,5=WK,6=BP,7=BN,8=BB,9=BR,10=BQ,11=BK
    private static final long[][] PIECE_SQUARE_KEYS = new long[12][64];
    private static final long SIDE_KEY;        // XOR when black to move
    private static final long[] CASTLING_KEYS = new long[16]; // 4 castling rights = 16 combinations
    private static final long[] EP_KEYS = new long[8];        // En passant file (0-7)
    
    static {
        // Use a fixed seed for reproducibility
        Random rng = new Random(123456789L);
        for (int p = 0; p < 12; p++)
            for (int sq = 0; sq < 64; sq++)
                PIECE_SQUARE_KEYS[p][sq] = rng.nextLong();
        SIDE_KEY = rng.nextLong();
        for (int i = 0; i < 16; i++) CASTLING_KEYS[i] = rng.nextLong();
        for (int f = 0; f < 8; f++) EP_KEYS[f] = rng.nextLong();
    }
    
    // Compute full hash from scratch - O(64)
    // Used only at game start; all subsequent updates are incremental O(1)
    public static long computeHash(char[] squares, BoardState state) {
        long hash = 0L;
        for (int sq = 0; sq < 64; sq++) {
            if (squares[sq] != '.') {
                hash ^= PIECE_SQUARE_KEYS[pieceIndex(squares[sq])][sq];
            }
        }
        if (!state.whiteToMove) hash ^= SIDE_KEY;
        hash ^= CASTLING_KEYS[castlingRightsIndex(state)];
        if (state.enPassantSquare != Square.NONE) {
            hash ^= EP_KEYS[Square.fileOf(state.enPassantSquare)];
        }
        return hash;
    }
    
    // XOR a piece on/off a square - O(1)
    // Called twice per move: once to remove, once to add
    public static long updatePiece(long hash, char piece, int square) {
        return hash ^ PIECE_SQUARE_KEYS[pieceIndex(piece)][square];
    }
    
    public static long toggleSide(long hash) { return hash ^ SIDE_KEY; }
    
    public static long updateCastling(long hash, BoardState oldState, BoardState newState) {
        return hash ^ CASTLING_KEYS[castlingRightsIndex(oldState)]
                    ^ CASTLING_KEYS[castlingRightsIndex(newState)];
    }
    
    public static long updateCastling(long hash, int oldIdx, int newIdx) {
        return hash ^ CASTLING_KEYS[oldIdx] ^ CASTLING_KEYS[newIdx];
    }
    
    public static long updateEnPassant(long hash, int oldEp, int newEp) {
        if (oldEp != Square.NONE) hash ^= EP_KEYS[Square.fileOf(oldEp)];
        if (newEp != Square.NONE) hash ^= EP_KEYS[Square.fileOf(newEp)];
        return hash;
    }
    
    private static int pieceIndex(char c) {
        return switch (c) {
            case 'P' -> 0; case 'N' -> 1; case 'B' -> 2;
            case 'R' -> 3; case 'Q' -> 4; case 'K' -> 5;
            case 'p' -> 6; case 'n' -> 7; case 'b' -> 8;
            case 'r' -> 9; case 'q' -> 10; case 'k' -> 11;
            default -> throw new IllegalArgumentException("Invalid piece: " + c);
        };
    }
    
    private static int castlingRightsIndex(BoardState s) {
        int idx = 0;
        if (!s.whiteKingMoved && !s.whiteKingsideRookMoved) idx |= 1;
        if (!s.whiteKingMoved && !s.whiteQueensideRookMoved) idx |= 2;
        if (!s.blackKingMoved && !s.blackKingsideRookMoved) idx |= 4;
        if (!s.blackKingMoved && !s.blackQueensideRookMoved) idx |= 8;
        return idx;
    }
}
