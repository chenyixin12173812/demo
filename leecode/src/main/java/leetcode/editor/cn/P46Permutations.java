//给定一个没有重复数字的序列，返回其所有可能的全排列。 
//
// 示例: 
//
// 输入: [1,2,3]
//输出:
//[
//  [1,2,3],
//  [1,3,2],
//  [2,1,3],
//  [2,3,1],
//  [3,1,2],
//  [3,2,1]
//] 
// Related Topics 回溯算法


package leetcode.editor.cn;

import java.util.ArrayList;
import java.util.List;

//Java：全排列

    //leetcode submit region begin(Prohibit modification and deletion)
//给定一个没有重复数字的序列，返回其所有可能的全排列。
//
// 示例:
//
// 输入: [1,2,3]
//输出:
//[
//  [1,2,3],
//  [1,3,2],
//  [2,1,3],
//  [2,3,1],
//  [3,1,2],
//  [3,2,1]
//]
// Related Topics 回溯算法



import java.util.ArrayList;
import java.util.List;

    //Java：全排列
    public class P46Permutations {
        public static void main(String[] args) {
            Solution solution = new leetcode.editor.cn.P46Permutations().new Solution();
            // TO TEST
        }

        //leetcode submit region begin(Prohibit modification and deletion)
        class Solution {
            public List<List<Integer>> permute(int[] nums) {

                List<List<Integer>> res = new ArrayList<>();

                if (nums.length == 0) {
                    return res;
                }
                List<Integer> path = new ArrayList<>();
                boolean use[] = new boolean[nums.length];
                bfs(res, path, 0, nums, use);
                return res;
            }

            void bfs(List<List<Integer>> res, List<Integer> path, int dep, int[] nums, boolean[] use) {

                if (dep == nums.length) {
                    res.add(new ArrayList<>(path));
                }
                for (int i=0;i<nums.length;i++){
                    if (!use[i]){
                        path.add(nums[i]);
                        use[i] = true;
                        bfs(res,path,dep+1,nums,use);
                        use[i] =false;
                        path.remove(dep);
                    }


                }

            }
        }


//leetcode submit region end(Prohibit modification and deletion)

    }
