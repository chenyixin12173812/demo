package com.leecode;

import java.util.*;

public class Test1345 {



    class Solution {
        public int minJumps(int[] arr) {
            int cunt =0;
            int len = arr.length;
            if(len==0) {
                return cunt;
            }
            boolean used[] = new boolean[len+1];
            Deque<Integer> q = new LinkedList<>();
            q.offer(0);
            used[0] =true;

            Map<Integer, List<Integer>> map = new HashMap<>();

            for(int i=0; i<arr.length;i++) {
                if(map.containsKey(arr[i])) {
                    map.get(arr[i]).add(i);
                } else {
                    map.put(arr[i],new ArrayList<Integer>());
                }

            }


            while(!q.isEmpty()) {

                int size = q.size();
                for(int i= 0;i<size;i++) {
                    int index = q.poll();
                    if(index==len-1) {
                        return cunt;
                    }
                    if(index+1<len&&!used[index+1]) {
                        q.offer(index+1);
                        used[index+1] =true;
                    }
                    if(index-1>=0 &&!used[index-1]) {
                        q.offer(index-1);
                        used[index-1] =true;
                    }
                    for(int same:map.get(arr[index])) {
                        if(!used[same]) {
                            q.offer(same);
                            used[same] =true;
                        }

                    }
                }
                cunt++;
            }
            return cunt;
        }




    }

}
