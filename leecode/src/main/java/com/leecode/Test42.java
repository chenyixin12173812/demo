package com.leecode;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

public class Test42 {


    class Solution {
        //https://leetcode-cn.com/problems/trapping-rain-water/solution/xiang-xi-tong-su-de-si-lu-fen-xi-duo-jie-fa-by-w-8/
        public int trap(int[] height) {

           // return method1(height);
            //return method2(height);
            return method3(height);
        }
        // m x n
        int method1(int[] height){
            int max = 0;
            int sum = 0;
            for (int i=0;i<height.length;i++){
                max = Math.max(max,height[i]);
            }
            for (int i =1;i<=max;i++){
                boolean isStart =false;
                int temSum =0;
                for (int j =0;j<height.length;j++){
                    if (isStart && height[j]<i){
                        temSum++;
                    }
                    if (height[j]>=i){
                        sum= sum+temSum;
                        temSum =0;
                        isStart = true;
                    }
                }
            }
            return sum;
        }
        //nxn
        int method2(int[] height){

            int sum = 0;
            for (int i=1;i<height.length-1;i++){

                int leftMax =0;
                for (int j=0;j<i;j++){
                    if (height[j]>leftMax){
                        leftMax = height[j];
                    }
                }

                int rightMax =0;
                for (int j = i+1;(i+1<height.length) &&(j<height.length);j++){
                    if (height[j]>rightMax){
                        rightMax = height[j];
                    }

                }

                int min = Math.min(leftMax,rightMax);

                if (min>height[i]){
                    sum = sum + (min-height[i]);
                }



            }

            return sum;
        }
        int method3(int[] height){

            int sum = 0;
            Deque<Integer> stack = new LinkedList<>();

            for (int i=0;i<height.length;i++) {

                while(!stack.isEmpty() && height[stack.peek()]<height[i]){
                    int h = height[stack.peek()];
                    stack.pop();
                    if (stack.isEmpty()){
                        break;
                    }
                    int distance= i-stack.peek()-1;

                    int min = Math.min(height[stack.peek()], height[i]);
                    sum = sum + distance * (min - h);

                }
                stack.push(i);

            }

            return sum;
        }

    }





}
