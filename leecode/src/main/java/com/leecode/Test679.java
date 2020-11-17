package com.leecode;

import sun.nio.ch.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class Test679 {


    public static void main(String[] args) {
        int a[] ={1,5,9,1};

        System.out.println(new Solution().judgePoint24(a));
    }

   static class Solution {
       boolean used[] = new boolean[4];
        public boolean judgePoint24(int[] nums) {
        List<List<Integer>> re = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        dfs(nums,0,re,path);
            System.out.println(re.size());

         for(List <Integer> num:re) {
             if (cal(num)==24){
                 return true;
             }
         }
            return false;
        }

        private  int cal(List <Integer> nums){
            if (nums.size()==1) {
                return nums.get(0);
            }

            int a = nums.remove(0);
            int b = nums.remove(1);

            return 0;
        }



        private void dfs(int [] nums,int index,List<List<Integer>> re,List<Integer> path) {
            int n = nums.length;
            if(index==n) {
                re.add(new ArrayList<>(path));
                return ;
            }
            for(int i =0;i<n;i++){
                if (used[i]) {
                    continue;
                }
                path.add(nums[i]);
                used[i] = true;
                dfs(nums,index+1,re,path);
                path.remove(path.size()-1);
                used[i] = false;
            };
        }
        private boolean canDiv (int a,int b) {
            if(b ==0) {
                return false;
            }
            return a % b == 0;

        }

    }




}
