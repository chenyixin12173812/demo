package com.leecode;

import java.util.ArrayList;
import java.util.List;

public class Test1306 {

    public static void main(String[] args) {
        int a[] ={4,2,3,0,3,1,2};
       boolean result = new Solution().canReach(a,5);
        System.out.println(result);
    }


   static   class Solution {
        public boolean canReach(int[] arr, int start) {


            List<Integer> path = new ArrayList<>();
            return canReach(arr,path,start);
        }

        private boolean canReach(int[] arr,List<Integer> path, int start){

            int n = arr.length;
            if(start>n-1 ||start<0||path.contains(start)) {
                return false;
            }
            path.add(start);
            if(arr[start]==0) {
                path.remove(path.size()-1);
                return true;
            }
//            if (arr[start]==0) {
//                path.remove(path.size()-1);
//                return false;
//            }
            if(canReach(arr,path,start+arr[start])){
               path.remove(path.size()-1);
                return true;
            }
            if(canReach(arr,path,start-arr[start])) {
               path.remove(path.size()-1);
                return true;
            }
            path.remove(path.size()-1);
            return false;
        }




    }

}
