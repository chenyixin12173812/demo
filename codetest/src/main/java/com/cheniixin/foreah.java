package com.cheniixin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class foreah {


    public static void main(String[] args) {

//        List<String> strings = new ArrayList<>();
//
//        strings.add("chen");
//        strings.add("chen");
//        strings.add("uo");
//        strings.add("xcinn");
//        strings.add("xin");
//
//        for (int i =0 ;i< strings.size();i++) {
//            if (strings.get(i).equals("chen")){
//                //  strings.add("ssss");
//                strings.remove(i);
//            }
//        }


        List<String> strings = new LinkedList<>();

        strings.add("1");
        strings.add("2");
//        strings.add("1");
        strings.add("3");
        strings.add("4");
        strings.add("5");
//        strings.add("xcinn");
//        strings.add("xin");

//        for (int i =0 ;i< strings.size();i++) {
//            if (strings.get(i).equals("chen")){
//                //  strings.add("ssss");
//                strings.remove(i);
//            }
//        }




        System.out.println(strings);


        for (String string : strings) {

            System.out.println(string);

            if (string.equals("1")){
              //  strings.add("ssss");
                strings.remove("1");
                strings.remove("2");
            }
        }
        System.out.println(strings);

    }


}
