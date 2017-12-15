package com.jj.algo.sorting;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yejaz on 12/30/2016.
 * http://javadecodedquestions.blogspot.sg/2013/02/data-structure-questions-for-java.html
 */
public class Sorting {

    public static void main(String args[]) {
        //testing our bubble sort method in Java
        int[] unsorted = {32, 39,21, 45, 23, 3};
        //bubbleSort(unsorted);
        System.out.println("unsorted array before sorting : " + Arrays.toString(unsorted));
        bubbleSortRecursive(unsorted, unsorted.length);

        //one more testing of our bubble sort code logic in Java
        int[] test = { 5, 3, 2, 1};
        //bubbleSort(test);
        System.out.println("unsorted array before sorting : " + Arrays.toString(test));
        bubbleSortRecursive(test, test.length);

        //quick sort
        int[] unsorted2 = {32, 39,21, 45, 23, 3};
        quickSort(unsorted2, 0, unsorted2.length - 1);
        System.out.println("sorted array using QuickSort: " + Arrays.toString(unsorted2));

        //merge sort
        int[] unsorted3 = {32, 39,21, 45, 23, 3};
        mergeSort(unsorted3, 0, unsorted2.length);
        System.out.println("sorted array using serial MergeSort: " + Arrays.toString(unsorted3));

        //merge sort using RecursiveAction
        long[] unsorted4 = {32, 39,21, 45, 23, 3};
        (new MergeSortTask(unsorted4)).invoke();
        System.out.println("sorted array using parallel MergeSortTask: " + Arrays.toString(unsorted4));

        List list;
    }

    //note that higherIndex is not the last element index
    public static void mergeSort(int[] array, int lowerIndex, int higherIndex) {
        if (lowerIndex + 1 < higherIndex) {
            //int middle = lowerIndex + (higherIndex - lowerIndex) / 2;
            int middle = (lowerIndex + higherIndex) >>> 1;
            // Below step sorts the left side of the array
            mergeSort(array, lowerIndex, middle);
            // Below step sorts the right side of the array
            mergeSort(array, middle, higherIndex);
            // Now merge both sides
            mergeParts(array, lowerIndex, middle, higherIndex);
        }
    }

    private static void mergeParts(int[] array, int lowerIndex, int middle, int higherIndex) {
        //super smart
        int[] buf = Arrays.copyOfRange(array, lowerIndex, middle);
        for (int i = 0, j = lowerIndex, k = middle; i < buf.length; j++) {
            //take buf[i} first, then i++
            array[j] = (k == higherIndex || buf[i] < array[k]) ? buf[i++] : array[k++];
        }
    }

    public static void quickSort(int[] arr, int low, int high) {
        if (arr == null || arr.length == 0)
            return;

        if (low >= high)
            return;

        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle];

        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }

            while (arr[j] > pivot) {
                j--;
            }

            if (i <= j) {
                /*int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;*/
                swap(arr, i, j);
                i++;
                j--;
            }
        }

        // recursively sort two sub parts
        if (low < j)
            quickSort(arr, low, j);

        if (high > i)
            quickSort(arr, i, high);
    }

    /*
     * In bubble sort we need n-1 iteration to sort n elements
     * at end of first iteration larget number is sorted and subsequently numbers smaller
     * than that.
     */
    public static void bubbleSort(int[] unsorted) {
        System.out.println("unsorted array before sorting : " + Arrays.toString(unsorted));
        // Outer loop - need n-1 iteration to sort n elements
        for(int i=0; i<unsorted.length -1; i++){

            //Inner loop to perform comparision and swapping between adjacent numbers
            //After each iteration one index from last is sorted
            for(int j= 1; j<unsorted.length -i; j++){

                //If current number is greater than swap those two
                if(unsorted[j-1] > unsorted[j]){
                    swap(unsorted, j-1, j);
                }
            }
            System.out.printf("unsorted array after %d pass %s: %n", i+1, Arrays.toString(unsorted));
        }
    }

    public static void bubbleSortRecursive(int[] unsorted, int length) {
        if (length == 1) {
            return;
        }

        //this is still iterative
        /*for (int i = 1; i < length; i++) {
            if (unsorted[i-1] > unsorted[i]) {
                swap(unsorted, i-1, i);
            }
        }*/
        bubbleSwapRecursive(unsorted, length, 1);

        System.out.printf("unsorted array after %d pass %s: %n", unsorted.length - length + 1, Arrays.toString(unsorted));
        bubbleSortRecursive(unsorted, length-1);
    }

    public static void bubbleSwapRecursive(int[] unsorted, int length, int index) {
        if (index == length) {
            return;
        }

        if (unsorted[index-1] > unsorted[index]) {
            swap(unsorted, index-1, index);
        }
        bubbleSwapRecursive(unsorted, length, index+1);
    }

    public static void swap(List list, int pos1, int pos2) {
        Collections.swap(list, pos1, pos2);
    }
    public static void swap(AtomicInteger a, AtomicInteger b) {
        a.set(b.getAndSet(a.get()));
    }

    public static void swap(int[] arrays, int pos1, int pos2) {
        /*int temp = arrays[pos1];
        arrays[pos1] = arrays[pos2];
        arrays[pos2] = temp;
        */

        //or use Apache's API which also uses a temp variable but it's more robust in handling edge cases
        ArrayUtils.swap(arrays, pos1, pos2);
    }

    public static void swap(int[] arrays1, int[] arrays2, int pos1, int pos2) {
        int temp = arrays1[pos1];
        arrays1[pos1] = arrays2[pos2];
        arrays2[pos2] = temp;
    }
}

class MergeSortTask extends RecursiveAction {
    static final int THRESHOLD = 3;//1000;
    final long[] array;
    final int lo, hi;

    //note that hi is not the last element index, it' more like the length
    MergeSortTask(long[] array, int lo, int hi) {
        this.array = array; this.lo = lo; this.hi = hi;
    }
    MergeSortTask(long[] array) { this(array, 0, array.length); }

    protected void compute() {
        if (hi - lo < THRESHOLD)
            sortSequentially(lo, hi);
        else {
            int mid = (lo + hi) >>> 1;
            invokeAll(new MergeSortTask(array, lo, mid),
                    new MergeSortTask(array, mid, hi));
            merge(lo, mid, hi);
        }
    }
    // implementation details follow:
    //QuickSort
    void sortSequentially(int lo, int hi) {
        Arrays.sort(array, lo, hi);
    }

    void merge(int lo, int mid, int hi) {
        long[] buf = Arrays.copyOfRange(array, lo, mid);
        for (int i = 0, j = lo, k = mid; i < buf.length; j++)
            array[j] = (k == hi || buf[i] < array[k]) ?
                    buf[i++] : array[k++];
    }
}