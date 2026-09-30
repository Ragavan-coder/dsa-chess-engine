// AttackDetector.java
// DSA: Efficiently detects if a square is attacked by any enemy piece
// Used for: check detection, king safety, castling validation
// Time complexity: O(N) where N = board size (64)
public class AttackDetector {
    
    // Check if square is attacked by the given side (white=true attacks white pieces attack)
    // Checks all attacker types: pawns, knights, bishops, rooks, queens, king
    public static boolean isSquareAttacked(char[] squares, int square, boolean byWhite) {
        return attackedByPawn(squares, square, byWhite) ||
               attackedByKnight(squares, square, byWhite) ||
               attackedByBishop(squares, square, byWhite) ||
               attackedByRook(squares, square, byWhite) ||
               attackedByQueen(squares, square, byWhite) ||
               attackedByKing(squares, square, byWhite);
    }
    
    private static boolean attackedByPawn(char[] squares, int square, boolean byWhite) {
        // White pawns attack diagonally UP (+7, +9 from their position)
        // So a square is attacked by white pawn if a white pawn is at square-7 or square-9
        // Black pawns attack diagonally DOWN (-7, -9 from their position)
        // Carefully handle file boundaries!
        int file = square % 8;
        char targetPawn = byWhite ? 'P' : 'p';
        if (byWhite) { // Attacked by white
            if (file > 0 && square - 9 >= 0 && squares[square - 9] == targetPawn) return true;
            if (file < 7 && square - 7 >= 0 && squares[square - 7] == targetPawn) return true;
        } else { // Attacked by black
            if (file > 0 && square + 7 < 64 && squares[square + 7] == targetPawn) return true;
            if (file < 7 && square + 9 < 64 && squares[square + 9] == targetPawn) return true;
        }
        return false;
    }
    
    private static boolean attackedByKnight(char[] squares, int square, boolean byWhite) {
        // Check all 8 knight offsets from the target square
        int[] offsets = {17, 15, 10, 6, -6, -10, -15, -17};
        char targetKnight = byWhite ? 'N' : 'n';
        int file = square % 8;
        int rank = square / 8;
        
        for (int off : offsets) {
            int t = square + off;
            if (t >= 0 && t < 64) {
                int tFile = t % 8;
                int tRank = t / 8;
                // Validate move is exactly an L-shape (file diff 1 or 2, rank diff 1 or 2)
                if ((Math.abs(tFile - file) == 1 && Math.abs(tRank - rank) == 2) ||
                    (Math.abs(tFile - file) == 2 && Math.abs(tRank - rank) == 1)) {
                    if (squares[t] == targetKnight) return true;
                }
            }
        }
        return false;
    }
    
    private static boolean attackedByBishop(char[] squares, int square, boolean byWhite) {
        // Ray-cast in 4 diagonal directions
        int[] dirs = {9, 7, -7, -9};
        for (int dir : dirs) {
            if (rayHitsAttacker(squares, square, dir, byWhite, true)) return true;
        }
        return false;
    }
    
    private static boolean attackedByRook(char[] squares, int square, boolean byWhite) {
        // Ray-cast in 4 straight directions
        int[] dirs = {8, -8, 1, -1};
        for (int dir : dirs) {
            if (rayHitsAttacker(squares, square, dir, byWhite, false)) return true;
        }
        return false;
    }
    
    private static boolean attackedByQueen(char[] squares, int square, boolean byWhite) {
        // Queen = bishop + rook rays
        return attackedByBishop(squares, square, byWhite) || attackedByRook(squares, square, byWhite);
    }
    
    private static boolean attackedByKing(char[] squares, int square, boolean byWhite) {
        int[] dirs = {8, -8, 1, -1, 9, 7, -7, -9};
        char targetKing = byWhite ? 'K' : 'k';
        int file = square % 8;
        int rank = square / 8;
        
        for (int dir : dirs) {
            int t = square + dir;
            if (t >= 0 && t < 64) {
                int tFile = t % 8;
                int tRank = t / 8;
                if (Math.abs(tFile - file) <= 1 && Math.abs(tRank - rank) <= 1) {
                    if (squares[t] == targetKing) return true;
                }
            }
        }
        return false;
    }
    
    // Helper: check if a ray in direction dir hits an attacker of specified type before hitting something else
    private static boolean rayHitsAttacker(char[] squares, int from, int dir, boolean byWhite, boolean diag) {
        int file = from % 8;
        int rank = from / 8;
        char target1 = diag ? (byWhite ? 'B' : 'b') : (byWhite ? 'R' : 'r');
        char target2 = byWhite ? 'Q' : 'q';
        
        int current = from + dir;
        while (current >= 0 && current < 64) {
            int cFile = current % 8;
            int cRank = current / 8;
            
            // Check boundary wrapping (step in file/rank should be at most 1)
            if (Math.abs(cFile - file) > 1 || Math.abs(cRank - rank) > 1) {
                break;
            }
            
            if (squares[current] != '.') {
                return squares[current] == target1 || squares[current] == target2;
            }
            
            file = cFile;
            rank = cRank;
            current += dir;
        }
        return false;
    }
}
