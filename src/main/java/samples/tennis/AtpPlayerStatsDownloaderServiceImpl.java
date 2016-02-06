package samples.tennis;

// downloads page with tennis player ranking
// parses above page and get url for each player
// requests pages with stats for each player using above url
// parses each received above page and get stats data
// saves stats data to excel

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class AtpPlayerStatsDownloaderServiceImpl implements DownloaderService {
    private static final String ATP_WORLD_TOUR_SITE_URL = "http://www.atpworldtour.com";
    private static final String RANKINGS_SINGLES_PAGE_PATH = "/en/rankings/singles";
    private static final String OUTPUT_FILE_NAME = "src/main/resources/SingleAtpTop100RankingPage.xml";
    private DataSource<AtpTennisPlayer> dataSource;

    @Override
    public void downloadStats() throws Exception {
        Callable<byte[]> mainPageDownloader = new PageDownloader(ATP_WORLD_TOUR_SITE_URL + RANKINGS_SINGLES_PAGE_PATH);
        byte[] content = Executors.newFixedThreadPool(1).submit(mainPageDownloader).get();
//        saveToFile(content, OUTPUT_FILE_NAME);
        Document parsedPage = Jsoup.parse(new BufferedInputStream(new ByteArrayInputStream(content)), Charset.defaultCharset().name(), ATP_WORLD_TOUR_SITE_URL);
        Element body = parsedPage.body();
        Elements tds = body.getElementsByClass("player-cell");
        List<Thread> threads = new ArrayList<>();
        for (Element td : tds) {
            Element a = td.children().first();
            if (a == null) continue;
            Attribute href = a.attributes().iterator().next();
            AtpTennisPlayer atpTennisPlayer = new AtpTennisPlayer(ATP_WORLD_TOUR_SITE_URL + href.getValue());
            Thread thread = new Thread(new AtpPlayerInfoPageDownloader(atpTennisPlayer, dataSource.getList(AtpTennisPlayer.class), dataSource.getTimePerPage()));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        dataSource.getList(AtpTennisPlayer.class).sort(AtpTennisPlayer::compareTo);
    }

    private void saveToFile(byte[] content, String filePath) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
        outputStream.write(content);
        outputStream.close();
    }

    public void configure(ConfigContext configContext) {
        this.dataSource = configContext.getDataSource();
    }
}


