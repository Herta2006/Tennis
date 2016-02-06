package samples.algorithms;

import java.util.ArrayList;

public class Sorter {

    public static void recursiveQuickSort(int[] arr, int left, int right) {
        // For Recusrion
        if (left < right) {
            int pivot = partition(arr, left, right);

            if (pivot > 1)
                recursiveQuickSort(arr, left, pivot - 1);

            if (pivot + 1 < right)
                recursiveQuickSort(arr, pivot + 1, right);
        }
    }

    static public void iterativeMergeSort(int[] numbers, int left, int right) {
        int mid;
        if (right <= left)
            return;

        class MergePosInfo {
            public int left;
            public int mid;
            public int right;
        }

        ArrayList<MergePosInfo> list1 = new ArrayList<>();
        ArrayList<MergePosInfo> list2 = new ArrayList<>();

        MergePosInfo info = new MergePosInfo();
        info.left = left;
        info.right = right;
        info.mid = -1;

        list1.add(info);

        while (true) {
            if (list1.size() == 0)
                break;

            left = list1.get(0).left;
            right = list1.get(0).right;
            list1.remove(0);

            mid = (right + left) / 2;

            if (left < right) {
                MergePosInfo info2 = new MergePosInfo();
                info2.left = left;
                info2.right = right;
                info2.mid = mid + 1;
                list2.add(info2);

                info.left = left;
                info.right = mid;
                list1.add(info);

                info.left = mid + 1;
                info.right = right;
                list1.add(info);
            }
        }

        for (MergePosInfo aList2 : list2) {
            merge(numbers, aList2.left, aList2.mid, aList2.right);
        }

    }

    static public void recursiveMergeSort(int[] numbers, int left, int right) {
        int mid;
        if (right > left) {
            mid = (right + left) / 2;
            recursiveMergeSort(numbers, left, mid);
            recursiveMergeSort(numbers, (mid + 1), right);
            merge(numbers, left, (mid + 1), right);
        }
    }

    static public void merge(int[] numbers, int left, int mid, int right) {
        int[] temp = new int[25];
        int i, left_end, num_elements, tmp_pos;

        left_end = (mid - 1);
        tmp_pos = left;
        num_elements = (right - left + 1);

        while ((left <= left_end) && (mid <= right)) {
            if (numbers[left] <= numbers[mid])
                temp[tmp_pos++] = numbers[left++];
            else
                temp[tmp_pos++] = numbers[mid++];
        }

        while (left <= left_end)
            temp[tmp_pos++] = numbers[left++];

        while (mid <= right)
            temp[tmp_pos++] = numbers[mid++];

        for (i = 0; i < num_elements; i++) {
            numbers[right] = temp[right];
            right--;
        }
    }

    public static int partition(int[] numbers, int left, int right) {
        int pivot = numbers[left];
        while (true) {
            while (numbers[left] < pivot)
                left++;

            while (numbers[right] > pivot)
                right--;

            if (left < right) {
                int temp = numbers[right];
                numbers[right] = numbers[left];
                numbers[left] = temp;
            } else {
                return right;
            }
        }
    }

    private Sorter() {
    }
}
