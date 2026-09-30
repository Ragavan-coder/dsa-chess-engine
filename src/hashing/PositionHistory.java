// PositionHistory.java - Tracks positions for threefold repetition detection
// DSA: HashMap<Long, Integer> maps Zobrist hash to occurrence count
// O(1) average-case lookup and update
import java.util.HashMap;
import java.util.Map;

public class PositionHistory {
    // DSA: HashMap demonstrating hash-based O(1) position lookup
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
