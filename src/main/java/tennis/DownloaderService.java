package tennis;

public interface DownloaderService extends Configurable {
    void downloadStats() throws Exception;
}
