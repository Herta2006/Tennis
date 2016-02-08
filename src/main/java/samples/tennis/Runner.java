package samples.tennis;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Runner {
    private PlayersInfoHolder holder;
    private DownloaderService downloaderService;
    private SaverService saverService;

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner();
        runner.injectBeans();
        runner.getPlayersInfo();
    }

    private void injectBeans() {
        ConfigContext configContext = new ConfigContextImpl();
        DataSource<AtpTennisPlayer> dataSource = new PlayersInfoHolder();
        configContext.setDataSource(dataSource);
        downloaderService = new AtpPlayerStatsDownloaderServiceImpl();
        downloaderService.configure(configContext);
        saverService = new AtpPlayerStatsSaverServiceImpl();
        saverService.configure(configContext);
    }

    private void getPlayersInfo() throws Exception {
        long start = System.currentTimeMillis();
        downloaderService.downloadStats();
        long end = System.currentTimeMillis();
        double spentTime = (end - start) / 1000.;
        System.out.println("Spent time: " + spentTime);
        saverService.saveInfo();
    }

//    NormalDistributionCalculator.countValues(
//            DownloaderService.PLAYERS.stream()
//            .countValues((e) -> e.getServiceRecord().getGamesWon().getValue())
//            .collect(Collectors.toList())).forEach((k,v) -> {
//        if (v instanceof Map) {
//            Map countValues = (Map)v;
//            countValues.entrySet().stream().forEach(System.out::println);
//        }
//    });

//    infoHolder.PLAYERS.selectionSort(AtpTennisPlayer::compareTo);
//        DownloaderService.PLAYERS.stream().forEach(System.out::println);
//    System.out.println("Quantity of got players info: " + infoHolder.PLAYERS.size() + ". Spent time: " + allTime + " secs");
//    downloaderService.saveInfo();
}
