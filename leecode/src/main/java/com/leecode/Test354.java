package com.leecode;

import java.util.Arrays;

public class Test354 {

    class Solution {
        public int maxEnvelopes(int[][] envelopes) {

            Arrays.sort(envelopes,(int a[], int b[])->{
                return a[0] == b[0] ? a[1]-b[1] : a[0] -b[0];
            });

            int n = envelopes[0].length;
            int val[] = new int [n];

            for(int i =0;i<n;i++){
                val[i] = envelopes[i][1];
            }

            return lengthOflsc(val);


        }

        private int lengthOflsc(int [] val){


            return 0;




        }



    }


}
