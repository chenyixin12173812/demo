package com.leecode;

import java.util.ArrayList;
import java.util.List;

public class Test61 {

    class Solution {
        public int[] plusOne(int[] digits) {

            int n = digits.length-1;
            List<Integer> res = new ArrayList<>();
            int floor =0;


            int sum =0;
            while (n >= 0) {

                if(n==digits.length-1){
                    sum = digits[n] +1;
                }else{
                    sum = digits[n] +floor;
                }

                floor= sum/10;
                sum =sum%10;
                res.add(sum);
                n--;
            }

            int a[] = new int[res.size()];

            for (int i = 0; i < res.size(); i++) {
                a[i] = res.get(res.size() -i-1);
            }
            return a;

        }
    }


}
