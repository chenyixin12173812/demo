package com.leecode;



import java.util.HashMap;
import java.util.Map;

public class Test327 {


    class Solution {
        public int countRangeSum(int[] nums, int lower, int upper) {
            int pre = 0;
            int cunt =0;
            Map<Integer,Integer> map = new HashMap<>();
            map.put(0,1);
            for(int num:nums) {
                pre = pre+num;

                map.put(pre,map.getOrDefault(pre,0)+1);

                for(Map.Entry<Integer,Integer> en:map.entrySet()) {
                    if(en.getKey()>=lower&&en.getKey()<=upper){
                        cunt += en.getValue();
                    }
                }

            }
            return cunt;

        }
    }

}
