package samples;

import java.util.*;

public class Runner {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 100_000_000; i++) {
            list.add(random.nextInt(10_000));
        }
        Set<Integer> set = new HashSet<>();

//        set.contains(x) - O(1)
//        for - O(n)
//        => O(n)
        long start = System.currentTimeMillis();
        for (int i = 0; i < list.size(); i++) {
            int value = list.get(i);
            if (set.contains(value)) {
                list.set(i, 0);
            }
            set.add(value);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);

    }
}
