package com.leecode;

public class Test165 {

    public static void main(String[] args) {
        new Solution().compareVersion("0.1","1.1");
    }


   static class Solution {
       public int compareVersion(String version1, String version2) {

           String [] strs1= version1.split("\\.");
           String [] strs2= version2.split("\\.");

           int len1 = strs1.length;
           int len2 = strs2.length;
           int tem1=0;
           int tem2 =0;
           for(int i=0;i<Math.min(len1,len2);i++) {
               tem1 = Integer.parseInt(strs1[i]);
               tem2 = Integer.parseInt(strs2[i]);
               if(tem1>tem2) {
                   return 1;
               } else if(tem1<tem2) {
                   return -1;
               }
           }
           int sum =0;
           if(len1>len2) {
               for(int i=len2;i<len1;i++)  {
                   sum += Integer.parseInt(strs1[i]);
               }
               return campare(sum);
           } else {
               for(int i=len1;i<len2;i++)  {
                   sum = Integer.parseInt(strs2[i]);
               }
               return campare(sum);
           }
       }
       private int campare(int a) {
           return Integer.compare(a, 0);
       }


   }








}
