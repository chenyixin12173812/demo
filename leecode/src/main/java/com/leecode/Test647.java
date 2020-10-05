package com.leecode;

public class Test647 {

    public static void main(String[] args) {
        new Solution().countSubstrings("aaa");
    }



    static class Solution {
        public int countSubstrings(String s) {

            int n = s.length();
            boolean dp [][] = new boolean[n+1][n+1];
            dp[0][0] = true;

            for(int j=1;j<n;j++){
                for(int i =j;i>=0;i--){
                    if(s.charAt(i) == s.charAt(j) &&(j-i<2||dp[i+1][j-1])){
                        dp[i][j] = true;
                    }
                }
            }

            int result =0;

            for(int i =0;i<n;i++){
                for(int j =i;j<n;j++){
                    if(dp[i][j]){
                        result++;

                    }
                }

            }






            return result;

        }
    }




}
