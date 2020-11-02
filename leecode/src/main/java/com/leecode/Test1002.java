package com.leecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test1002 {
    public static void main(String[] args) {

    }

  static  class Solution {
        public List<String> commonChars(String[] A) {
            List<String> res = new ArrayList<>();
            int table[] = new int[26];
            Arrays.fill(table,Integer.MAX_VALUE);
            for(String tem:A) {
                int inTable[] = new int [26];
                for(char c:tem.toCharArray()) {
                    inTable[c-'a'] = inTable[c-'a'] +1;
                }
                for(int i=0;i<26;i++) {
                    table[i] = Math.min(table[i],inTable[i]);
                }
            }

            for(int i=0;i<26;i++) {
                int x =table[i];
                for(int l =1;l<=x;l++) {
                    res.add(String.valueOf((char) (i + 'a')));
                }
            }





            return res;

        }
    }


}
