package samples;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        sortDemo();
    }

    private static void sortDemo() {
        int quantity = 10;
        byte[] arr = new byte[quantity];
//        Byte[] arr = new Byte[quantity];
        java.util.Random generator = new java.util.Random();
        for (int i = 0; i < quantity; i++) {
            arr[i] = (byte) generator.nextInt(Byte.MAX_VALUE);
        }
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//        System.out.println(java.util.Arrays.toString(arr));
        System.out.println("Sorted? - " + samples.algorithms.Sorter.isSorted(arr));
//        System.out.println("Sorted? - " + samples.algorithms.QuickSorter.isSorted(arr));
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        long start = System.nanoTime();
//        samples.algorithms.MergeSorter.sort(arr);     // 10 000 ~ 35 ms,  100 000 ~ 150 ms,   1 000 000 ~ 1 s,    10 000 000 ~ 70 s
//        samples.algorithms.QuickSorter.sort(arr);     // 10 000 ~ 20 ms,  100 000 ~ 100 ms,   1 000 000 ~ 400 ms, 10 000 000 ~ 3.5 s, 100 000 000 ~ 30 s
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//        samples.algorithms.Sorter.bucketSort(arr);    // 10 000 ~ 1 ms,   100 000 ~ 10 ms,    1 000 000 ~ 55 ms,  10 000 000 ~ 70 ms, 100 000 000 ~ 300 ms,   500 000 000 ~ 1.5 s
//        samples.algorithms.Sorter.quickSort(0, arr);  // 10 000 ~ 8 ms,   100 000 ~ 50 ms,    1 000 000 ~ 250 ms, 10 000 000 ~ 2 s,   100 000 000 ~ 18 s,     500 000 000 ~ 60 s
//        samples.algorithms.Sorter.heapSort(arr);      // 10 000 ~ 10 ms,  100 000 ~ 60 ms,    1 000 000 ~ 400 ms, 10 000 000 ~ 3.8 s, 100 000 000 ~ 35 s
//        samples.algorithms.Sorter.mergeSort(0, arr);  // 10 000 ~ 25 ms,  100 000 ~ 200 ms,   1 000 000 ~ 550 ms, 10 000 000 ~ 4 s,   100 000 000 ~ 38 s
//        samples.algorithms.Sorter.insertionSort(arr); // 10 000 ~ 60 ms,  100 000 ~ 3.5 s,    1 000 000 ~ 45 s
//        samples.algorithms.Sorter.selectionSort(arr); // 10 000 ~ 300 ms, 100 000 ~ 24 s
//        samples.algorithms.Sorter.bubbleSort(arr);    // 10 000 ~ 500 ms, 100 000 ~ 50 s
//        samples.algorithms.Sorter.bogoSort(arr);
        long end = System.nanoTime();
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        System.out.println("Sorted? - " + samples.algorithms.Sorter.isSorted(arr));
//        System.out.println("Sorted? - " + samples.algorithms.QuickSorter.isSorted(arr));
//        System.out.println(java.util.Arrays.toString(arr));
        System.out.println("Time spent:" + ((end - start) / 1_000_000) + " ms");
    }
}

//        samples.algorithms.MergeSorter.sort(arr);     // 10 000 ~ 35 ms,  100 000 ~ 150 ms,   1 000 000 ~ 1 s,    10 000 000 ~ 70 s
//        samples.algorithms.QuickSorter.sort(arr);     // 10 000 ~ 20 ms,  100 000 ~ 100 ms,   1 000 000 ~ 400 ms, 10 000 000 ~ 3.5 s, 100 000 000 ~ 30 s
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//  bucketSort(arr);    // 10 000 ~ 0 ms,   100 000 ~ 2 ms,     1 000 000 ~ 10 ms,  10 000 000 ~ 40 ms,     100 000 000 ~ 125 ms,   500 000 000 ~ 550 ms, 1 000 000 000 000 ~ 1.1 s
//  quickSort(0, arr);  // 10 000 ~ 5 ms,   100 000 ~ 15 ms,    1 000 000 ~ 75 ms,  10 000 000 ~ 650 ms,    100 000 000 ~ 6.5 s,    500 000 000 ~ 35 s
//  heapSort(arr);      // 10 000 ~ 2 ms,   100 000 ~ 15 ms,    1 000 000 ~ 125 ms, 10 000 000 ~ 1.2 s,     100 000 000 ~ 12 s
//  mergeSort(0, arr);  // 10 000 ~ 5 ms,   100 000 ~ 25 ms,    1 000 000 ~ 175 ms, 10 000 000 ~ 1.5 s,     100 000 000 ~ 12.2 s
//  insertionSort(arr); // 10 000 ~ 17 ms,  100 000 ~ 1 s,      1 000 000 ~ 1.5 m
//  selectionSort(arr); // 10 000 ~ 90 ms,  100 000 ~ 8.5 s
//  bubbleSort(arr);    // 10 000 ~ 175 ms, 100 000 ~ 16.5 s