package com.leecode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Test564 {

    public static void main(String[] args) {


        System.out.println(nearestPalindromic("11"));

    }
    public static String nearestPalindromic(String n) {

        long num = Long.parseLong(n);

        if (num <= 9L) return toString(num - 1);

        long y = num;
        for (int i = 1; i <= n.length() / 2; i++) {
            y = y /10;
        }

        long p[] = new long[5];
         p[0] = buildNum(y, n.length() % 2 == 0);
         p[1] = buildNum(y + 1, n.length() % 2 == 0);
         p[2] = buildNum(y - 1, n.length() % 2 == 0);
         p[3] = 9L;

        for (int i = 1; i < n.length() - 1; i++) {
            p[3] = p[3] * 10 + 9;
        }

        p[4] = 1L;
        for (int i = 1; i <= n.length(); i++) {
            p[4] = p[4] * 10;
        }
        p[4] = p[4] + 1;

        return calute(p,num);

    }

    private static String calute(long[] p, long num) {
        List<Long> result = new ArrayList<>();

        Long min = Math.abs(p[0]-num) ==0 ?Integer.MAX_VALUE:Math.abs(p[0]-num) ;

        for (int i =1;i<p.length;i++){
            min =Math.min( min,Math.abs(p[i]-num)==0?Integer.MAX_VALUE:Math.abs(p[i]-num));
        }

        for (int i =0;i<p.length;i++){
            if (Math.abs(p[i]-num) ==min){
                result.add(p[i]);
            }
        }


        Long re = result.get(0);
        for (int i =1;i<result.size();i++){
            re = Math.min(re,result.get(i));
        }

        return toString(re);


    }

    private static String toString(long x){

        return String.valueOf(x);
    }

    private static long buildNum(long n,boolean flag){

        if(n ==0L) return 0L;

        long x = n;

        Queue<Long> q = new LinkedList<>();

        while(n!=0L){
            if (flag){
                q.offer(n%10);
                x = x *10;
            }
            flag = true;
            n = n/10;

        }
       long y= 0L;
        while(!q.isEmpty()){
            y = y*10 + q.poll();
        }
        return x+y;

    }


}
