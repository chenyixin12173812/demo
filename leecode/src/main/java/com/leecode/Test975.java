package com.leecode;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class Test975 {

    public static void main(String[] args) {
        int A[] = {10, 13, 12, 14, 15};
        System.out.println(new Solution().oddEvenJumps(A));
    }


    static class Solution {
        public int oddEvenJumps(int[] A) {

            int len = A.length;
            if (len == 0) {
                return 0;
            }
            if (len == 1) {
                return 1;
            }
            boolean dp[][] = new boolean[len][2];

            Integer[] arr = new Integer[len];
            for (int i = 0; i < len; i++) {
                arr[i] = i;
            }
            Arrays.sort(arr, (a, b) -> A[a] == A[b] ? a - b : A[a] - A[b]);
            int[] up = oder(arr);
            Arrays.sort(arr, (a, b) -> A[a] == A[b] ? a - b : A[b] - A[a]);
            int[] down = oder(arr);

            dp[len - 1][0] = true;  //偶数
            dp[len - 1][1] = true;  //奇数

            for (int i = len - 2; i >= 0; i--) {

                int lastter = up[i];
                int smaller = down[i];
                if (lastter != -1) {
                    dp[i][1] = dp[lastter][0];
                }
                if (smaller != -1) {
                    dp[i][0] = dp[smaller][1];
                }

            }
            int cunt = 0;
            for (int i = 0; i < len; i++) {
                if (dp[i][1]) {
                    cunt++;
                }
            }

            return cunt;


        }
       //503下一个更大元素 II
        private int[] oder(Integer arr[]) {
            Deque<Integer> deque = new LinkedList<>();
            int[] res = new int[arr.length];
            Arrays.fill(res, -1);
            deque.push(arr[0]);
            for (int i = 1; i < arr.length; i++) {
                while (!deque.isEmpty() && deque.peek() < arr[i]) {
                    res[deque.poll()] = arr[i];
                }
                deque.add(arr[i]);
            }
            return res;

        }


    }


}
