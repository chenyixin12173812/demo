package com.leecode;

import java.util.Deque;
import java.util.LinkedList;

public class Test1438 {
    public static void main(String[] args) {
        int a [] = {8,2,4,7};
        new Solution().longestSubarray(a,4);
    }

    static class Solution {
        public int longestSubarray(int[] nums, int limit) {

            int start =0;
            int end = 0;
            Deque<Integer> minStack = new LinkedList<>();
            Deque<Integer> MaxStack = new LinkedList<>();

            int max= 0;
            while(start<=end &&end<=nums.length-1) {

                while(!minStack.isEmpty() && nums[minStack.peekLast()]<nums[end]) {
                    minStack.removeLast();
                }
                minStack.addLast(end);

                while(!MaxStack.isEmpty() && nums[MaxStack.peekLast()]>nums[end]) {
                    MaxStack.removeLast();
                }
                MaxStack.addLast(end);
                end++;
                //
                while (start<=end && (nums[minStack.peekFirst()]-nums[MaxStack.peekFirst()]>limit) ) {

                    start++;
                    System.out.println(start);
                    while(!MaxStack.isEmpty() && MaxStack.peekFirst()<start) {
                        MaxStack.removeFirst();
                    }
                    while(!minStack.isEmpty() &&minStack.peekFirst()<start) {
                        minStack.removeFirst();
                    }

                }
                max =Math.max(max, end -start +1);

            }

            return max;


        }
    }










}
