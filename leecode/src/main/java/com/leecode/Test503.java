package com.leecode;

import java.util.*;

public class Test503 {
    public static void main(String[] args) {

        int  []a = {1,2,1};

        Set<List<Integer>> set = new HashSet<>();
        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        List<Integer> list2 = new ArrayList<>();
        list2.add(1);

        set.add(list1);
        set.add(list2);

        System.out.println(set.size());

        new Solution().nextGreaterElements(a);

    }


    static class Solution {
        public int[] nextGreaterElements(int[] nums) {
            int len = nums.length;

            int index =0;
            int ans[] = new int [len];
            Arrays.fill(ans,-1);
            Deque<Integer> stack = new LinkedList<>();
            for(int i =0;i<(len+index) ;i++) {
                while(!stack.isEmpty()&& ((i<len &&nums[i%len] >nums[stack.peek()%len]) ||(i>len &&nums[i%len] >=nums[stack.peek()%len]))) {
                    ans[stack.pop()%len] = nums[i%len];
                }
                stack.push(i);
                if (index<len) {
                    index++;
                }

            }


            return ans;

        }
    }






}
