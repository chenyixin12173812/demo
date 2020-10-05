package com.leecode;

public class Test151 {

    public static void main(String[] args) {
        new Solution().reverseWords("  cheyii xin ");
    }


    static class Solution {
        public String reverseWords(String s) {

            StringBuilder sb = trimSpace(s);
            reverse(sb,0,sb.length()-1);
            reverseEachWord(sb);
            return sb.toString();

        }
        void reverseEachWord(StringBuilder s){
            int start =0;
            int end = 0;
            int n = s.length();
            while(start<n) {
                while(end<n &&s.charAt(start)!=' '){
                    end++;
                }
                reverse(s,start,end-1);
                start =end+1;
                end ++;


            }




        }


        StringBuilder trimSpace(String s){

            int left =0;
            int right = s.length()-1;
            while(left<=right && s.charAt(left) ==' '){
                    left++;
            }
            while(left<=right && s.charAt(right) ==' '){
                    right--;
            }

            StringBuilder sb = new StringBuilder();
            while(left<=right){
                if(s.charAt(left)!=' '){
                    sb.append(s.charAt(left));
                }else if(s.charAt(sb.length()-1) !=' '){
                    sb.append(s.charAt(left));
                }

                left++;
            }

            return sb;




        }


        void reverse(StringBuilder s,int i,int j){


            while(i<j){
                char temp = s.charAt(i);
                s.setCharAt(i,s.charAt(j));
                s.setCharAt(j,temp);
                i++;
                j--;
            }


        }


    }



}
