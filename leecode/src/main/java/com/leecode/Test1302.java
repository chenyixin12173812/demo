package com.leecode;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

//https://leetcode-cn.com/problems/deepest-leaves-sum/submissions/
public class Test1302 {

    public static void main(String[] args) {

    }



      public class TreeNode {
          int val;
          TreeNode left;
          TreeNode right;
         TreeNode(int x) { val = x; }
      }

    class Solution {

        private int maxdep= 0;
        private int total = 0;
        public int deepestLeavesSum(TreeNode root) {
           // dfs(root,0);
            dfs2(root);
           // bfs(root);
            return total;
        }
        public void dfs(TreeNode treeNode,int dep){
            if (treeNode == null) {
                return;
            }
            if (dep > maxdep) {
                maxdep = dep;
                total = treeNode.val;
            }else if (dep == maxdep) {
                total += treeNode.val;
            }
            dfs(treeNode.left,dep+1);
            dfs(treeNode.right,dep+1);

        }

        public void dfs2(TreeNode treeNode){

            Stack<PIT> pitStack = new Stack<>();
            pitStack.push(new PIT(treeNode,0));
            while (!pitStack.isEmpty()) {

                PIT cal = pitStack.pop();
                if (cal.dept> maxdep){
                    maxdep = cal.dept;
                    total = cal.treeNode.val;
                } else if (cal.dept == maxdep){
                    total += cal.treeNode.val;
                }
                if (cal.treeNode.left!= null) {
                    pitStack.push(new PIT(cal.treeNode.left,cal.dept +1));
                }
                if (cal.treeNode.right!= null) {
                    pitStack.push(new PIT(cal.treeNode.right,cal.dept +1));
                }

            }




        }







        public void bfs(TreeNode treeNode){

            LinkedList<PIT> queue = new LinkedList<>();
             queue.push(new PIT(treeNode,0));
             while (!queue.isEmpty()){
                 PIT cal = queue.pop();
                 if (cal.dept> maxdep){
                     maxdep = cal.dept;
                     total = cal.treeNode.val;
                 } else if (cal.dept == maxdep){
                     total += cal.treeNode.val;
                 }

                if (cal.treeNode.left!= null) {
                    queue.push(new PIT(cal.treeNode.left,cal.dept +1));
                }
                 if (cal.treeNode.right!= null) {
                     queue.push(new PIT(cal.treeNode.right,cal.dept +1));
                 }

             }
        }


        public class PIT {
            TreeNode treeNode;
            int dept;
            PIT(TreeNode treeNode,int dept){
                this.treeNode = treeNode;
                this.dept = dept;

            }
        }


    }




}
