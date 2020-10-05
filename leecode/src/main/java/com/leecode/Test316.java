package com.leecode;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Test316 {

    public static void main(String[] args) {
        System.out.println(new Test316().removeDuplicateLetters("bcabc"));
    }

        public String removeDuplicateLetters(String s) {

            int cunt [] = new int[26];
            char [] chars = s.toCharArray();
            for(char c : chars){
                cunt[c-'a']++;
            }


            Deque<Character> stack = new LinkedList<>();

            Set<Character> set = new HashSet<>();
            for(char c : chars){
                if(set.contains((Character)c)){
                    cunt[c-'a'] --;
                    continue;
                }
                while(!stack.isEmpty() && stack.peekLast()>c && cunt[stack.peekLast()-'a']>1){
                    cunt[stack.peekLast()-'a'] --;
                    set.remove((Character)stack.peekLast());
                    stack.removeLast();
                }
                stack.addLast(c);
                set.add(c);
            }


            StringBuilder ans = new StringBuilder() ;

            for(char c : stack) {
                ans.append(c);

            }

            if(ans.length()==0){
                return "";
            }

            return ans.toString();
        }





}
