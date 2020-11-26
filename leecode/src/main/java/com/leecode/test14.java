package com.leecode;

public class test14 {

    class Solution {

        public String longestCommonPrefix(String[] strs) {



                for (int j =1;j<strs[0].length();j++){
                    for (int i =1;i<strs.length;i++){
                    if (!(strs[0].substring(0,j).equals(strs[i].substring(0,j)))){


                        return strs[0].substring(0,j);
                    }


                }

            }

            return "" ;
        }
    }





}
