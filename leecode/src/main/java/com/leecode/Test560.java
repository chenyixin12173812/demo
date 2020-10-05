package com.leecode;

import java.util.HashMap;
import java.util.Map;

public class Test560 {

    public static void main(String[] args) {

         int[] a= {1,1,0,1};
        System.out.println( new Solution().method1(a,2));
        System.out.println(new Solution().method2(a,2));
    }


    static class Solution {

        //https://leetcode-cn.com/problems/subarray-sum-equals-k/solution/he-wei-kde-zi-shu-zu-by-leetcode-solution/
        public int subarraySum(int[] nums, int k) {

            return method2(nums,k);

        }
        // 暴力法
        int method1(int[] nums, int k){
            int cunt = 0;
            for(int i=0;i<nums.length;i++){
                int sum =0;
                for(int j =i;j<nums.length;j++){
                    sum = sum + nums[j];
                    if(sum==k){
                        cunt++;
                    }
                }
            }

            return cunt;

        }
        int method2(int[] nums, int k){

            int pre = 0;
            int cunt =0;
            Map<Integer,Integer> map = new HashMap<>();
            //有就得为1
            map.put(0,1);
            for(int i =0;i<nums.length;i++){
                pre = pre + nums[i];

                if(map.containsKey(pre-k)){
                    cunt  = cunt + map.get(pre-k);
                }
                map.put(pre,map.getOrDefault(pre,0)+1);
            }

            return cunt;
        }
    }



}
