package samples.tennis;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Runner {
    private static final DownloaderService SERVICE = new AtpPlayerStatsDownloaderServiceImpl();

    public static void main(String[] args) throws Exception {
        getPlayersInfo();
        List<Integer> values = DownloaderService.PLAYERS.stream()
                .map((e) -> e.getReturnRecord().getGamesWon().getValue())
                .collect(Collectors.toList());
        NormalDistributionCalculator.splitByGroups(values, 7).stream().forEach((e) -> {
            AtomicInteger sum = new AtomicInteger(0);
            e.getValues().values().parallelStream().forEach((v) -> sum.set(sum.get() + v));
            System.out.println(e + " players quantity is " + sum);
        });

    }

    private static void getPlayersInfo() throws Exception {
        long start = System.currentTimeMillis();
        SERVICE.doIt();
        long end = System.currentTimeMillis();
        double allTime = (end - start) / 1000.;
        DownloaderService.PLAYERS.sort(AtpTennisPlayer::compareTo);
//        DownloaderService.PLAYERS.stream().forEach(System.out::println);
        System.out.println("Quantity of got players info: " + DownloaderService.PLAYERS.size() + ". Spent time: " + allTime + " secs");
//        SERVICE.saveInfoToExcel();
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
}
