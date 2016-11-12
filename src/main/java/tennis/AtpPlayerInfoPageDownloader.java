package tennis;

import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import tennis.AtpTennisPlayer.AbstractRecord;
import tennis.AtpTennisPlayer.ServiceRecord;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AtpPlayerInfoPageDownloader implements Runnable {
    private final AtpTennisPlayer atpTennisPlayer;
    private static List<AtpTennisPlayer> players;
    private static Map<String, Long> timesPerPage;
    private static final int MAX_FAILED_ATTEMPT = 3;
    private int failedAttempt;

    public AtpPlayerInfoPageDownloader(AtpTennisPlayer atpTennisPlayer, List<AtpTennisPlayer> dst, Map<String, Long> timesPerPage) {
        this.atpTennisPlayer = atpTennisPlayer;
        players = dst;
        AtpPlayerInfoPageDownloader.timesPerPage = timesPerPage;
    }

    @Override
    public void run() {
        if (StringUtils.isEmpty(atpTennisPlayer.getAtpUrl())) {
            throw new IllegalArgumentException("page for downloading is empty");
        }
        long start = System.currentTimeMillis();
        parsePlayerStatsTabInfo();
        long end = System.currentTimeMillis();
        timesPerPage.put(atpTennisPlayer.getAtpUrl(), end - start);
    }

    private void parsePlayerStatsTabInfo() {
        URL url;
        String urlToDownload = atpTennisPlayer.getAtpUrl().replace(PlayerInfoTab.OVERVIEW.getValue(), PlayerInfoTab.PLAYER_STATS.getValue());
        try {
            url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            Document parsedPage = Jsoup.parse(new BufferedInputStream(inputStream), Charset.defaultCharset().name(), urlToDownload);
            Element body = parsedPage.body();
            atpTennisPlayer.setRank(Integer.parseInt(getTagByClassName(body, "data-number")));
            atpTennisPlayer.setFirstName(getTagByClassName(body, "first-name"));
            atpTennisPlayer.setSurname(getTagByClassName(body, "last-name"));
            Element serviceRecordTable = body.getElementsByClass("mega-table").first();
            if (isParsed(serviceRecordTable)) {
                players.add(atpTennisPlayer);
                failedAttempt = 0;
            }
        } catch (MalformedURLException e) {
            failedAttempt++;
            Logger.getGlobal().log(Level.WARNING, "cannot create a URL by string " + urlToDownload + ". Attempt № " + failedAttempt);
        } catch (IOException e) {
            failedAttempt++;
            Logger.getGlobal().log(Level.WARNING, "cannot open connection/get input stream/read bytes by url " + urlToDownload + ". Attempt № " + failedAttempt);
        } finally {
            if (0 < failedAttempt && failedAttempt < MAX_FAILED_ATTEMPT) {
                parsePlayerStatsTabInfo();
            }
        }
    }

    private boolean isParsed(Element serviceRecordTable) {
        return parseServiceRecordTable(serviceRecordTable, atpTennisPlayer) &&
                parseReturnRecordTable(serviceRecordTable.nextElementSibling(), atpTennisPlayer);
    }

    private String getTagByClassName(Element body, String className) {
        return body.getElementsByClass(className).first().childNodes().iterator().next().toString().trim();
    }

    private boolean parseServiceRecordTable(Element serviceRecordTable, AtpTennisPlayer atpTennisPlayer) {
        Element tBody = serviceRecordTable.children().first().nextElementSibling();
        if (tBody == null || tBody.children().isEmpty()) {
            return false;
        }

        Iterator<Element> iterator = tBody.children().iterator();
        ServiceRecord serviceRecord = new ServiceRecord();
        serviceRecord.setAces(createPair(iterator.next().children()));
        serviceRecord.setDoubleFaults(createPair(iterator.next().children()));
        serviceRecord.setPointsWon(createPair(iterator.next().children()));
        serviceRecord.setFirstServePointsWon(createPair(iterator.next().children()));
        serviceRecord.setSecondServePointsWon(createPair(iterator.next().children()));
        serviceRecord.setBreakPointsFaced(createPair(iterator.next().children()));
        serviceRecord.setBreakPointsWon(createPair(iterator.next().children()));
        serviceRecord.setGamesPlayed(createPair(iterator.next().children()));
        serviceRecord.setGamesWon(createPair(iterator.next().children()));
        serviceRecord.setTotalPointsWon(createPair(iterator.next().children()));
        atpTennisPlayer.setServiceRecord(serviceRecord);

        return true;
    }

    private Pair<String, Integer> createPair(Elements tdElements) {
        Iterator<Element> iterator = tdElements.iterator();
        Element parameterName = iterator.next();
        String key = parameterName.childNodes().iterator().next().toString().trim();
        Node parameterValue = parameterName.nextElementSibling().childNodes().iterator().next();
        String value = parameterValue.toString().trim().replace(",", "").replace("%", "");
        return new Pair<>(key, Integer.valueOf(value));
    }

    private boolean parseReturnRecordTable(Element returnRecordTable, AtpTennisPlayer atpTennisPlayer) {
        Element tBody = returnRecordTable.children().first().nextElementSibling();
        if (tBody == null || tBody.children().isEmpty()) {
            return false;
        }

        AbstractRecord returnRecord = new AbstractRecord();
        Iterator<Element> iterator = tBody.children().iterator();
        returnRecord.setFirstServePointsWon(createPair(iterator.next().children()));
        returnRecord.setSecondServePointsWon(createPair(iterator.next().children()));
        returnRecord.setBreakPointsFaced(createPair(iterator.next().children()));
        returnRecord.setBreakPointsWon(createPair(iterator.next().children()));
        returnRecord.setGamesPlayed(createPair(iterator.next().children()));
        returnRecord.setGamesWon(createPair(iterator.next().children()));
        returnRecord.setPointsWon(createPair(iterator.next().children()));
        returnRecord.setTotalPointsWon(createPair(iterator.next().children()));
        atpTennisPlayer.setReturnRecord(returnRecord);

        return true;
    }

    enum PlayerInfoTab {
        OVERVIEW("overview"), PLAYER_STATS("player-stats");
        private String value;

        PlayerInfoTab(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
