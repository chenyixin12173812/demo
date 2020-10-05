package com.leecode;

public class Test421 {
    public static void main(String[] args) {
        int a[] = {-3,-8};
        System.out.println(new Solution().findMaximumXOR(a));
    }


    static  class Solution {
        public int findMaximumXOR(int[] nums) {
            Tire tire = new Tire();
            for(int num:nums) {
                tire.add(num);
            }
            return cacul(nums,tire);
        }

        private int cacul(int [] nums, Tire tire){
            int max = Integer.MIN_VALUE;
            for(int num:nums) {
                max = Math.max(tire.cacul(num),max);
            }
            return max;
        }

        class Tire {
            TireNode root;
            public Tire(){
                root = new TireNode();
            }
            public void add(int num){
                TireNode cur = root;
                for(int i =31;i>=0;i--) {
                    int bit = ((num>>i) &1);
                    if(cur.nexts[bit]==null) {
                        cur.nexts[bit] = new TireNode();
                    }
                    cur = cur.nexts[bit];
                }
            }
            public int cacul(int num){
                int ans = 0;
                TireNode cur = root;
                for(int i =31;i>=0;i--) {
                    int bit = ((num>>i) &1);
                    int path = (bit^1);
                    if(cur.nexts[path]==null){
                        cur = cur.nexts[bit];
                    }else{
                        cur = cur.nexts[path];
                        ans |= (1<<i);
                    }
                }
                return ans;
            }
        }
        class TireNode {
            TireNode [] nexts;
            public TireNode(){
                nexts = new TireNode[2];
            }
        }


    }


}
