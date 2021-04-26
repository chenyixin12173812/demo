package com.leecode.base;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MergeSort {

    
   public int  test(int x) {
        while (true) {
            if(x==1) {
                return  1;
            }
        }
    }


    public static void main(String[] args) {
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(8,12,
                        19, TimeUnit.MINUTES, new ArrayBlockingQueue<>(200));

        for(int i =0;i<4;i++) {
            executor.execute( new Mythread());
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(executor);



    }



}
