package samples.tennis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DownloaderService extends Configurable {
    void downloadStats() throws Exception;
}
