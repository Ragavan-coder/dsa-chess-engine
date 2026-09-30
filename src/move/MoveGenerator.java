import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    
    private static final int[] N_OFFSETS = {-17, -15, -10, -6, 6, 10, 15, 17};
    private static final int[] B_OFFSETS = {-9, -7, 7, 9};
    private static final int[] R_OFFSETS = {-8, -1, 1, 8};
    private static final int[] Q_OFFSETS = {-9, -8, -7, -1, 1, 7, 8, 9};
    private static final int[] K_OFFSETS = {-9, -8, -7, -1, 1, 7, 8, 9};

    public static List<Move> generatePseudoLegalMoves(Board board, boolean white) {
        List<Move> moves = new ArrayList<>();
        char[] squares = board.getSquares();
        BoardState state = board.getState();

        for (int i = 0; i < 64; i++) {
            char p = squares[i];
            if (p == '.') continue;
            if (Piece.isWhite(p) != white) continue;

            char lower = Character.toLowerCase(p);
            switch (lower) {
                case 'p' -> generatePawnMoves(moves, squares, state, i, white);
                case 'n' -> generateStepMoves(moves, squares, i, white, N_OFFSETS, p);
                case 'b' -> generateSlidingMoves(moves, squares, i, white, B_OFFSETS, p);
                case 'r' -> generateSlidingMoves(moves, squares, i, white, R_OFFSETS, p);
                case 'q' -> generateSlidingMoves(moves, squares, i, white, Q_OFFSETS, p);
                case 'k' -> {
                    generateStepMoves(moves, squares, i, white, K_OFFSETS, p);
                    generateCastlingMoves(moves, squares, state, i, white, p);
                }
            }
        }
        return moves;
    }

    private static void generatePawnMoves(List<Move> moves, char[] squares, BoardState state, int from, boolean white) {
        int dir = white ? 8 : -8;
        int startRank = white ? 1 : 6;
        int promRank = white ? 7 : 0;
        char p = squares[from];

        int to = from + dir;
        if (to >= 0 && to < 64 && squares[to] == '.') {
            addPawnMove(moves, from, to, MoveType.NORMAL, p, '.', Square.rankOf(to) == promRank);
            
            if (Square.rankOf(from) == startRank) {
                int to2 = from + 2 * dir;
                if (squares[to2] == '.') {
                    moves.add(new Move(from, to2, MoveType.NORMAL, p, '.', '.'));
                }
            }
        }

        int[] captureDirs = white ? new int[]{7, 9} : new int[]{-9, -7};
        for (int cd : captureDirs) {
            int toCap = from + cd;
            if (toCap >= 0 && toCap < 64 && Math.abs(Square.fileOf(from) - Square.fileOf(toCap)) == 1) {
                if (squares[toCap] != '.' && Piece.isWhite(squares[toCap]) != white) {
                    addPawnMove(moves, from, toCap, MoveType.CAPTURE, p, squares[toCap], Square.rankOf(toCap) == promRank);
                } else if (toCap == state.enPassantSquare) {
                    moves.add(new Move(from, toCap, MoveType.EN_PASSANT, p, white ? 'p' : 'P', '.'));
                }
            }
        }
    }

    private static void addPawnMove(List<Move> moves, int from, int to, MoveType type, char p, char cap, boolean isPromotion) {
        if (isPromotion) {
            MoveType pType = (type == MoveType.CAPTURE) ? MoveType.PROMOTION_CAPTURE : MoveType.PROMOTION;
            char[] promPieces = Piece.isWhite(p) ? new char[]{'Q', 'R', 'B', 'N'} : new char[]{'q', 'r', 'b', 'n'};
            for (char prom : promPieces) {
                moves.add(new Move(from, to, pType, p, cap, prom));
            }
        } else {
            moves.add(new Move(from, to, type, p, cap, '.'));
        }
    }

    private static void generateStepMoves(List<Move> moves, char[] squares, int from, boolean white, int[] offsets, char p) {
        for (int off : offsets) {
            int to = from + off;
            if (to < 0 || to >= 64) continue;
            
            int f1 = Square.fileOf(from);
            int f2 = Square.fileOf(to);
            if (Math.abs(off) == 1 && Math.abs(f1 - f2) > 1) continue;
            if ((Math.abs(off) == 7 || Math.abs(off) == 9 || Math.abs(off) == 8) && Math.abs(f1 - f2) > 1) continue;
            if ((Math.abs(off) == 6 || Math.abs(off) == 10) && Math.abs(f1 - f2) > 2) continue;
            if ((Math.abs(off) == 15 || Math.abs(off) == 17) && Math.abs(f1 - f2) > 1) continue;

            if (squares[to] == '.') {
                moves.add(new Move(from, to, MoveType.NORMAL, p, '.', '.'));
            } else if (Piece.isWhite(squares[to]) != white) {
                moves.add(new Move(from, to, MoveType.CAPTURE, p, squares[to], '.'));
            }
        }
    }

    private static void generateSlidingMoves(List<Move> moves, char[] squares, int from, boolean white, int[] offsets, char p) {
        for (int off : offsets) {
            int to = from;
            while (true) {
                int prevFile = Square.fileOf(to);
                to += off;
                if (to < 0 || to >= 64) break;
                if (Math.abs(prevFile - Square.fileOf(to)) > 1) break;

                if (squares[to] == '.') {
                    moves.add(new Move(from, to, MoveType.NORMAL, p, '.', '.'));
                } else {
                    if (Piece.isWhite(squares[to]) != white) {
                        moves.add(new Move(from, to, MoveType.CAPTURE, p, squares[to], '.'));
                    }
                    break;
                }
            }
        }
    }

    private static void generateCastlingMoves(List<Move> moves, char[] squares, BoardState state, int from, boolean white, char p) {
        if (white) {
            if (!state.whiteKingMoved) {
                if (!state.whiteKingsideRookMoved && squares[5] == '.' && squares[6] == '.') {
                    moves.add(new Move(4, 6, MoveType.CASTLE_KINGSIDE, p, '.', '.'));
                }
                if (!state.whiteQueensideRookMoved && squares[1] == '.' && squares[2] == '.' && squares[3] == '.') {
                    moves.add(new Move(4, 2, MoveType.CASTLE_QUEENSIDE, p, '.', '.'));
                }
            }
        } else {
            if (!state.blackKingMoved) {
                if (!state.blackKingsideRookMoved && squares[61] == '.' && squares[62] == '.') {
                    moves.add(new Move(60, 62, MoveType.CASTLE_KINGSIDE, p, '.', '.'));
                }
                if (!state.blackQueensideRookMoved && squares[57] == '.' && squares[58] == '.' && squares[59] == '.') {
                    moves.add(new Move(60, 58, MoveType.CASTLE_QUEENSIDE, p, '.', '.'));
                }
            }
        }
    }
}
