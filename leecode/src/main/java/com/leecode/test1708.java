package com.leecode;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class test1708 {

    class Solution {
        public int bestSeqAtIndex(int[] height, int[] weight) {
            int len =  height.length;
            Integer[] index = new Integer [height.length];
             for(int i=0;i<len;i++) {
                 index[i] =i;
             }

            LinkedHashMap<Integer,Integer> cuntMap = new LinkedHashMap<>();
            for(Map.Entry<Integer,Integer> x: cuntMap.entrySet()) {

            }

            Arrays.sort(index,(a, b)->{
                return (height[a]==height[b])?(weight[b]-weight[a]):(height[a]-height[b]);
            });
            int dp[] = new int [len+1];
            int max =0;
            for(int i =1;i<=len;i++) {
                dp[i] =1;
                for(int j =i-1;j>=1;j--) {
                    if(weight[index[i-1]]>weight[index[j-1]]) {
                        dp[i] =Math.max(dp[i], dp[j] +1);
                    }

                }
                max = Math.max(max,dp[i]);

            }
            return max;
        }
    }



}
