package com.leecode;

import java.util.*;

public class Test440 {
    public static void main(String[] args) {
        new Solution().findKthNumber(13,2);
    }



    static class Solution {
        public int findKthNumber(int n, int k) {

            Map<Character,Integer> map = new HashMap<>();

            PriorityQueue<Integer> q = new PriorityQueue<>((a, b)->{
//                String str1 = String.valueOf(a);
//                String str2 = String.valueOf(b);
//                return str2.compareTo(str1);
                return b-a;
            });

            for(int i =1;i<=n;i++) {
                q.offer(i);

            }


            List<Integer> ans = new ArrayList<>();
          while(!q.isEmpty())  {
              ans.add(q.poll());
          }


            return q.peek();
        }
    }


}
