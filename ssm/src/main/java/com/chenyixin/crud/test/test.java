package com.chenyixin.crud.test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class test {

    public static void main(String[] args) throws InterruptedException {
        Integer STR1= 1;
        String str2 = "2";




        ReferenceQueue <Object> referenceQueue = new ReferenceQueue<>();
         Student student =new Student("23",12);
         ClassRoom classRoom = new ClassRoom(student,"2323233",referenceQueue);
      //  WeakReference<Object> weakReference = new WeakReference<>(student,referenceQueue);
        student =null;

        System.out.println("referenceQueue." +(classRoom.student==student?true:false));

        System.out.println("referenceQueue." +classRoom);


        int i=0;


        Object obj1 = new Object();



        Object obj2 = new Object();


        obj1 = obj2;

        System.out.println(obj1==obj2);






        while(true){

                System.gc();

                for (Object x; (x = referenceQueue.poll()) != null; ) {

                    System.out.println("referenceQueue." +x);
                }



        }





    }





}
