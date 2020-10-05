package com.leecode;

import java.util.*;

public class Test321 {

    public static void main(String[] args) {

        int a[] = {2,5,6,4,4,0};
        int b[] = {7,3,8,0,6,5,7,6,2};
        new Test321().maxNumber(a,b,15);
    }

    public int[] maxNumber(int[] nums1, int[] nums2, int k) {


        List<List<Integer>> res = new ArrayList<>();
        int len1= nums1.length;
        int len2= nums2.length;
        for(int i =0; i<=len1;i++){
            List<Integer> l1 = removeK(nums1,len1-i);
            List<Integer> l2 = removeK(nums2,len2-k+i);
            if (l1.size() +l2.size() ==k){
                res.add(mergeTwo(l1,l2));
            }

        }
        int [] ans = new int [k];
        res.sort(new Compare());
        for(int i = 0;i<res.get(0).size();i++){
            ans[i] = res.get(0).get(i);
        }
        return ans;
    }
    public class Compare implements Comparator<List<Integer>>{
        @Override
        public int compare(List<Integer> l1,List<Integer> l2){
            return com(l1, l2);
        }

        public int com(List<Integer> l1, List<Integer> l2) {
            int n = l1.size();
            int m = l2.size();
            int min = Math.min(n,m);
            for(int i=0;i<min ;i++){
                if(l2.get(i) -l1.get(i) !=0){
                    return l2.get(i) -l1.get(i);
                }
            }
            return m-n;
        }
    }

    private List<Integer> mergeTwo(List<Integer> l1,List<Integer> l2) {
        if(l1.isEmpty()){
            return l2;
        }
        if(l2.isEmpty()){
            return l1;
        }
        int i =0;
        int j =0;

        List<Integer> res = new ArrayList<>();

        while(i<l1.size()&&j<l2.size()){
            if(l1.get(i)>l2.get(j)){
                res.add(l1.get(i));
                i++;

            }else if (l1.get(i)<l2.get(j)){
                res.add(l2.get(j));
                j++;
            } else {
              if (new Compare().com(l1.subList(i,l1.get(i)),l2.subList(j,l1.get(j)))>=0){
                  res.add(l2.get(j));
                  j++;
              } else{
                  res.add(l1.get(i));
                  i++;
              }
            }
        }
        while(i<l1.size()){
            res.add(l1.get(i));
            i++;
        }
        while(j<l2.size()){
            res.add(l2.get(j));
            j++;
        }
        return res;

    }



    private List<Integer> removeK(int [] nums ,int k){

        Deque<Integer> stack = new LinkedList<>();
        for(int num :nums) {
            while(!stack.isEmpty() && k>0 && stack.peekLast()<num){
                stack.removeLast();
                k--;
            }
            stack.addLast(num);
        }

        for(int i=0;i<k;i++){
            if(!stack.isEmpty()){
                stack.removeLast();
            }

        }
        return new ArrayList<>(stack);
    }




}
