package samples;

import samples.algorithms.Sorter;

import java.util.Random;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        int quantity = 8_000_000;
        byte[] arr = new byte[quantity];
        Random generator = new Random();
        for (int i = 0; i < quantity; i++) {
            arr[i] = (byte)generator.nextInt(Byte.MAX_VALUE);
        }
//        System.out.println(Arrays.toString(arr));
        System.out.println("Sorted? - " + Sorter.isSorted(arr));
        long start = System.nanoTime();
        Sorter.recursiveMergeSort(arr, 3);
        long end = System.nanoTime();
        System.out.println("Sorted? - " + Sorter.isSorted(arr));
//        System.out.println(Arrays.toString(arr));
        System.out.println("Time spent:" + ((end - start) / 1_000_000) + " ms");
    }
}
