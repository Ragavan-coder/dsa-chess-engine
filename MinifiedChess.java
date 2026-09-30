import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;

public class Main {
public static void main(String[] args) {
System.out.println("=== Real-Time Chess Engine ===");
Board bd = new Board();
Scanner scanner = new Scanner(System.in);
HashMap<Long, Integer> ph = new HashMap<>();
while (true) {
printBoard(bd);
GameStatus status = ChessRules.getGameStatus(bd, ph);
if (status != GameStatus.ACTIVE) {
System.out.println("Game Over: " + status);
break;
}
boolean wtm = bd.isWhiteToMove();
System.out.println((wtm ? "White" : "Black") + " to move.");
List<Move> lm = LegalMoveGenerator.glm(bd);
if (lm.isEmpty()) {
System.out.println("Game Over: No legal moves left!");
break;
}
System.out.print("Enter move (e.g. e2e4) or 'quit': ");
String input = scanner.nextLine().trim();
if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
break;
}
if (input.equalsIgnoreCase("undo")) {
BoardMover.unMv(bd);
System.out.println("Move undone.");
continue;
}
Move move = parseMove(input, lm);
if (move == null) {
System.out.println("Invalid or illegal move. Try again.");
continue;
}
ph.put(bd.getZobristHash(), ph.getOrDefault(bd.getZobristHash(), 0) + 1);
BoardMover.mkMv(bd, move);
}
scanner.close();
}
private static void printBoard(Board bd) {
char[] sq = bd.getSquares();
System.out.println("\n  a b c d e f g h");
for (int rank = 7; rank >= 0; rank--) {
System.out.print((rank + 1) + " ");
for (int file = 0; file < 8; file++) {
int sq = rank * 8 + file;
System.out.print(sq[sq] + " ");
}
System.out.println((rank + 1));
}
System.out.println("  a b c d e f g h\n");
}
private static Move parseMove(String input, List<Move> lm) {
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
prom = input.charAt(4);
}
for (Move m : lm) {
if (m.from == fromSq && m.to == toSq) {
if (m.pp != '.') {
if (Character.toLowerCase(m.pp) == prom) {
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
class Board {
private char[] sq;
private BoardState state;
private Deque<MoveState> mh;
public Board() {
sq = new char[64];
String initialFen = "RNBQKBNRPPPPPPPP................................pppppppprnbqkbnr";
for(int i = 0; i < 64; i++) sq[i] = initialFen.charAt(i);
state = new BoardState();
mh = new ArrayDeque<>();
}
public char[] getSquares() { return sq; }
public BoardState getState() { return state; }
public Deque<MoveState> getMoveHistory() { return mh; }
public boolean isWhiteToMove() { return state.wtm; }
public long getZobristHash() { return state.zh; }
public int getHalfMoveClock() { return state.hmc; }
public int findKing(boolean white) {
char target = white ? 'K' : 'k';
for (int i = 0; i < 64; i++) {
if (sq[i] == target) return i;
}
return Square.NONE;
}
}
class BoardMover {
public static void mkMv(Board bd, Move move) {
char[] sq = bd.getSquares();
BoardState state = bd.getState();
MoveState savedState = new MoveState(move, state);
bd.getMoveHistory().push(savedState);
long hash = state.zh;
if (state.eps != Square.NONE) {
hash = ZobristHash.updateEnPassant(hash, state.eps, Square.NONE);
}
int oldCastlingIdx = castlingIndex(state);
hash = ZobristHash.updatePiece(hash, sq[move.from], move.from);
switch (move.mt) {
case NORMAL -> {
sq[move.to] = sq[move.from];
sq[move.from] = '.';
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
}
case CAPTURE -> {
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
sq[move.to] = sq[move.from];
sq[move.from] = '.';
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
}
case EN_PASSANT -> {
int capturedPawnSquare = state.wtm ? move.to - 8 : move.to + 8;
hash = ZobristHash.updatePiece(hash, sq[capturedPawnSquare], capturedPawnSquare);
sq[capturedPawnSquare] = '.';
sq[move.to] = sq[move.from];
sq[move.from] = '.';
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
}
case CASTLE_KINGSIDE -> {
sq[move.to] = sq[move.from];
sq[move.from] = '.';
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
boolean white = state.wtm;
int rookFrom = white ? 7 : 63;
int rookTo   = white ? 5 : 61;
hash = ZobristHash.updatePiece(hash, sq[rookFrom], rookFrom);
sq[rookTo] = sq[rookFrom];
sq[rookFrom] = '.';
hash = ZobristHash.updatePiece(hash, sq[rookTo], rookTo);
}
case CASTLE_QUEENSIDE -> {
sq[move.to] = sq[move.from];
sq[move.from] = '.';
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
boolean white = state.wtm;
int rookFrom = white ? 0 : 56;
int rookTo   = white ? 3 : 59;
hash = ZobristHash.updatePiece(hash, sq[rookFrom], rookFrom);
sq[rookTo] = sq[rookFrom];
sq[rookFrom] = '.';
hash = ZobristHash.updatePiece(hash, sq[rookTo], rookTo);
}
case PROMOTION, PROMOTION_CAPTURE -> {
if (move.mt == MoveType.PROMOTION_CAPTURE) {
hash = ZobristHash.updatePiece(hash, sq[move.to], move.to);
}
sq[move.from] = '.';
sq[move.to] = move.pp;
hash = ZobristHash.updatePiece(hash, move.pp, move.to);
}
}
boolean isPawnMove = Character.toLowerCase(move.mp) == 'p';
boolean isCapture = move.isCapture();
state.hmc = (isPawnMove || isCapture) ? 0 : state.hmc + 1;
updateCastlingRights(state, move);
state.eps = Square.NONE;
if (move.mt == MoveType.NORMAL && isPawnMove) {
int diff = move.to - move.from;
if (Math.abs(diff) == 16) {
state.eps = state.wtm ? move.from + 8 : move.from - 8;
hash = ZobristHash.updateEnPassant(hash, Square.NONE, state.eps);
}
}
int newCastlingIdx = castlingIndex(state);
if (oldCastlingIdx != newCastlingIdx) {
hash = ZobristHash.updateCastling(hash, oldCastlingIdx, newCastlingIdx);
}
state.wtm = !state.wtm;
hash = ZobristHash.toggleSide(hash);
if (state.wtm) state.fmn++;
state.zh = hash;
}
public static void unMv(Board bd) {
if (bd.getMoveHistory().isEmpty()) return;
MoveState saved = bd.getMoveHistory().pop();
Move move = saved.move;
char[] sq = bd.getSquares();
BoardState state = bd.getState();
state.wtm = !state.wtm;
state.wkm = saved.wkm;
state.wkrm = saved.wkrm;
state.wqrm = saved.wqrm;
state.bkm = saved.bkm;
state.bkrm = saved.bkrm;
state.bqrm = saved.bqrm;
state.eps = saved.eps;
state.hmc = saved.hmc;
state.fmn = saved.fmn;
state.zh = saved.zh;
switch (move.mt) {
case NORMAL -> {
sq[move.from] = move.mp;
sq[move.to] = '.';
}
case CAPTURE -> {
sq[move.from] = move.mp;
sq[move.to] = move.cp;
}
case EN_PASSANT -> {
sq[move.from] = move.mp;
sq[move.to] = '.';
int capturedPawnSquare = state.wtm ? move.to - 8 : move.to + 8;
sq[capturedPawnSquare] = state.wtm ? 'p' : 'P';
}
case CASTLE_KINGSIDE -> {
sq[move.from] = move.mp;
sq[move.to] = '.';
boolean white = state.wtm;
int rookFrom = white ? 7 : 63;
int rookTo   = white ? 5 : 61;
sq[rookFrom] = white ? 'R' : 'r';
sq[rookTo] = '.';
}
case CASTLE_QUEENSIDE -> {
sq[move.from] = move.mp;
sq[move.to] = '.';
boolean white = state.wtm;
int rookFrom = white ? 0 : 56;
int rookTo   = white ? 3 : 59;
sq[rookFrom] = white ? 'R' : 'r';
sq[rookTo] = '.';
}
case PROMOTION, PROMOTION_CAPTURE -> {
sq[move.from] = move.mp;
sq[move.to] = move.cp;
}
}
}
private static void updateCastlingRights(BoardState state, Move move) {
if (move.mp == 'K') { state.wkm = true; }
if (move.mp == 'k') { state.bkm = true; }
if (move.from == 0  || move.to == 0)  state.wqrm = true;
if (move.from == 7  || move.to == 7)  state.wkrm = true;
if (move.from == 56 || move.to == 56) state.bqrm = true;
if (move.from == 63 || move.to == 63) state.bkrm = true;
}
private static int castlingIndex(BoardState s) {
int idx = 0;
if (!s.wkm && !s.wkrm) idx |= 1;
if (!s.wkm && !s.wqrm) idx |= 2;
if (!s.bkm && !s.bkrm) idx |= 4;
if (!s.bkm && !s.bqrm) idx |= 8;
return idx;
}
}
class BoardState {
public boolean wtm;
public boolean wkm;
public boolean wkrm;
public boolean wqrm;
public boolean bkm;
public boolean bkrm;
public boolean bqrm;
public int eps;
public int hmc;
public int fmn;
public long zh;
public BoardState() {
eps = Square.NONE;
wtm = true;
fmn = 1;
}
}
class MoveState {
public Move move;
public boolean wkm;
public boolean wkrm;
public boolean wqrm;
public boolean bkm;
public boolean bkrm;
public boolean bqrm;
public int eps;
public int hmc;
public int fmn;
public long zh;
public MoveState(Move move, BoardState state) {
this.move = move;
this.wkm = state.wkm;
this.wkrm = state.wkrm;
this.wqrm = state.wqrm;
this.bkm = state.bkm;
this.bkrm = state.bkrm;
this.bqrm = state.bqrm;
this.eps = state.eps;
this.hmc = state.hmc;
this.fmn = state.fmn;
this.zh = state.zh;
}
}
class Square {
public static final int NONE = -1;
public static int fileOf(int sq) { return sq % 8; }
public static int rankOf(int sq) { return sq / 8; }
}
enum GameStatus {
ACTIVE, CHECK, CHECKMATE, STALEMATE, DRAW
}
class PositionHistory {
private final Map<Long, Integer> history = new HashMap<>();
public void record(long hash) {
history.merge(hash, 1, Integer::sum);
}
public void remove(long hash) {
int count = history.getOrDefault(hash, 0);
if (count <= 1) history.remove(hash);
else history.put(hash, count - 1);
}
public int getCount(long hash) {
return history.getOrDefault(hash, 0);
}
public boolean isRepetition(long hash) {
return getCount(hash) >= 3;
}
public Map<Long, Integer> getMap() { return history; }
public void clear() { history.clear(); }
}
class ZobristHash {
private static final long[][] PIECE_SQUARE_KEYS = new long[12][64];
private static final long SIDE_KEY;
private static final long[] CASTLING_KEYS = new long[16];
private static final long[] EP_KEYS = new long[8];
static {
Random rng = new Random(123456789L);
for (int p = 0; p < 12; p++)
for (int sq = 0; sq < 64; sq++)
PIECE_SQUARE_KEYS[p][sq] = rng.nextLong();
SIDE_KEY = rng.nextLong();
for (int i = 0; i < 16; i++) CASTLING_KEYS[i] = rng.nextLong();
for (int f = 0; f < 8; f++) EP_KEYS[f] = rng.nextLong();
}
public static long computeHash(char[] sq, BoardState state) {
long hash = 0L;
for (int sq = 0; sq < 64; sq++) {
if (sq[sq] != '.') {
hash ^= PIECE_SQUARE_KEYS[pieceIndex(sq[sq])][sq];
}
}
if (!state.wtm) hash ^= SIDE_KEY;
hash ^= CASTLING_KEYS[castlingRightsIndex(state)];
if (state.eps != Square.NONE) {
hash ^= EP_KEYS[Square.fileOf(state.eps)];
}
return hash;
}
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
if (!s.wkm && !s.wkrm) idx |= 1;
if (!s.wkm && !s.wqrm) idx |= 2;
if (!s.bkm && !s.bkrm) idx |= 4;
if (!s.bkm && !s.bqrm) idx |= 8;
return idx;
}
}
class LegalMoveGenerator {
public static List<Move> glm(Board bd) {
boolean white = bd.isWhiteToMove();
List<Move> pl = MoveGenerator.gplm(bd, white);
List<Move> legal = new ArrayList<>();
for (Move move : pl) {
BoardMover.mkMv(bd, move);
if (!CheckDetector.inChk(bd, white)) {
if (move.mt == MoveType.CASTLE_KINGSIDE || move.mt == MoveType.CASTLE_QUEENSIDE) {
BoardMover.unMv(bd);
if (isCastlingLegal(bd, move)) {
legal.add(move);
}
continue;
} else {
legal.add(move);
}
}
BoardMover.unMv(bd);
}
return legal;
}
public static List<Move> generateLegalMovesFrom(Board bd, int fromSquare) {
List<Move> all = glm(bd);
List<Move> fromSquareMoves = new ArrayList<>();
for (Move m : all) {
if (m.from == fromSquare) fromSquareMoves.add(m);
}
return fromSquareMoves;
}
public static boolean hasLegalMoves(Board bd) {
return !glm(bd).isEmpty();
}
public static boolean isCheckmate(Board bd) {
return CheckDetector.inChk(bd, bd.isWhiteToMove()) && !hasLegalMoves(bd);
}
public static boolean isStalemate(Board bd) {
return !CheckDetector.inChk(bd, bd.isWhiteToMove()) && !hasLegalMoves(bd);
}
public static boolean isCastlingLegal(Board bd, Move castleMove) {
boolean white = Piece.isWhite(castleMove.mp);
char[] sq = bd.getSquares();
if (CheckDetector.inChk(bd, white)) return false;
boolean kingside = castleMove.mt == MoveType.CASTLE_KINGSIDE;
int[] passingSquares = white ?
(kingside ? new int[]{5, 6} : new int[]{2, 3}) :
(kingside ? new int[]{61, 62} : new int[]{58, 59});
for (int sq : passingSquares) {
if (AttackDetector.isSquareAttacked(sq, sq, !white)) return false;
}
return true;
}
}
class Move {
public int from;
public int to;
public MoveType mt;
public char mp;
public char cp;
public char pp;
public Move(int from, int to, MoveType mt, char mp, char cp, char pp) {
this.from = from;
this.to = to;
this.mt = mt;
this.mp = mp;
this.cp = cp;
this.pp = pp;
}
public boolean isCapture() {
return mt == MoveType.CAPTURE || mt == MoveType.EN_PASSANT || mt == MoveType.PROMOTION_CAPTURE;
}
}
class MoveGenerator {
private static final int[] N_OFFSETS = {-17, -15, -10, -6, 6, 10, 15, 17};
private static final int[] B_OFFSETS = {-9, -7, 7, 9};
private static final int[] R_OFFSETS = {-8, -1, 1, 8};
private static final int[] Q_OFFSETS = {-9, -8, -7, -1, 1, 7, 8, 9};
private static final int[] K_OFFSETS = {-9, -8, -7, -1, 1, 7, 8, 9};
public static List<Move> gplm(Board bd, boolean white) {
List<Move> moves = new ArrayList<>();
char[] sq = bd.getSquares();
BoardState state = bd.getState();
for (int i = 0; i < 64; i++) {
char p = sq[i];
if (p == '.') continue;
if (Piece.isWhite(p) != white) continue;
char lower = Character.toLowerCase(p);
switch (lower) {
case 'p' -> generatePawnMoves(moves, sq, state, i, white);
case 'n' -> generateStepMoves(moves, sq, i, white, N_OFFSETS, p);
case 'b' -> generateSlidingMoves(moves, sq, i, white, B_OFFSETS, p);
case 'r' -> generateSlidingMoves(moves, sq, i, white, R_OFFSETS, p);
case 'q' -> generateSlidingMoves(moves, sq, i, white, Q_OFFSETS, p);
case 'k' -> {
generateStepMoves(moves, sq, i, white, K_OFFSETS, p);
generateCastlingMoves(moves, sq, state, i, white, p);
}
}
}
return moves;
}
private static void generatePawnMoves(List<Move> moves, char[] sq, BoardState state, int from, boolean white) {
int dir = white ? 8 : -8;
int startRank = white ? 1 : 6;
int promRank = white ? 7 : 0;
char p = sq[from];
int to = from + dir;
if (to >= 0 && to < 64 && sq[to] == '.') {
addPawnMove(moves, from, to, MoveType.NORMAL, p, '.', Square.rankOf(to) == promRank);
if (Square.rankOf(from) == startRank) {
int to2 = from + 2 * dir;
if (sq[to2] == '.') {
moves.add(new Move(from, to2, MoveType.NORMAL, p, '.', '.'));
}
}
}
int[] captureDirs = white ? new int[]{7, 9} : new int[]{-9, -7};
for (int cd : captureDirs) {
int toCap = from + cd;
if (toCap >= 0 && toCap < 64 && Math.abs(Square.fileOf(from) - Square.fileOf(toCap)) == 1) {
if (sq[toCap] != '.' && Piece.isWhite(sq[toCap]) != white) {
addPawnMove(moves, from, toCap, MoveType.CAPTURE, p, sq[toCap], Square.rankOf(toCap) == promRank);
} else if (toCap == state.eps) {
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
private static void generateStepMoves(List<Move> moves, char[] sq, int from, boolean white, int[] offsets, char p) {
for (int off : offsets) {
int to = from + off;
if (to < 0 || to >= 64) continue;
int f1 = Square.fileOf(from);
int f2 = Square.fileOf(to);
if (Math.abs(off) == 1 && Math.abs(f1 - f2) > 1) continue;
if ((Math.abs(off) == 7 || Math.abs(off) == 9 || Math.abs(off) == 8) && Math.abs(f1 - f2) > 1) continue;
if ((Math.abs(off) == 6 || Math.abs(off) == 10) && Math.abs(f1 - f2) > 2) continue;
if ((Math.abs(off) == 15 || Math.abs(off) == 17) && Math.abs(f1 - f2) > 1) continue;
if (sq[to] == '.') {
moves.add(new Move(from, to, MoveType.NORMAL, p, '.', '.'));
} else if (Piece.isWhite(sq[to]) != white) {
moves.add(new Move(from, to, MoveType.CAPTURE, p, sq[to], '.'));
}
}
}
private static void generateSlidingMoves(List<Move> moves, char[] sq, int from, boolean white, int[] offsets, char p) {
for (int off : offsets) {
int to = from;
while (true) {
int prevFile = Square.fileOf(to);
to += off;
if (to < 0 || to >= 64) break;
if (Math.abs(prevFile - Square.fileOf(to)) > 1) break;
if (sq[to] == '.') {
moves.add(new Move(from, to, MoveType.NORMAL, p, '.', '.'));
} else {
if (Piece.isWhite(sq[to]) != white) {
moves.add(new Move(from, to, MoveType.CAPTURE, p, sq[to], '.'));
}
break;
}
}
}
}
private static void generateCastlingMoves(List<Move> moves, char[] sq, BoardState state, int from, boolean white, char p) {
if (white) {
if (!state.wkm) {
if (!state.wkrm && sq[5] == '.' && sq[6] == '.') {
moves.add(new Move(4, 6, MoveType.CASTLE_KINGSIDE, p, '.', '.'));
}
if (!state.wqrm && sq[1] == '.' && sq[2] == '.' && sq[3] == '.') {
moves.add(new Move(4, 2, MoveType.CASTLE_QUEENSIDE, p, '.', '.'));
}
}
} else {
if (!state.bkm) {
if (!state.bkrm && sq[61] == '.' && sq[62] == '.') {
moves.add(new Move(60, 62, MoveType.CASTLE_KINGSIDE, p, '.', '.'));
}
if (!state.bqrm && sq[57] == '.' && sq[58] == '.' && sq[59] == '.') {
moves.add(new Move(60, 58, MoveType.CASTLE_QUEENSIDE, p, '.', '.'));
}
}
}
}
}
enum MoveType {
NORMAL, CAPTURE, EN_PASSANT, CASTLE_KINGSIDE, CASTLE_QUEENSIDE, PROMOTION, PROMOTION_CAPTURE
}
class Piece {
public static boolean isWhite(char p) {
return Character.isUpperCase(p);
}
}
class AttackDetector {
public static boolean isSquareAttacked(char[] sq, int square, boolean byWhite) {
return attackedByPawn(sq, square, byWhite) ||
attackedByKnight(sq, square, byWhite) ||
attackedByBishop(sq, square, byWhite) ||
attackedByRook(sq, square, byWhite) ||
attackedByQueen(sq, square, byWhite) ||
attackedByKing(sq, square, byWhite);
}
private static boolean attackedByPawn(char[] sq, int square, boolean byWhite) {
int file = square % 8;
char targetPawn = byWhite ? 'P' : 'p';
if (byWhite) {
if (file > 0 && square - 9 >= 0 && sq[square - 9] == targetPawn) return true;
if (file < 7 && square - 7 >= 0 && sq[square - 7] == targetPawn) return true;
} else {
if (file > 0 && square + 7 < 64 && sq[square + 7] == targetPawn) return true;
if (file < 7 && square + 9 < 64 && sq[square + 9] == targetPawn) return true;
}
return false;
}
private static boolean attackedByKnight(char[] sq, int square, boolean byWhite) {
int[] offsets = {17, 15, 10, 6, -6, -10, -15, -17};
char targetKnight = byWhite ? 'N' : 'n';
int file = square % 8;
int rank = square / 8;
for (int off : offsets) {
int t = square + off;
if (t >= 0 && t < 64) {
int tFile = t % 8;
int tRank = t / 8;
if ((Math.abs(tFile - file) == 1 && Math.abs(tRank - rank) == 2) ||
(Math.abs(tFile - file) == 2 && Math.abs(tRank - rank) == 1)) {
if (sq[t] == targetKnight) return true;
}
}
}
return false;
}
private static boolean attackedByBishop(char[] sq, int square, boolean byWhite) {
int[] dirs = {9, 7, -7, -9};
for (int dir : dirs) {
if (rayHitsAttacker(sq, square, dir, byWhite, true)) return true;
}
return false;
}
private static boolean attackedByRook(char[] sq, int square, boolean byWhite) {
int[] dirs = {8, -8, 1, -1};
for (int dir : dirs) {
if (rayHitsAttacker(sq, square, dir, byWhite, false)) return true;
}
return false;
}
private static boolean attackedByQueen(char[] sq, int square, boolean byWhite) {
return attackedByBishop(sq, square, byWhite) || attackedByRook(sq, square, byWhite);
}
private static boolean attackedByKing(char[] sq, int square, boolean byWhite) {
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
if (sq[t] == targetKing) return true;
}
}
}
return false;
}
private static boolean rayHitsAttacker(char[] sq, int from, int dir, boolean byWhite, boolean diag) {
int file = from % 8;
int rank = from / 8;
char target1 = diag ? (byWhite ? 'B' : 'b') : (byWhite ? 'R' : 'r');
char target2 = byWhite ? 'Q' : 'q';
int current = from + dir;
while (current >= 0 && current < 64) {
int cFile = current % 8;
int cRank = current / 8;
if (Math.abs(cFile - file) > 1 || Math.abs(cRank - rank) > 1) {
break;
}
if (sq[current] != '.') {
return sq[current] == target1 || sq[current] == target2;
}
file = cFile;
rank = cRank;
current += dir;
}
return false;
}
}
class CheckDetector {
public static boolean inChk(Board bd, boolean white) {
int kingSquare = bd.findKing(white);
if (kingSquare == Square.NONE) return false;
return AttackDetector.isSquareAttacked(bd.getSquares(), kingSquare, !white);
}
public static boolean inChk(char[] sq, boolean white) {
char king = white ? 'K' : 'k';
int kingSquare = -1;
for (int i = 0; i < 64; i++) if (sq[i] == king) { kingSquare = i; break; }
if (kingSquare == -1) return false;
return AttackDetector.isSquareAttacked(sq, kingSquare, !white);
}
}
class ChessRules {
public static boolean isLegalMove(Board bd, Move move) {
List<Move> lm = LegalMoveGenerator.glm(bd);
for (Move legal : lm) {
if (legal.from == move.from && legal.to == move.to &&
legal.pp == move.pp) {
return true;
}
}
return false;
}
public static GameStatus getGameStatus(Board bd, Map<Long, Integer> posHistory) {
boolean white = bd.isWhiteToMove();
boolean inCheck = CheckDetector.inChk(bd, white);
boolean hasMoves = LegalMoveGenerator.hasLegalMoves(bd);
if (!hasMoves) {
return inCheck ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
}
if (inCheck) return GameStatus.CHECK;
if (DrawRules.isFiftyMoveRule(bd)) return GameStatus.DRAW;
if (DrawRules.isThreefoldRepetition(posHistory, bd.getZobristHash())) return GameStatus.DRAW;
if (DrawRules.isInsufficientMaterial(bd)) return GameStatus.DRAW;
return GameStatus.ACTIVE;
}
}
class Constants {
public static final int FIFTY_MOVE_LIMIT = 100;
}
class DrawRules {
public static boolean isFiftyMoveRule(Board bd) {
return bd.getHalfMoveClock() >= Constants.FIFTY_MOVE_LIMIT;
}
public static boolean isThreefoldRepetition(Map<Long, Integer> ph, long currentHash) {
return ph.getOrDefault(currentHash, 0) >= 3;
}
public static boolean isInsufficientMaterial(Board bd) {
char[] sq = bd.getSquares();
int whiteMinors = 0, blackMinors = 0;
boolean whiteBishop = false, blackBishop = false;
boolean whiteKnight = false, blackKnight = false;
boolean whitePawnsRooksQueens = false, blackPawnsRooksQueens = false;
for (char c : sq) {
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
if (whiteMinors == 0 && blackMinors == 0) return true;
if (whiteMinors == 0 && blackMinors == 1) return true;
if (blackMinors == 0 && whiteMinors == 1) return true;
return false;
}
}