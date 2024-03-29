package com.leecode;

import java.util.List;

public class Test20 {

    class Solution {
        public int minimumTotal(List<List<Integer>> triangle) {

            int n = triangle.size();
            int [][] dp = new int[triangle.size()][triangle.size()];

            dp[0][0] = triangle.get(0).get(0);

            for (int i =1;i<triangle.size();i++){

                dp[i][0] = dp[i-1][0] + triangle.get(i).get(0);
                for (int j =1;j<i;j++){
                    dp[i][j] = Math.min(dp[i-1][j],dp[i-1][j-1]) + triangle.get(i).get(j);
                }
                dp[i][i] = dp[i-1][i-1] + triangle.get(i).get(i);
            }

            int min = dp[n-1][0];

            for (int i=1;i<n;i++){
                min = Math.min(min,dp[n-1][i]);
            }
            return  min;
        }
    }




}
