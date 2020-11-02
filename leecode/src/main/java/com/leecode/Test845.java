package com.leecode;

public class Test845 {
    public static void main(String[] args) {

        int a[] = {0,1,2,3,4,5,4,3,2,1,0};
        new Solution().longestMountain(a);


    }
    static class Solution {
        public int longestMountain(int[] A) {

            int len = A.length;
            if(len<3) {
                return 0;
            }
            int max =0;
            boolean des = true;

            int sart =0;
            int end =0;
            for(int i =1;i<len;i++) {
                if(A[i-1]<A[i]) {
                    if(des) {
                        end =i;
                    } else{
                        sart =i-1;
                        end =i;
                        des =true;
                    }
                } else if(A[i-1]>A[i]) {
                    if(des) {
                        end =i;
                        des =false;
                    } else{
                        end =i;
                    }

                } else {
                    des =true;
                    sart=i;
                    end =i;
                }
                if(end-sart+1>=3) {
                    max = Math.max(max,end-sart+1);
                }

            }

            return max;



        }
    }


}
