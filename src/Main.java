import java.util.List;
import java.util.Scanner;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Real-Time Chess Engine ===");
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);
        HashMap<Long, Integer> positionHistory = new HashMap<>();
        
        while (true) {
            printBoard(board);
            
            GameStatus status = ChessRules.getGameStatus(board, positionHistory);
            if (status != GameStatus.ACTIVE) {
                System.out.println("Game Over: " + status);
                break;
            }
            
            boolean whiteToMove = board.isWhiteToMove();
            System.out.println((whiteToMove ? "White" : "Black") + " to move.");
            
            List<Move> legalMoves = LegalMoveGenerator.generateLegalMoves(board);
            if (legalMoves.isEmpty()) {
                System.out.println("Game Over: No legal moves left!");
                break;
            }
            
            System.out.print("Enter move (e.g. e2e4) or 'quit': ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            }
            if (input.equalsIgnoreCase("undo")) {
                BoardMover.undoMove(board);
                System.out.println("Move undone.");
                continue;
            }
            
            Move move = parseMove(input, legalMoves);
            if (move == null) {
                System.out.println("Invalid or illegal move. Try again.");
                continue;
            }
            
            // Record position for threefold repetition
            positionHistory.put(board.getZobristHash(), positionHistory.getOrDefault(board.getZobristHash(), 0) + 1);
            
            // Execute the move
            BoardMover.makeMove(board, move);
        }
        scanner.close();
    }
    
    private static void printBoard(Board board) {
        char[] squares = board.getSquares();
        System.out.println("\n  a b c d e f g h");
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int sq = rank * 8 + file;
                System.out.print(squares[sq] + " ");
            }
            System.out.println((rank + 1));
        }
        System.out.println("  a b c d e f g h\n");
    }
    
    private static Move parseMove(String input, List<Move> legalMoves) {
        if (input.length() < 4) return null;
        input = input.toLowerCase();
        int fromFile = input.charAt(0) - 'a';
        int fromRank = input.charAt(1) - '1';
        int toFile = input.charAt(2) - 'a';
        int toRank = input.charAt(3) - '1';
        
        if (fromFile < 0 || fromFile > 7 || fromRank < 0 || fromRank > 7 ||
            toFile < 0 || toFile > 7 || toRank < 0 || toRank > 7) {
            return null;
        }
        
        int fromSq = fromRank * 8 + fromFile;
        int toSq = toRank * 8 + toFile;
        
        char prom = '.';
        if (input.length() == 5) {
            prom = input.charAt(4); // e.g., 'q' for e7e8q
        }
        
        for (Move m : legalMoves) {
            if (m.from == fromSq && m.to == toSq) {
                if (m.promotionPiece != '.') {
                    if (Character.toLowerCase(m.promotionPiece) == prom) {
                        return m;
                    }
                } else {
                    return m;
                }
            }
        }
        return null;
    }
}
