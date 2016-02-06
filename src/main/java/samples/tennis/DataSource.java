package samples.tennis;

import java.util.List;
import java.util.Map;

public interface DataSource<T> {
    List<T> getList(Class<T> type);

    Map<String, Long> getTimePerPage();
}
