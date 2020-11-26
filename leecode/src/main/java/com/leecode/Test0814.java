package com.leecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test0814 {
    public static void main(String[] args) {
      int c=  new Solution().countEval("1^0|0|1",0);
        System.out.println(c);
    }

    static  class Solution {
        List<Character> op= new ArrayList<>();
        List<Boolean> bools = new ArrayList<>();
        Map<String,List<Boolean>> map= new HashMap<>();
        public int countEval(String s, int result) {

            if(s.length()==1) {
                if(s.equals(result+"")){
                    return 1;
                }  else {
                    return 0;
                }

            }
            split(s);
            List<Boolean> w =  partion(0,bools.size()-1);
            int cunt =0;
            for(boolean b:w) {
                if(b==(result==1)){
                    cunt++;
                }
            }

            return cunt;
        }

        private void split(String s) {
            char []chars = s.toCharArray();
            for(int i =0;i<chars.length;i++) {
                if(i%2==0) {
                    if(chars[i]=='1') {
                        bools.add(true);
                    }else{
                        bools.add(false);
                    }
                } else{
                    op.add(chars[i]);
                }
            }
        }

        private List<Boolean> partion(int start,int end) {
            List<Boolean> res = new ArrayList<>();
            if(start==end){
                res.add(bools.get(start));
                return res;
            }
            for(int i = start;i<end;i++) {
                String keyleft = start+"@"+i;
                String keyRight = (i+1)+"@"+end;
                List<Boolean> ls;
                if(map.containsKey(keyleft) ) {
                      ls   =  map.get(keyleft) ;
                }else{
                    ls   = partion(start,i);
                }
                List<Boolean> rs;
                if(map.containsKey(keyRight) ) {
                    rs  =  map.get(keyleft) ;
                }else{
                    rs  = partion(start,i);
                }
                for(boolean l:ls) {
                    for(boolean r:rs)  {
                        res.add(compute(i,l,r));
                    }
                }
            }
            return res;
        }
        private boolean compute(int index,boolean l,boolean r) {
            char c = op.get(index);
            if(c=='^') {
                return l ^ r;
            } else if(c=='|') {
                return l | r;
            } else {
                return l & r;
            }
        }

    }



}
