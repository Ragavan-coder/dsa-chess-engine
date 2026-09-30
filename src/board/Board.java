import java.util.ArrayDeque;
import java.util.Deque;

public class Board {
    private char[] squares;
    private BoardState state;
    private Deque<MoveState> moveHistory;

    public Board() {
        squares = new char[64];
        String initialFen = "RNBQKBNRPPPPPPPP................................pppppppprnbqkbnr";
        for(int i = 0; i < 64; i++) squares[i] = initialFen.charAt(i);
        state = new BoardState();
        moveHistory = new ArrayDeque<>();
    }

    public char[] getSquares() { return squares; }
    public BoardState getState() { return state; }
    public Deque<MoveState> getMoveHistory() { return moveHistory; }
    public boolean isWhiteToMove() { return state.whiteToMove; }
    public long getZobristHash() { return state.zobristHash; }
    public int getHalfMoveClock() { return state.halfMoveClock; }
    
    public int findKing(boolean white) {
        char target = white ? 'K' : 'k';
        for (int i = 0; i < 64; i++) {
            if (squares[i] == target) return i;
        }
        return Square.NONE;
    }
}
