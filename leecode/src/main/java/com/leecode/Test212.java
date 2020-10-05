package com.leecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test212 {

    public static void main(String[] args) {


        char[][] board = {{'a','b'},{'a','a'}};
                String[] words  = new String[7];
        words[0] ="aba";
        words[1] ="baa";
        words[2] ="bab";
        words[3] ="aaab";
        words[4] ="aaa";
        words[5] ="aaaa";
        words[6] ="aaba";
        new  Test212().findWords(board,words);
    }
    Trie trie;
    public List<String> findWords(char[][] board, String[] words) {
        trie = new Trie();
        for(String str : words) {
            trie.insert(str);
        }
        Set<String> res = new HashSet<>();
        StringBuilder path= new StringBuilder();
        int m = board.length;
        if(m ==0) return new ArrayList<>(res);
        int n = board[0].length;
        for(int i =0;i<m;i++){
            for(int j=0;j<n;j++) {
                dfs(board,res,path,i,j);
            }

        }
        return new ArrayList<>(res);
    }
    private void dfs(char[][] board,Set<String> res, StringBuilder path,int i,int j) {
        int m = board.length;
        int n = board[0].length;

        if(i<0 ||j<0||i>=m||j>=n ||board[i][j] =='.'){

            return;
        }

        char temp = board[i][j];
        int len = path.length();
        path.append(temp);
        String temp1 = path.toString();


        //   if(!trie.startWith(temp1)){
        //       path.deleteCharAt(path.length()-1);
        //       return;
        //   }
        if(trie.search(temp1)) {
            res.add(temp1);
            path.deleteCharAt(path.length()-1);
            return;
        }
        board[i][j] ='.';


        dfs(board,res,path,i+1,j);
        dfs(board,res,path,i-1,j);
        dfs(board,res,path,i,j+1);
        dfs(board,res,path,i,j-1);
        path.deleteCharAt(path.length()-1);
        board[i][j] =temp;

    }



    public class Trie {

        private TrieNode root;
        public Trie() {
            root = new TrieNode();
        }
        public void insert(String word) {
            if(word==null){
                return;
            }
            char [] chars = word.toCharArray();
            int index =0;
            TrieNode node = root;
            for(char c:chars){
                index = c-'a';

                if(node.nexts[index] ==null){
                    node.nexts[index] = new TrieNode();
                }
                node =  node.nexts[index];
                node.path++;
            }
            node.end++;
        }
        public boolean startWith(String prefix) {
            if(prefix==null){
                return false;
            }
            char [] chars = prefix.toCharArray();
            int index =0;
            TrieNode node = root;
            for(char c:chars){
                index = c-'a';
                node = node.nexts[index];
                if(node ==null){
                    return false;
                }

            }
            return node.path>0;
        }
        public boolean search(String word) {
            if(word==null) {
                return false;
            }
            char[] chars = word.toCharArray();
            TrieNode node = root;
            int index = 0;
            for(char c :chars) {
                index = c-'a';
                if(node.nexts[index]==null){
                    return false;
                }
                node = node.nexts[index];
            }
            return node.end>0;
        }




    }

    public class TrieNode {
        public int path;
        public int end;
        public TrieNode nexts[];

        public TrieNode() {
            path = 0;
            end = 0;
            nexts = new TrieNode[26];
        }

    }










}
