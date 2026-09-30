public class Square {
    public static final int NONE = -1;
    public static int fileOf(int sq) { return sq % 8; }
    public static int rankOf(int sq) { return sq / 8; }
}
