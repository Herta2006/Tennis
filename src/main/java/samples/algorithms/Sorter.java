package samples.algorithms;

import java.util.Arrays;

public class Sorter {

    public static boolean isSorted(byte[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static void recursiveMergeSort(byte[] arr, int powerOf2OfThreadsAmount) {
        Sorter.SortTask.setMaxRecursionDepth(powerOf2OfThreadsAmount);
        Sorter.SortTask sortTask = new Sorter.SortTask(arr);
        sortTask.start();
        try {
            sortTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final class SortTask extends Thread implements Runnable {
        private final byte[] arr;
        private static int recursionDepth;
        private static int maxRecursionDepth;

        @Override
        public void run() {
            recursiveMergeSort(arr);
        }

        private void recursiveMergeSort(byte... main) {
            if (main.length == 1) {
                return;
            }
            int leftEnd = main.length / 2;
            byte[] left = Arrays.copyOfRange(main, 0, leftEnd);
            byte[] right = Arrays.copyOfRange(main, leftEnd, main.length);
            if (recursionDepth++ < maxRecursionDepth) {
                try {
                    SortTask leftTask = new SortTask(left);
                    SortTask rightTask = new SortTask(right);
                    leftTask.start();
                    rightTask.start();
                    leftTask.join();
                    rightTask.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                recursiveMergeSort(left);
                recursiveMergeSort(right);
            }
            int leftCounter = 0;
            int rightCounter = 0;
            int mainCounter = 0;
            for (; leftCounter < left.length && rightCounter < right.length; mainCounter++) {
                if (left[leftCounter] < right[rightCounter]) {
                    main[mainCounter] = left[leftCounter++];
                } else {
                    main[mainCounter] = right[rightCounter++];
                }
            }
            if (leftCounter < left.length) {
                System.arraycopy(left, leftCounter, main, mainCounter, left.length - leftCounter);
            } else if (rightCounter < right.length) {
                System.arraycopy(right, rightCounter, main, mainCounter, right.length - rightCounter);
            }
        }

        public SortTask(byte[] arr) {
            this.arr = arr;
        }

        public byte[] getArr() {
            return arr;
        }

        public static void setMaxRecursionDepth(int maxRecursionDepth) {
            SortTask.maxRecursionDepth = maxRecursionDepth;
        }
    }

    private Sorter() {
    }
}


//    public static void mergeSort(int... arr) {
//        if (arr.length == 1) {
//            return;
//        }
//        int leftEnd = arr.length / 2;
//        int[] left = Arrays.copyOfRange(arr, 0, leftEnd);
//        int[] right = Arrays.copyOfRange(arr, leftEnd, arr.length);
//
//        mergeSort(left);
//        mergeSort(right);
//        int leftCounter = 0;
//        int rightCounter = 0;
//        int mainCounter = 0;
//        for (; leftCounter < left.length && rightCounter < right.length; mainCounter++) {
//            if (left[leftCounter] < right[rightCounter]) {
//                arr[mainCounter] = left[leftCounter++];
//            } else {
//                arr[mainCounter] = right[rightCounter++];
//            }
//        }
//        if (leftCounter < left.length) {
//            System.arraycopy(left, leftCounter, arr, mainCounter, left.length - leftCounter);
//        } else if (rightCounter < right.length) {
//            System.arraycopy(right, rightCounter, arr, mainCounter, right.length - rightCounter);
//        }
//    }


//    public static void recursiveQuickSort(int[] arr, int left, int right) {
//        // For Recusrion
//        if (left < right) {
//            int pivot = partition(arr, left, right);
//
//            if (pivot > 1)
//                recursiveQuickSort(arr, left, pivot - 1);
//
//            if (pivot + 1 < right)
//                recursiveQuickSort(arr, pivot + 1, right);
//        }
//    }
//
//    static public void iterativeMergeSort(int[] numbers, int left, int right) {
//        int mid;
//        if (right <= left)
//            return;
//
//        class MergePosInfo {
//            public int left;
//            public int mid;
//            public int right;
//        }
//
//        ArrayList<MergePosInfo> list1 = new ArrayList<>();
//        ArrayList<MergePosInfo> list2 = new ArrayList<>();
//
//        MergePosInfo info = new MergePosInfo();
//        info.left = left;
//        info.right = right;
//        info.mid = -1;
//
//        list1.add(info);
//
//        while (true) {
//            if (list1.size() == 0)
//                break;
//
//            left = list1.get(0).left;
//            right = list1.get(0).right;
//            list1.remove(0);
//
//            mid = (right + left) / 2;
//
//            if (left < right) {
//                MergePosInfo info2 = new MergePosInfo();
//                info2.left = left;
//                info2.right = right;
//                info2.mid = mid + 1;
//                list2.add(info2);
//
//                info.left = left;
//                info.right = mid;
//                list1.add(info);
//
//                info.left = mid + 1;
//                info.right = right;
//                list1.add(info);
//            }
//        }
//
//        for (MergePosInfo aList2 : list2) {
//            merge(numbers, aList2.left, aList2.mid, aList2.right);
//        }
//
//    }
//
//    static public void recursiveMergeSort(int[] numbers, int left, int right) {
//        int mid;
//        if (right > left) {
//            mid = (right + left) / 2;
//            recursiveMergeSort(numbers, left, mid);
//            recursiveMergeSort(numbers, (mid + 1), right);
//            merge(numbers, left, (mid + 1), right);
//        }
//    }
//
//    static public void merge(int[] numbers, int left, int mid, int right) {
//        int[] temp = new int[25];
//        int i, left_end, num_elements, tmp_pos;
//
//        left_end = (mid - 1);
//        tmp_pos = left;
//        num_elements = (right - left + 1);
//
//        while ((left <= left_end) && (mid <= right)) {
//            if (numbers[left] <= numbers[mid])
//                temp[tmp_pos++] = numbers[left++];
//            else
//                temp[tmp_pos++] = numbers[mid++];
//        }
//
//        while (left <= left_end)
//            temp[tmp_pos++] = numbers[left++];
//
//        while (mid <= right)
//            temp[tmp_pos++] = numbers[mid++];
//
//        for (i = 0; i < num_elements; i++) {
//            numbers[right] = temp[right];
//            right--;
//        }
//    }
//
//    public static int partition(int[] numbers, int left, int right) {
//        int pivot = numbers[left];
//        while (true) {
//            while (numbers[left] < pivot)
//                left++;
//
//            while (numbers[right] > pivot)
//                right--;
//
//            if (left < right) {
//                int temp = numbers[right];
//                numbers[right] = numbers[left];
//                numbers[left] = temp;
//            } else {
//                return right;
//            }
//        }
//    }

//    public static void doMergeSortInThreads(int threadsAmount, int... main) {
//        System.out.println(Arrays.toString(main) + "\n");
//        List<SortTask> tasks = new ArrayList<>();
//        int length = main.length / threadsAmount;
//        int lastArrLength = length;
//        if (threadsAmount > 1) {
//            for (int i = 0; i < threadsAmount - 1; i++) {
//                int[] smallerArr = new int[length];
//                System.arraycopy(main, i * length, smallerArr, 0, length);
//                tasks.add(new SortTask(smallerArr));
//                System.out.print(Arrays.toString(smallerArr));
//            }
//            lastArrLength = main.length - threadsAmount * length;
//        }
//        int[] lastSmallerArr = new int[lastArrLength];
//        System.arraycopy(main, length - lastArrLength, lastSmallerArr, 0, lastArrLength);
//        SortTask lastTask = new SortTask(lastSmallerArr);
//        tasks.add(lastTask);
//        System.out.print(Arrays.toString(lastSmallerArr));
//        tasks.stream().forEach(t -> {
//            t.start();
//        });
//        tasks.stream().forEach(t -> {
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        if (threadsAmount > 1) {
//            for (int i = 0; i < threadsAmount - 1; i++) {
//                int[] arr = tasks.get(i).getArr();
//                System.arraycopy(arr, 0, main, i * length, length);
//            }
//            System.arraycopy(tasks.get(tasks.size() - 1).getArr(), 0, main, threadsAmount * length, lastArrLength);
//        } else {
//            System.arraycopy(tasks.iterator().next().getArr(), 0, main, 0, lastArrLength);
//        }
//        System.out.println("\n" + Arrays.toString(main));
//    }