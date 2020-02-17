//给定一个可包含重复数字的序列，返回所有不重复的全排列。 
//
// 示例: 
//
// 输入: [1,1,2]
//输出:
//[
//  [1,1,2],
//  [1,2,1],
//  [2,1,1]
//] 
// Related Topics 回溯算法


package leetcode.editor.cn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Java：全排列 II
public class P47PermutationsIi {
    public static void main(String[] args) {
        Solution solution = new P47PermutationsIi().new Solution();
        // TO TEST
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<List<Integer>> permuteUnique(int[] nums) {

            List<List<Integer>> res = new ArrayList<>();

            if (nums.length == 0) {
                return res;
            }
            Arrays.sort(nums);
            List<Integer> path = new ArrayList<>();
            boolean use[] = new boolean[nums.length];
            bfs(res, path, 0, nums, use);
            return res;
        }

        void bfs(List<List<Integer>> res, List<Integer> path, int dep, int[] nums, boolean[] use) {

            if (dep == nums.length) {
                res.add(new ArrayList<>(path));
            }
            for (int i = 0; i < nums.length; i++) {
                if (!use[i]) {
                    if (i > 0 && nums[i - 1] == nums[i] && !use[i - 1]) {
                        continue;
                    }


                    path.add(nums[i]);
                    use[i] = true;
                    bfs(res, path, dep + 1, nums, use);
                    use[i] = false;
                    path.remove(dep);
                }


            }

        }
    }
//leetcode submit region end(Prohibit modification and deletion)

}