package tennis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// like a database, cloud... i.e. any datasource
final public class PlayersInfoHolder implements DataSource<AtpTennisPlayer> {
    private List<AtpTennisPlayer> players = new ArrayList<>();
    private Map<String, Long> timePerPage = new HashMap<>();

    @Override
    public List<AtpTennisPlayer> getList(Class<AtpTennisPlayer> type) {
        return players;
    }

    public Map<String, Long> getTimePerPage() {
        return timePerPage;
    }
}
