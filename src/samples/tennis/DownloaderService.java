package samples.tennis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DownloaderService {
    List<AtpTennisPlayer> PLAYERS = new ArrayList<>();
    Map<String, Long> TIMES_PER_PAGE = new HashMap<>();

    void doIt() throws Exception;

    void saveInfoToExcel();
}
