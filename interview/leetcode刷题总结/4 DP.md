# 一 动态规划

# 1. 股票问题 （必考）

#### [121. 买卖股票的最佳时机](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock/)

给定一个数组，它的第 i 个元素是一支给定股票第 i 天的价格。

如果你最多只允许完成一笔交易（即买入和卖出一支股票一次），设计一个算法来计算你所能获取的最大利润。

注意：你不能在买入股票前卖出股票。

示例 1:

输入: [7,1,5,3,6,4]
输出: 5
解释: 在第 2 天（股票价格 = 1）的时候买入，在第 5 天（股票价格 = 6）的时候卖出，最大利润 = 6-1 = 5 。
     注意利润不能是 7-1 = 6, 因为卖出价格需要大于买入价格；同时，你不能在买入前卖出股票。
示例 2:

输入: [7,6,4,3,1]
输出: 0
解释: 在这种情况下, 没有交易完成, 所以最大利润为 0。

```java
方法1 双循环
class Solution {
        public int maxProfit(int[] prices) {

            int res = 0;
            for (int i = 0; i < prices.length; i++) {
                for (int j = 0; j < i; j++) {
                    if (prices[i] - prices[j] > res) {
                        res = prices[i] - prices[j];
                    }
                }
            }
            return res;
        }
    }
方法2 实时更新最小值 和最大收益（不是求出所有最小值，和最大值做差值） 贪心思想
class Solution {
        public int maxProfit(int[] prices) {
            int len = prices.length;
            if(len ==0) {
                return 0;
            }
            
            int max = 0;
            int min = prices[0];
            for (int i = 0; i < len; i++) {
               if(prices[i]-min>max){
                   max = prices[i]-min;
               }
               if(prices[i]<min){
                   min = prices[i];
               }
            }
            return max;
        }
    }

```

#### [122. 买卖股票的最佳时机 II](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-ii/)

给定一个数组，它的第 i 个元素是一支给定股票第 i 天的价格。

设计一个算法来计算你所能获取的最大利润。你可以尽可能地完成更多的交易（多次买卖一支股票）。

注意：你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）。

示例 1:

输入: [7,1,5,3,6,4]
输出: 7
解释: 在第 2 天（股票价格 = 1）的时候买入，在第 3 天（股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4 。
     随后，在第 4 天（股票价格 = 3）的时候买入，在第 5 天（股票价格 = 6）的时候卖出, 这笔交易所能获得利润 = 6-3 = 3 。
示例 2:

输入: [1,2,3,4,5]
输出: 4
解释: 在第 1 天（股票价格 = 1）的时候买入，在第 5 天 （股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4 。
     注意你不能在第 1 天和第 2 天接连购买股票，之后再将它们卖出。
     因为这样属于同时参与了多笔交易，你必须在再次购买前出售掉之前的股票。
示例 3:

输入: [7,6,4,3,1]
输出: 0
解释: 在这种情况下, 没有交易完成, 所以最大利润为 0。

```java
    class Solution {
        public int maxProfit(int[] prices) {


            return greedyMethod(prices);

        }

        int dpMethod(int[] prices) {

            int n = prices.length;

            if (n <= 1) return 0;

            int dp[][] = new int[n][2];


            dp[0][0] = 0;
            dp[0][1] = -prices[0];
            for (int i = 1; i < n; i++) {
                dp[i][0] = Math.max(dp[i - 1][0], dp[i - 1][1] + prices[i]);
                dp[i][1] = Math.max(dp[i - 1][1], dp[i - 1][0] - prices[i]);

            }

            return dp[n - 1][0];

        }

        int greedyMethod(int[] prices) {
            int n = prices.length;

            if (n <= 1) return 0;
            int res =0;
            for (int i =1 ;i<n;i++){

                if (prices[i]-prices[i-1]>0){

                    res += prices[i] -prices[i-1];
                }
            }
            return res;
        }
    }

```

#### [123. 买卖股票的最佳时机 III](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-iii/)

示例 1:

输入: [3,3,5,0,0,3,1,4]
输出: 6
解释: 在第 4 天（股票价格 = 0）的时候买入，在第 6 天（股票价格 = 3）的时候卖出，这笔交易所能获得利润 = 3-0 = 3 。
     随后，在第 7 天（股票价格 = 1）的时候买入，在第 8 天 （股票价格 = 4）的时候卖出，这笔交易所能获得利润 = 4-1 = 3 。
示例 2:

输入: [1,2,3,4,5]
输出: 4
解释: 在第 1 天（股票价格 = 1）的时候买入，在第 5 天 （股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4 。   
     注意你不能在第 1 天和第 2 天接连购买股票，之后再将它们卖出。   
     因为这样属于同时参与了多笔交易，你必须在再次购买前出售掉之前的股票。
示例 3:

输入: [7,6,4,3,1] 
输出: 0 
解释: 在这个情况下, 没有交易完成, 所以最大利润为 0。

```java

```

#### [188. 买卖股票的最佳时机 IV](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-iv/)

给定一个数组，它的第 i 个元素是一支给定的股票在第 i 天的价格。

设计一个算法来计算你所能获取的最大利润。你最多可以完成 k 笔交易。

注意: 你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）。

示例 1:

输入: [2,4,1], k = 2
输出: 2
解释: 在第 1 天 (股票价格 = 2) 的时候买入，在第 2 天 (股票价格 = 4) 的时候卖出，这笔交易所能获得利润 = 4-2 = 2 。
示例 2:

输入: [3,2,6,5,0,3], k = 2
输出: 7
解释: 在第 2 天 (股票价格 = 2) 的时候买入，在第 3 天 (股票价格 = 6) 的时候卖出, 这笔交易所能获得利润 = 6-2 = 4 。
     随后，在第 5 天 (股票价格 = 0) 的时候买入，在第 6 天 (股票价格 = 3) 的时候卖出, 这笔交易所能获得利润 = 3-0 = 3 

```java

```





#### [309. 最佳买卖股票时机含冷冻期](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-with-cooldown/)

给定一个整数数组，其中第 i 个元素代表了第 i 天的股票价格 。

设计一个算法计算出最大利润。在满足以下约束条件下，你可以尽可能地完成更多的交易（多次买卖一支股票）:

你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）。
卖出股票后，你无法在第二天买入股票 (即冷冻期为 1 天)。
示例:

输入: [1,2,3,0,2]
输出: 3 
解释: 对应的交易状态为: [买入, 卖出, 冷冻期, 买入, 卖出]

```java
class Solution {
    public int maxProfit(int[] prices) {

     int len = prices.length;
     if(len<2) {
         return 0;
     }
     
     int dp[][] = new int[len] [2];

     dp[0][0] = -prices[0];
     dp[0][1] = 0;
     dp[1][0] = Math.max(-prices[0],-prices[1]);
     dp[1][1] = Math.max(0,-prices[0]+prices[1]);

     for(int i =2; i<len; i++) {
       //买入冻结一天  
       dp[i][0] = Math.max(dp[i-1][0],dp[i-2][1]-prices[i]);
       //卖无需冻结    
       dp[i][1] = Math.max(dp[i-1][1],dp[i-1][0]+prices[i]);
     }
     return dp[len-1][1]; 

    }
}
```

#### [714. 买卖股票的最佳时机含手续费](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-with-transaction-fee/)

给定一个整数数组 prices，其中第 i 个元素代表了第 i 天的股票价格 ；非负整数 fee 代表了交易股票的手续费用。

你可以无限次地完成交易，但是你每笔交易都需要付手续费。如果你已经购买了一个股票，在卖出它之前你就不能再继续购买股票了。

返回获得利润的最大值。

注意：这里的一笔交易指买入持有并卖出股票的整个过程，每笔交易你只需要为支付一次手续费。

示例 1:

输入: prices = [1, 3, 2, 8, 4, 9], fee = 2
输出: 8
解释: 能够达到的最大利润:  
在此处买入 prices[0] = 1
在此处卖出 prices[3] = 8
在此处买入 prices[4] = 4
在此处卖出 prices[5] = 9
总利润: ((8 - 1) - 2) + ((9 - 4) - 2) = 8.

```java
class Solution {
    public int maxProfit(int[] prices, int fee) {
        
     int len = prices.length;

     if(len<2){
         return 0;
     }
     int dp[][] = new int [len][2];
     dp[0][0] = -prices[0];
     dp[0][1] = 0;

     for(int i =1;i<len;i++) {
        dp[i][0] = Math.max(dp[i-1][0],dp[i-1][1]-prices[i]);
         //卖时 减去手续费
        dp[i][1] = Math.max(dp[i-1][1],dp[i-1][0]+prices[i]-fee);
     }

     return dp[len-1][1];
    }
}
```



# 2 石子游戏

不同顺序算一个

个排列问题怎么解决。比如nums=[1,2,3] target = 8，要计算能构成8的所有排列有几个，那就可以计算构成5的排列有几个，但是到这我想的是，这个3应该有几种方法插入到构成5的排列里呢？就卡在这了。然后看了别人的代码，一时又难以理解，想了半天想明白了。

在计算构成8的排列有几个时即dp[8]，我们只关注分别以1,2,3为“屁股”的所有排列的个数，这仨排列个数之和就是构成8的所有排列了。为什么这么说呢？因为所有排列一定一定是以1,2,3其中一个为结尾的（废话），那么对于8而言，以3为结尾的排列个数就是dp[5]，以2为结尾的排列个数就是dp[6]，同理以1为结尾的排列个数就是dp[7]，把它仨加起来，就是dp[8]了。

这道题要注意一下初始化和overflow问题，初始化是应该dp[0]=1，详见代码注释。

零钱兑换问题中dp[i]为dp[i-nums[j]]的最小值，而这道题中dp[i]为dp[i-nums[j]]之和，这是它们的区别。

# 3  完全背包问题（必考）

#### [518. 零钱兑换 II](https://leetcode-cn.com/problems/coin-change-2/)

```java
class Solution {
    public int change(int amount, int[] coins) {

    int dp[] = new int[amount+1]; 

    dp[0] =1;
    for(int coin :coins) {
       for(int i = 1;i<=amount;i++) {
         if(i>=coin) {
             dp[i] = dp[i] +dp[i-coin];
         } 

       }
    }
    return dp[amount];
    }
}
```

#### [377. 组合总和 Ⅳ](https://leetcode-cn.com/problems/combination-sum-iv/)

给定一个由正整数组成且不存在重复数字的数组，找出和为给定目标正整数的组合的个数。

**示例:**

```java
nums = [1, 2, 3]
target = 4

所有可能的组合为：
(1, 1, 1, 1)
(1, 1, 2)
(1, 2, 1)
(1, 3)
(2, 1, 1)
(2, 2)
(3, 1)

请注意，顺序不同的序列被视作不同的组合。

因此输出为 7。
class Solution {
    public int combinationSum4(int[] nums, int target) {
         
         int dp[] = new int [target+1];
         dp[0] =1;

         for(int i =1;i<=target;i++){

             for(int num : nums){

             if(i>=num)
              dp[i] = dp[i] + dp[i-num];
             }
         }
         return dp[target];
    }
     private void dfs(List<List<Integer>> res,List<Integer> path,int[] nums, int deep,int sum){

         if (sum == 0){
             res.add(new ArrayList(path));
             return;
         }
         if(sum<0 ||deep>nums.length){
             return;
         }
         for(int i =0;i<nums.length;i++){
               path.add(nums[i]);
               dfs(res,path,nums,deep+1,sum-nums[i]);
               path.remove(path.size()-1);

         }
     }
}


```



## 322. 零钱兑换

给定不同面额的硬币 coins 和一个总金额 amount。编写一个函数来计算可以凑成总金额所需的最少的硬币个数。如果没有任何一种硬币组合能组成总金额，返回 -1。

示例 1:

输入: coins = [1, 2, 5], amount = 11
输出: 3 
解释: 11 = 5 + 5 + 1
示例 2:

输入: coins = [2], amount = 3
输出: -1

```java
class Solution {

    public int coinChange(int[] coins, int amount) {
     
     int len = coins.length;
     if(len==0||amount<=0) {
         return 0;
     }

     int dp[] = new int [amount+1];
     Arrays.fill(dp,amount+1);
     dp[0] = 0;
     for(int i =1;i<=amount;i++) {
         for(int coin :coins) {
            if(i>=coin) {
              dp[i] = Math.min(dp[i],dp[i-coin]+1);
            }
         }
     }
     return dp[amount]==amount+1?-1:dp[amount];
    }
}
```



# 4  0-1背包问题 （必考）

## [416. 分割等和子集](https://leetcode-cn.com/problems/partition-equal-subset-sum/)

难度中等378收藏分享切换为英文关注反馈

给定一个**只包含正整数**的**非空**数组。是否可以将这个数组分割成两个子集，使得两个子集的元素和相等。

**注意:**

1. 每个数组中的元素不会超过 100
2. 数组的大小不会超过 200

**示例 1:**

```
输入: [1, 5, 11, 5]

输出: true

解释: 数组可以分割成 [1, 5, 5] 和 [11].
```

 

**示例 2:**

```
输入: [1, 2, 3, 5]

输出: false

解释: 数组不能分割成两个元素和相等的子集.
```

```java
class Solution {
    public boolean canPartition(int[] nums) {

    int len = nums.length;
    if(len ==0) {
        return false;
    }
    int sum =0;
    for(int i =0;i<len;i++) {
      sum += nums[i];
    }
    if(sum%2==1){
        return false;
    }    

    boolean dp[][] = new  boolean [len+1][sum+1];

    dp[0][0] =true;
 
    for(int i =0;i<=len;i++){
        dp[i][0] = true;
    }
    sum = sum/2;
    for(int i=1;i<=len;i++) {
      for(int j =1;j<=sum;j++) {
      if(j<nums[i-1]) {
         dp[i][j] = dp[i-1][j];
      } else{
          dp[i][j] = dp[i-1][j] || dp[i-1][j-nums[i-1]];
      }
      }
    } 
    return dp[len][sum]; 
    }
}
```

## 474. 一和零

现在，假设你分别支配着 **m** 个 `0` 和 **n** 个 `1`。另外，还有一个仅包含 `0` 和 `1` 字符串的数组。

你的任务是使用给定的 **m** 个 `0` 和 **n** 个 `1` ，找到能拼出存在于数组中的字符串的最大数量。每个 `0` 和 `1` 至多被使用**一次**。

**注意:**

1. 给定 `0` 和 `1` 的数量都不会超过 `100`。
2. 给定字符串数组的长度不会超过 `600`。

**示例 1:**

```
输入: Array = {"10", "0001", "111001", "1", "0"}, m = 5, n = 3
输出: 4

解释: 总共 4 个字符串可以通过 5 个 0 和 3 个 1 拼出，即 "10","0001","1","0" 。
```

**示例 2:**

```
输入: Array = {"10", "0", "1"}, m = 1, n = 1
输出: 2

解释: 你可以拼出 "10"，但之后就没有剩余数字了。更好的选择是拼出 "0" 和 "1" 。
```

```java
class Solution {
    public int findMaxForm(String[] strs, int m, int n) {

    int len = strs.length;

    if(len==0) {
        return 0;
    }

    int dp [][][] = new int [len+1][m+1][n+1];

    for(int i =1 ;i<=len;i++) {
        for(int j =0;j<=m;j++) {
            for(int k =0;k<=n;k++) {
             int ones = cuntOne(strs[i-1]);
             int zero=  strs[i-1].length() - ones;
             if(ones<=k && zero<=j) {
              dp[i][j][k] = Math.max(dp[i-1][j][k],dp[i-1][j-zero][k-ones]+1);
             } 
             else {
              dp[i][j][k] = dp[i-1][j][k];   
             }
            }
        }

    }

     return dp[len][m][n];
    }

    private int cuntOne(String str) {
        int cunt =0;
        char chars[] = str.toCharArray();
        for(char c:chars){
            if(c=='1') {
                cunt++;
            }
        }
        return cunt;  
    }


}
```

## 494. 目标和

难度中等377收藏分享切换为英文关注反馈

给定一个非负整数数组，a1, a2, ..., an, 和一个目标数，S。现在你有两个符号 `+` 和 `-`。对于数组中的任意一个整数，你都可以从 `+` 或 `-`中选择一个符号添加在前面。

返回可以使最终数组和为目标数 S 的所有添加符号的方法数。

**示例：**

```java
输入：nums: [1, 1, 1, 1, 1], S: 3
输出：5
解释：

-1+1+1+1+1 = 3
+1-1+1+1+1 = 3
+1+1-1+1+1 = 3
+1+1+1-1+1 = 3
+1+1+1+1-1 = 3

一共有5种方法让最终目标和为3。
 class Solution {

    int cunt =0;
     // 方法3动态规划
    // https://leetcode-cn.com/problems/target-sum/solution/dong-tai-gui-hua-he-hui-su-suan-fa-dao-di-shui-shi/
    public int findTargetSumWays(int[] nums, int S) {
      return dpMethod(nums,S);
    }

    public int dpMethod(int[] nums, int target){
       
    int sum = 0;
    for (int n : nums) sum += n;
    // 这两种情况，不可能存在合法的子集划分
    if (sum < target || (sum + target) % 2 == 1) {
        return 0;
    }
    return subsets(nums, (sum + target) / 2);
    }

    private int subsets(int[] nums, int sum){
 int n = nums.length;
    int[][] dp = new int[n + 1][sum + 1];
    // base case
    for (int i = 0; i <= n; i++) {
        dp[i][0] = 1;
    }
    
    for (int i = 1; i <= n; i++) {
        for (int j = 0; j <= sum; j++) {
            if (j >= nums[i-1]) {
                // 两种选择的结果之和
                dp[i][j] = dp[i-1][j] + dp[i-1][j-nums[i-1]];
            } else {
                // 背包的空间不足，只能选择不装物品 i
                dp[i][j] = dp[i-1][j];
            }
        }
    }
    return dp[n][sum];

    }
    public void backTrack(int nums[],int i,int sum){

      if(i==nums.length){
          if (sum==0){
              cunt ++;
          }
          return;
      }else{
          backTrack(nums,i+1,sum-nums[i]);
          backTrack(nums,i+1,sum+nums[i]);
      }
    }
}   
    
    
    
```

# 5  [354. 俄罗斯套娃信封问题](https://leetcode-cn.com/problems/russian-doll-envelopes/)(必考)

给定一些标记了宽度和高度的信封，宽度和高度以整数对形式 (w, h) 出现。当另一个信封的宽度和高度都比这个信封大的时候，这个信封就可以放进另一个信封里，如同俄罗斯套娃一样。

请计算最多能有多少个信封能组成一组“俄罗斯套娃”信封（即可以把一个信封放到另一个信封里面）。

说明:
不允许旋转信封。

示例:

输入: envelopes = [[5,4],[6,4],[6,7],[2,3]]
输出: 3 
解释: 最多信封的个数为 3, 组合为: [2,3] => [5,4] => [6,7]。

**先排序，最长上升子序列问题 这种方式最佳吗？**

看看[面试题 08.13. 堆箱子](https://leetcode-cn.com/problems/pile-box-lcci/) 的解法

```java
//LIS方法
class Solution {
    public int maxEnvelopes(int[][] envelopes) {

            Arrays.sort(envelopes,(int a[], int b[])->{
                return a[0] == b[0] ? b[1]-a[1] : a[0] -b[0];
            });

      int n = envelopes.length;
      int val[] = new int [n];

      for(int i =0;i<n;i++){
           val[i] = envelopes[i][1]; 
      }
       return lengthOflsc(val);
    }

    private int lengthOflsc(int [] nums){

        if(nums.length == 0) return 0;
        int[] dp = new int[nums.length];
        int res = 1;
        Arrays.fill(dp, 1);
        for(int i = 1; i < nums.length; i++) {
            for(int j = 0; j < i; j++) {
                if(nums[j] < nums[i]) dp[i] = Math.max(dp[i], dp[j] + 1);
            }
            res = Math.max(res, dp[i]);
        }
        return res;
    }
}
//最后一列排序法
class Solution {
    public int maxEnvelopes(int[][] envelopes) {

     int len = envelopes.length;
     if(len==0){
         return 0;
     }

     Arrays.sort(envelopes,(int a[],int b[])->a[1]-b[1]);
     return cuntLis(envelopes); 

    }
    private int cuntLis(int [][] envelopes) {

      int len = envelopes.length;
      int dp[] = new int [len];
      int max =1;
    
      for(int i =0;i<len;i++) {
          dp[i] =1;
          for(int j=0;j<i;j++){
           if(envelopes[i][0]>envelopes[j][0] && envelopes[i][1]>envelopes[j][1]){
             dp[i] = Math.max(dp[i],dp[j]+1);
           }
          }
          max= Math.max(dp[i],max);
      }
      return max;
    }
}


```

#### [面试题 08.13. 堆箱子](https://leetcode-cn.com/problems/pile-box-lcci/) 

堆箱子。给你一堆n个箱子，箱子宽 wi、深 di、高 hi。箱子不能翻转，将箱子堆起来时，下面箱子的宽度、高度和深度必须大于上面的箱子。实现一种方法，搭出最高的一堆箱子。箱堆的高度为每个箱子高度的总和。

输入使用数组[wi, di, hi]表示每个箱子。

示例1:

 输入：box = [[1, 1, 1], [2, 2, 2], [3, 3, 3]]
 输出：6
示例2:

 输入：box = [[1, 1, 1], [2, 3, 4], [2, 6, 7], [3, 4, 5]]
 输出：10

```java
class Solution {
        public int pileBox(int[][] box) {
            int m = box.length;
            if(m==0) {
                return 0;
            }
            Arrays.sort(box, (a, b) ->  a[2] - b[2]);

            int dp [] = new int [m+1];
            int max = 0;
            for(int i =0;i<m;i++) {
                dp[i] =box[i][2];
                for(int j = i-1;j>=0;j--) {
                    if(box[i][0] > box[j][0] && box[i][1] > box[j][1] && box[i][2] > box[j][2]) {
                        dp[i] = Math.max(dp[i],dp[j] +box[i][2]);
                    }
                }
                max = Math.max(max,dp[i]);
            }
            System.out.println("max"+max);

            return max;
        }
    }
```



# 6 1312 [让字符串成为回文串的最少插入次数](https://leetcode-cn.com/problems/minimum-insertion-steps-to-make-a-string-palindrome/)

给你一个字符串 s ，每一次操作你都可以在字符串的任意位置插入任意字符。

请你返回让 s 成为回文串的 最少操作次数 。

示例 1：

输入：s = "zzazz"
输出：0
解释：字符串 "zzazz" 已经是回文串了，所以不需要做任何插入操作。
示例 2：

输入：s = "mbadm"
输出：2
解释：字符串可变为 "mbdadbm" 或者 "mdbabdm" 。
示例 3：

输入：s = "leetcode"
输出：5
解释：插入 5 个字符后字符串变为 "leetcodocteel" 。
示例 4：

输入：s = "g"
输出：0
示例 5：

输入：s = "no"
输出：1

**与leetcode516相识**,用总长度减去回文序列

```java
class Solution {
    public int minInsertions(String s) {
      int n = s.length();
      int dp[][] = new int [n][n];
      for(int i=n-1;i>=0;i--){
          dp[i][i] =1;
         for(int j =i+1;j<n;j++){
           if(s.charAt(i) == s.charAt(j)){
           dp[i][j] = dp[i+1][j-1] +2;

           } else{
               dp[i][j] = Math.max(dp[i+1][j],dp[i][j-1]);
           }  
         }
      }
      return n -dp[0][n-1];
    }
}
```





# 7 516[最长回文子序列](https://leetcode-cn.com/problems/longest-palindromic-subsequence/) [5. 最长回文子串](https://leetcode-cn.com/problems/longest-palindromic-substring/)（必考）

给定一个字符串 s ，找到其中最长的回文子序列，并返回该序列的长度。可以假设 s 的最大长度为 1000 。

 

示例 1:
输入:

"bbbab"
输出:

4
一个可能的最长回文子序列为 "bbbb"。

示例 2:
输入:

"cbbd"
输出:

2
一个可能的最长回文子序列为 "bb"。

```java
class Solution {
    public int longestPalindromeSubseq(String s) {

     int len = s.length();
     if(len==0) {
         return 0;
     }
     //i-j 最长的回文子序列
     int dp[][] = new int [len+1][len+1];
    //1 j在外层 i自底向上
    for(int j= 0;j<len;j++) {
      dp[j][j] =1;   
      for (int i = j - 1; i >=0; i--) {
       if(s.charAt(i)==s.charAt(j)) {
         dp[i][j] = dp[i+1][j-1]+2;
       } else{
          dp[i][j] =Math.max(dp[i][j-1],dp[i+1][j]);
       }
      }
     }  

    //2 i在外层
    //  for(int i= len-1;i>=0;i--) {
    //   dp[i][i] =1;   
    //   for (int j = i + 1; j < len; j++) {
    //    if(s.charAt(i)==s.charAt(j)) {
    //      dp[i][j] = dp[i+1][j-1]+2;
    //    } else{
    //       dp[i][j] =Math.max(dp[i][j-1],dp[i+1][j]);
    //    }
    //   }
    //  }
      
    return dp[0][len-1];  
    }
}
```

#### [5. 最长回文子串](https://leetcode-cn.com/problems/longest-palindromic-substring/)

给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。

示例 1：

输入: "babad"
输出: "bab"
注意: "aba" 也是一个有效答案。
示例 2：

输入: "cbbd"
输出: "bb"

```java
class Solution {
    public String longestPalindrome(String s) {
        
    if(s==null||s.length()==0) {
        return "";
    }
    int len = s.length();
    boolean dp[][] = new boolean[len+1][len+1];
    int start =0;
    int end =0;
    //1    
        for (int j =1 ;j<len;j++){
            for (int i = j;i>=0;i--){
                if (s.charAt(i)==s.charAt(j) && (j-i<2 ||dp[i+1][j-1])){
                    dp[i][j] =true;

                    if (j-i>end-start){
                        start =i;
                        end =j;
                    }


                }
            }
        }    
    //2    
    for(int i= len-1;i>=0;i--){
        dp[i][i] = true;
        for(int j=i+1;j<len;j++){
            if(s.charAt(i)==s.charAt(j) && (j-i<2 ||dp[i+1][j-1])) {
              dp[i][j] =true;
              if(j-i>end-start) {
                  start =i;
                  end =j;
              }
            }
        }
    }

    return s.substring(start,end+1);
    }
}
```



# 8  1143. 最长公共子序列(必考)

```java
class Solution {
    public int longestCommonSubsequence(String text1, String text2) {

     int m = text1.length();
     int n = text2.length();
     int dp[][] = new int [m+1][n+1];


     for(int i =0;i<m;i++){
      for(int j =0;j<n;j++){

        if(text1.charAt(i) == text2.charAt(j)){
           dp[i+1][j+1] =  dp[i][j]+1;
        } else{
            dp[i+1][j+1] = Math.max(dp[i][j+1],dp[i+1][j]); 
        }
      }             
     }
     return dp[m][n];
    }
}
```



# 9  [718. 最长重复子数组](https://leetcode-cn.com/problems/maximum-length-of-repeated-subarray/)  最大子串问题(必考)

```java
class Solution {
    public int findLength(int[] A, int[] B) {
       int len1 = A.length;
       int len2 = B.length;
       if(len1==0 || len2 == 0){
          return 0;
       } 
       int max=0;
       //以i结尾的子串，由j结尾的子串 最大公共长度
       int dp[][] = new int [len1+1][len2+1];
        for(int i =0;i<len1;i++) {
           for(int j =0;j<len2;j++) {
            if(A[i]==B[j]) {
              dp[i+1][j+1] = dp[i][j]+1;
            }
            //与子序列的区别
            else{
               dp[i+1][j+1] = 0; 
            }
            max = Math.max(max,dp[i+1][j+1]);
           }
        }
        return max;
    }
}
```





# 11 [300. 最长上升子序列](https://leetcode-cn.com/problems/longest-increasing-subsequence/)（必考）

难度中等847收藏分享切换为英文关注反馈

给定一个无序的整数数组，找到其中最长上升子序列的长度。

**示例:**

```
输入: [10,9,2,5,3,7,101,18]
输出: 4 
解释: 最长的上升子序列是 [2,3,7,101]，它的长度是 4。
```

 dp[i] 代表 已i结尾的子序列

```java
class Solution {
    public int lengthOfLIS(int[] nums) {

        int n = nums.length;
        if(n ==0){
            return 0;
        }
        int dp[] = new int [n+1];
        int max =0;
        for(int i= 0; i<n;i++){
         dp[i] =1;
        for(int j = i-1;j>=0;j--){
           if(nums[j]<nums[i]){
               dp[i] = Math.max(dp[i],dp[j]+1);
           }
         }
         max = Math.max(max,dp[i]);
        }

       return max;

    }
}
```





# 12  [312. 戳气球](https://leetcode-cn.com/problems/burst-balloons/)

有 n 个气球，编号为0 到 n-1，每个气球上都标有一个数字，这些数字存在数组 nums 中。

现在要求你戳破所有的气球。如果你戳破气球 i ，就可以获得 nums[left] * nums[i] * nums[right] 个硬币。 这里的 left 和 right 代表和 i 相邻的两个气球的序号。注意当你戳破了气球 i 后，气球 left 和气球 right 就变成了相邻的气球。

求所能获得硬币的最大数量。

说明:

你可以假设 nums[-1] = nums[n] = 1，但注意它们不是真实存在的所以并不能被戳破。
0 ≤ n ≤ 500, 0 ≤ nums[i] ≤ 100
示例:

输入: [3,1,5,8]
输出: 167 
解释: nums = [3,1,5,8] --> [3,5,8] -->   [3,8]   -->  [8]  --> []
     coins =  3*1*5      +  3*5*8    +  1*3*8      + 1*8*1   = 167

```java
class Solution {
    public int maxCoins(int[] nums) {

      int n = nums.length;
      int rev[] = new int [n+2];
      int dp[][] = new int [n+2][n+2];
      
      rev[0] =1;
      rev[n+1] =1;

      for(int i =1;i<n+1;i++){
        rev[i] = nums[i-1];
      }

       for(int i = n-1;i>=0;i--){
        for(int j = i+2;j<n+2;j++){
           for(int k =i+1;k<j;k++){
            // 为什么不是 rev[k-1]] * rev[j] *rev[k+1] 模拟添加气球的
             int sum = rev[i] * rev[j] *rev[k];
             // 为什么包含k? i-k或者k-j
            sum = sum + dp[i][k] +dp[k][j];
             dp[i][j] = Math.max(dp[i][j],sum);

           }
        }}
         return dp[0][n+1];
    }
}
```

# 13 [647. 回文子串](https://leetcode-cn.com/problems/palindromic-substrings/)（必考）

给定一个字符串，你的任务是计算这个字符串中有多少个回文子串。

具有不同开始位置或结束位置的子串，即使是由相同的字符组成，也会被计为是不同的子串。

示例 1:

输入: "abc"
输出: 3
解释: 三个回文子串: "a", "b", "c".
示例 2:

输入: "aaa"
输出: 6
说明: 6个回文子串: "a", "a", "a", "aa", "aa", "aaa".

```java
class Solution {
        public int countSubstrings(String s) {

            int n = s.length();
            boolean dp [][] = new boolean[n+1][n+1];
            dp[0][0] = true;

            for(int j=1;j<n;j++){
                for(int i =j;i>=0;i--){
                    if(s.charAt(i) == s.charAt(j) &&(j-i<2||dp[i+1][j-1])){
                        dp[i][j] = true;
                    }
                }
            }

            int result =0;

            for(int i =0;i<n;i++){
                for(int j =i;j<n;j++){
                    if(dp[i][j]){
                        result++;

                    }
                }

            }
            return result;

        }
    }
```



# 14  72[编辑距离](https://leetcode-cn.com/problems/edit-distance/)（思想NB）

给你两个单词 word1 和 word2，请你计算出将 word1 转换成 word2 所使用的最少操作数 。

你可以对一个单词进行如下三种操作：

插入一个字符
删除一个字符
替换一个字符


示例 1：

输入：word1 = "horse", word2 = "ros"
输出：3
解释：
horse -> rorse (将 'h' 替换为 'r')
rorse -> rose (删除 'r')
rose -> ros (删除 'e')

```java
class Solution {
    public int minDistance(String word1, String word2) {

      int m = word1.length();
      int n= word2.length();
      int dp[][] = new int [m+1][n+1];

        //有初值问题
       // 初始化：当 word 2 长度为 0 时，将 word1 的全部删除
        for (int i = 1; i <= m; i++) {
            dp[i][0] = i;
        }
        // 当 word1 长度为 0 时，就插入所有 word2 的字符
        for (int j = 1; j <= n; j++) {
            dp[0][j] = j;
        }
      
       for(int i =0;i<m;i++){
         for(int j =0;j<n;j++){
           if(word1.charAt(i)==word2.charAt(j)){
               //无需操作
               dp[i+1][j+1] = dp[i][j];
           } else{
                int insert = dp[i + 1][j] + 1;
                // 2、替换一个字符
                int replace = dp[i][j] + 1;
                // 3、删除一个字符
                int delete = dp[i][j + 1] + 1;
                dp[i + 1][j + 1] = Math.min(Math.min(insert, replace), delete);
           }
         }

       }
       return dp[m][n];

    }
}
```



#### [面试题 01.05. 一次编辑](https://leetcode-cn.com/problems/one-away-lcci/)（能用上面的dp，不是最优）

字符串有三种编辑操作:插入一个字符、删除一个字符或者替换一个字符。 给定两个字符串，编写一个函数判定它们是否只需要一次(或者零次)编辑。

 

**示例 1:**

```
输入: 
first = "pale"
second = "ple"
输出: True
```

 

**示例 2:**

```
输入: 
first = "pales"
second = "pal"
输出: False
```

```java
class Solution {
    public boolean oneEditAway(String first, String second) {
      if(first.equals(second)) {
          return true;
      }
      int len1= first.length();
      int len2 = second.length();
      if(Math.abs(len1-len2)>1) {
          return false;
      } 

      int dp[][] = new int [len1+1][len2+1];
      for(int i=0;i<len1;i++) {
          dp[i][0] =i;
      } 
      for(int j=0;j<len2;j++) {
          dp[0][j] =j;
      }
      for(int i =0;i<len1;i++) {
         for(int j =0;j<len2;j++) {
            if(first.charAt(i)==second.charAt(j)) {
                dp[i+1][j+1] = dp[i][j];
            } else {
                dp[i+1][j+1] = Math.min(dp[i+1][j]+1,Math.min(dp[i][j+1]+1,dp[i][j]+1));
            }
         } 

      }
       return dp[len1][len2]<=1;
    }
}


//双指针方法 真牛逼

class Solution {
    public boolean oneEditAway(String first, String second) {

        
        if (first.equals(second)) {
            return true;
        }
        if (Math.abs(first.length() - second.length()) > 1) {
            return false;
        }
        int i = 0;
        int len1 = first.length();
        int len2 = second.length();
        int j = first.length() - 1;
        int k = second.length() - 1;
        while (i < len1 && i < len2 && first.charAt(i) == second.charAt(i)) {
            i++;
        }
        while (j >= 0 && k >= 0 && first.charAt(j) == second.charAt(k)) {
            j--;
            k--;
        }
        return j - i < 1 && k - i < 1;
    
    
    }
}

```







# 15 

# 16 





# 17 打家劫舍（必考）

##  [198. 打家劫舍](https://leetcode-cn.com/problems/house-robber/)

你是一个专业的小偷，计划偷窃沿街的房屋。每间房内都藏有一定的现金，影响你偷窃的唯一制约因素就是相邻的房屋装有相互连通的防盗系统，如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警。

给定一个代表每个房屋存放金额的非负整数数组，计算你 不触动警报装置的情况下 ，一夜之内能够偷窃到的最高金额。

示例 1：

输入：[1,2,3,1]
输出：4
解释：偷窃 1 号房屋 (金额 = 1) ，然后偷窃 3 号房屋 (金额 = 3)。
     偷窃到的最高金额 = 1 + 3 = 4 。
示例 2：

输入：[2,7,9,3,1]
输出：12
解释：偷窃 1 号房屋 (金额 = 2), 偷窃 3 号房屋 (金额 = 9)，接着偷窃 5 号房屋 (金额 = 1)。
     偷窃到的最高金额 = 2 + 9 + 1 = 12 。

```java
class Solution {
    public int rob(int[] nums) {

     int n = nums.length;
     if(n==0) return 0;
    if(n==1) return nums[0] ;
     int dp[] = new int[n+1];
    dp[0] = nums[0];
    dp[1] = Math.max(nums[0],nums[1]);

    for(int i =2;i<n;i++){
        dp[i] = Math.max(dp[i-2]+nums[i],dp[i-1]);
    }

    return dp[n-1];  


    }
}
```

## 213. 打家劫舍 II

你是一个专业的小偷，计划偷窃沿街的房屋，每间房内都藏有一定的现金。这个地方所有的房屋都围成一圈，这意味着第一个房屋和最后一个房屋是紧挨着的。同时，相邻的房屋装有相互连通的防盗系统，如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警。

给定一个代表每个房屋存放金额的非负整数数组，计算你在不触动警报装置的情况下，能够偷窃到的最高金额。

示例 1:

输入: [2,3,2]
输出: 3
解释: 你不能先偷窃 1 号房屋（金额 = 2），然后偷窃 3 号房屋（金额 = 2）, 因为他们是相邻的。
示例 2:

输入: [1,2,3,1]
输出: 4
解释: 你可以先偷窃 1 号房屋（金额 = 1），然后偷窃 3 号房屋（金额 = 3）。
     偷窃到的最高金额 = 1 + 3 = 4 。

```java
class Solution {
    public int rob(int[] nums) {
     int len = nums.length;   
     if(len<1) {
         return 0;
     }
     if(len==1) {
         return nums[0];
     }
      return Math.max(rob(nums,0,len-2) , rob(nums,1,len-1));
    }
    public int rob(int[] nums,int start,int end) {
      int len = end-start+1;
      if(len==0){
          return 0;
      }  
      if(len ==1){
          return nums[start];
      }
      if(len ==2){
          return Math.max(nums[start],nums[start+1]);
      }
      int dp[] = new int [len];
      dp[0] = nums[start];
      dp[1] = Math.max(nums[start],nums[start+1]);

      for(int i =2;i<len;i++) {
        dp[i] = Math.max(dp[i-2]+nums[i+start],dp[i-1]);
      }  

      return dp[len-1];

    } 

}
```



## 337 打家劫舍 III

 在上次打劫完一条街道之后和一圈房屋后，小偷又发现了一个新的可行窃的地区。这个地区只有一个入口，我们称之为“根”。 除了“根”之外，每栋房子有且只有一个“父“房子与之相连。一番侦察之后，聪明的小偷意识到“这个地方的所有房屋的排列类似于一棵二叉树”。 如果两个直接相连的房子在同一天晚上被打劫，房屋将自动报警。

计算在不触动警报的情况下，小偷一晚能够盗取的最高金额。

示例 1:

输入: [3,2,3,null,3,null,1]

     3
    / \
   2   3
    \   \ 
     3   1

输出: 7 
解释: 小偷一晚能够盗取的最高金额 = 3 + 3 + 1 = 7.

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public int rob(TreeNode root) {

      if(root==null){
          return 0;
      }
      int result =root.val;

      if(root.left != null){
          result  = result + rob(root.left.left) + rob(root.left.right); 
      }
      if(root.right != null){
          result  = result + rob(root.right.left) + rob(root.right.right); 
      }

      return Math.max(result,rob(root.left)+rob(root.right));

    }
```



# 18 [139. 单词拆分](https://leetcode-cn.com/problems/word-break/)(必考)

给定一个非空字符串 s 和一个包含非空单词列表的字典 wordDict，判定 s 是否可以被空格拆分为一个或多个在字典中出现的单词。

说明：

拆分时可以重复使用字典中的单词。
你可以假设字典中没有重复的单词。
示例 1：

输入: s = "leetcode", wordDict = ["leet", "code"]
输出: true
解释: 返回 true 因为 "leetcode" 可以被拆分成 "leet code"。
示例 2：

输入: s = "applepenapple", wordDict = ["apple", "pen"]
输出: true
解释: 返回 true 因为 "applepenapple" 可以被拆分成 "apple pen apple"。
     注意你可以重复使用字典中的单词。
示例 3：

输入: s = "catsandog", wordDict = ["cats", "dog", "sand", "and", "cat"]
输出: false

```java
class Solution {
    public boolean wordBreak(String s, List<String> wordDict) {
     int len = s.length();
     boolean dp [] = new boolean[len+1];
     dp[0] =true;


        for(int j = 1; j <= s.length(); j++){
            for(int i = j-1; i >= 0; i--){
                dp[j] = dp[i] && check(wordDict,s.substring(i, j));
                if(dp[j])   break;
            }
        }

     return dp[len];
    }

    boolean check(List<String> wordDict,String s){

        return wordDict.contains(s);
    }

}
```

# 19 [140. 单词拆分 II](https://leetcode-cn.com/problems/word-break-ii/)（dp dfs trie）

# 20 [376. 摆动序列](https://leetcode-cn.com/problems/wiggle-subsequence/)

如果连续数字之间的差严格地在正数和负数之间交替，则数字序列称为摆动序列。第一个差（如果存在的话）可能是正数或负数。少于两个元素的序列也是摆动序列。

例如， [1,7,4,9,2,5] 是一个摆动序列，因为差值 (6,-3,5,-7,3) 是正负交替出现的。相反, [1,4,7,2,5] 和 [1,7,4,5,5] 不是摆动序列，第一个序列是因为它的前两个差值都是正数，第二个序列是因为它的最后一个差值为零。

给定一个整数序列，返回作为摆动序列的最长子序列的长度。 通过从原始序列中删除一些（也可以不删除）元素来获得子序列，剩下的元素保持其原始顺序。

示例 1:

输入: [1,7,4,9,2,5]
输出: 6 
解释: 整个序列均为摆动序列。
示例 2:

输入: [1,17,5,10,13,15,10,5,16,8]
输出: 7
解释: 这个序列包含几个长度为 7 摆动序列，其中一个可为[1,17,10,13,10,16,8]。
示例 3:

输入: [1,2,3,4,5,6,7,8,9]
输出: 2
进阶:
你能否用 O(n) 时间复杂度完成此题?

```java
class Solution {
    public int wiggleMaxLength(int[] nums) {

     int len = nums.length;
     if(len==0) {
         return 0;
     } 

     //0 升序
     //1降序
     int dp[][] = new int [len+1][2];
     dp[0][0] =1;
     dp[0][1] =1;
     for(int i =1;i<len;i++) {
        if(nums[i]>nums[i-1]){
          dp[i][0] = dp[i-1][0]; 
          dp[i][1] = dp[i-1][0] +1;
        } 
        else if(nums[i]<nums[i-1]){
          dp[i][0] = dp[i-1][1]+1; 
          dp[i][1] = dp[i-1][1];
        } else{
          dp[i][0] = dp[i-1][0]; 
          dp[i][1] = dp[i-1][1];
        }
     }
     int max =1; 

     for(int i =0;i<len;i++){
         max = Math.max(max,Math.max(dp[i][0],dp[i][1]));
     }
     return max;

    }
}
```

# 21  [10. 正则表达式匹配](https://leetcode-cn.com/problems/regular-expression-matching/)

给你一个字符串 s 和一个字符规律 p，请你来实现一个支持 '.' 和 '*' 的正则表达式匹配。

'.' 匹配任意单个字符
'*' 匹配零个或多个前面的那一个元素
所谓匹配，是要涵盖 整个 字符串 s的，而不是部分字符串。

说明:

s 可能为空，且只包含从 a-z 的小写字母。
p 可能为空，且只包含从 a-z 的小写字母，以及字符 . 和 *。
示例 1:

输入:
s = "aa"
p = "a"
输出: false
解释: "a" 无法匹配 "aa" 整个字符串。
示例 2:

输入:
s = "aa"
p = "a*"
输出: true
解释: 因为 '*' 代表可以匹配零个或多个前面的那一个元素, 在这里前面的元素就是 'a'。因此，字符串 "aa" 可被视为 'a' 重复了一次。
示例 3:

输入:
s = "ab"
p = ".*"
输出: true
解释: ".*" 表示可匹配零个或多个（'*'）任意字符（'.'）。
示例 4:

输入:
s = "aab"
p = "c*a*b"
输出: true
解释: 因为 '*' 表示零个或多个，这里 'c' 为 0 个, 'a' 被重复一次。因此可以匹配字符串 "aab"。
示例 5:

输入:
s = "mississippi"
p = "mis*is*p*."
输出: false

```java
class Solution {
    public boolean isMatch(String s, String p) {
     if(s==null||p==null) {
         return false;
     }
     int m = s.length();
     int n = p.length();    
     boolean dp[][] = new boolean [m+1][n+1];

     char chars [] = s.toCharArray();
     char charP [] = p.toCharArray();

     if(m==0&&n==0){
         return true;
     }

     dp[0][0] = true;
     for (int j = 1; j < n + 1; j++) {
         if (charP[j - 1] == '*') {
          dp[0][j] = dp[0][j - 2];
         }
   }

      for(int i =1;i<=m;i++) {
         for(int j =1;j<=n;j++) {
         if(chars[i-1] ==charP[j-1]||charP[j-1] =='.'){
             dp[i][j] = dp[i-1][j-1]; 
         } else if(charP[j-1] =='*') {
             // 化为 0个或1个及以上
//dp[i-1][j] 1个及以上 前一个字母， abcd 与acd* 这个场景下 abcd 与acdd*,最后一个d约掉变成 adc和acd* 变成 dp[i-1][j]问题了
// 
             if(chars[i-1] ==charP[j-2]||charP[j-2] =='.'){
               dp[i][j] = dp[i][j-2] ||dp[i-1][j];
             }
             //*前面的字母只能选0个 
             else{
               dp[i][j] = dp[i][j-2];
             }
         } else{
             dp[i][j] = false;
         }
       }
      }
     return dp[m][n];
    }
}
```

# 22  路径问题（必考）

## [63. 不同路径 II](https://leetcode-cn.com/problems/unique-paths-ii/)

```java
class Solution {
    public int uniquePathsWithObstacles(int[][] obstacleGrid) {
        
      int m = obstacleGrid.length;
      if (m==0) {
          return 0;
      }
      int n = obstacleGrid[0].length;

      int [][] dp = new int [m+1][n+1];

      for(int i =0;i<m;i++) {
          if(obstacleGrid[i][0]!=1) {
             dp[i+1][1] =1; 
          } else{
              break;
          }
      } 
      for(int j =0;j<n;j++) {
          if(obstacleGrid[0][j]!=1){
            dp[1][j+1] = 1;
          } else{
              break;
          }
          
      }  

      for(int i =1;i<m;i++) {
          for(int j =1;j<n;j++) {
             if(obstacleGrid[i][j]!=1) {
               dp[i+1][j+1] = dp[i+1][j] + dp[i][j+1];
             }
          }
      }

      return dp[m][n];   



    }
}
```



## 64 [最小路径和](https://leetcode-cn.com/problems/minimum-path-sum/)

给定一个包含非负整数的 m x n 网格，请找出一条从左上角到右下角的路径，使得路径上的数字总和为最小。

说明：每次只能向下或者向右移动一步。

示例:

输入:
[
  [1,3,1],
  [1,5,1],
  [4,2,1]
]
输出: 7
解释: 因为路径 1→3→1→1→1 的总和最小。

## 120. 三角形最小路径和

给定一个三角形，找出自顶向下的最小路径和。每一步只能移动到下一行中相邻的结点上。

相邻的结点 在这里指的是 下标 与 上一层结点下标 相同或者等于 上一层结点下标 + 1 的两个结点。

 

例如，给定三角形：

[
     [2],
    [3,4],
   [6,5,7],
  [4,1,8,3]
]
自顶向下的最小路径和为 11（即，2 + 3 + 5 + 1 = 11）。

说明：如果你可以只使用 O(n) 的额外空间（n 为三角形的总行数）来解决这个问题，那么你的算法会很加分。

## 174. 地下城游戏

一些恶魔抓住了公主（P）并将她关在了地下城的右下角。地下城是由 M x N 个房间组成的二维网格。我们英勇的骑士（K）最初被安置在左上角的房间里，他必须穿过地下城并通过对抗恶魔来拯救公主。

骑士的初始健康点数为一个正整数。如果他的健康点数在某一时刻降至 0 或以下，他会立即死亡。

有些房间由恶魔守卫，因此骑士在进入这些房间时会失去健康点数（若房间里的值为负整数，则表示骑士将损失健康点数）；其他房间要么是空的（房间里的值为 0），要么包含增加骑士健康点数的魔法球（若房间里的值为正整数，则表示骑士将增加健康点数）。

为了尽快到达公主，骑士决定每次只向右或向下移动一步。

 

编写一个函数来计算确保骑士能够拯救到公主所需的最低初始健康点数。

例如，考虑到如下布局的地下城，如果骑士遵循最佳路径 右 -> 右 -> 下 -> 下，则骑士的初始健康点数至少为 7。

![1599931047553](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\1599931047553.png)


说明:

骑士的健康点数没有上限。

任何房间都可能对骑士的健康点数造成威胁，也可能增加骑士的健康点数，包括骑士进入的左上角房间以及公主被监禁的右下角房间。

# 23 [44. 通配符匹配](https://leetcode-cn.com/problems/wildcard-matching/)

给定一个字符串 (s) 和一个字符模式 (p) ，实现一个支持 '?' 和 '*' 的通配符匹配。

'?' 可以匹配任何单个字符。
'*' 可以匹配任意字符串（包括空字符串）。
两个字符串完全匹配才算匹配成功。

说明:

s 可能为空，且只包含从 a-z 的小写字母。
p 可能为空，且只包含从 a-z 的小写字母，以及字符 ? 和 *。
示例 1:

输入:
s = "aa"
p = "a"
输出: false
解释: "a" 无法匹配 "aa" 整个字符串。
示例 2:

输入:
s = "aa"
p = "*"
输出: true
解释: '*' 可以匹配任意字符串。
示例 3:

输入:
s = "cb"
p = "?a"
输出: false
解释: '?' 可以匹配 'c', 但第二个 'a' 无法匹配 'b'。
示例 4:

输入:
s = "adceb"
p = "*a*b"
输出: true
解释: 第一个 '*' 可以匹配空字符串, 第二个 '*' 可以匹配字符串 "dce".
示例 5:

输入:
s = "acdcb"
p = "a*c?b"
输出: false

```java
class Solution {
    public boolean isMatch(String s, String p) {

    if(s==null||p==null){
        return false;
    }

    int m = s.length();
    int n = p.length();

    boolean dp[][] = new boolean[m+1][n+1];
    char chars[] = s.toCharArray();
    char charp[] = p.toCharArray();

    dp[0][0] = true;

    for(int j =1;j<=n;j++){
        dp[0][j] = dp[0][j-1] &&(charp[j-1]=='*');
    }
    

    for(int i =1;i<=m;i++) {
         for(int j=1;j<=n;j++) {
          if(chars[i-1] == charp[j-1] || charp[j-1]== '?')  {
              dp[i][j] = dp[i-1][j-1];
          }      
          else if (charp[j-1]=='*'){
              for(int w =i;w>=0;w--){
               dp[i][j] = dp[w][j-1]||dp[i][j];
               if(dp[i][j]){
                   break;
               }
              }
          }
         }
    }  

     return dp[m][n];


    }
}
```

# 24 [403. 青蛙过河](https://leetcode-cn.com/problems/frog-jump/)

一只青蛙想要过河。 假定河流被等分为 x 个单元格，并且在每一个单元格内都有可能放有一石子（也有可能没有）。 青蛙可以跳上石头，但是不可以跳入水中。

给定石子的位置列表（用单元格序号升序表示）， 请判定青蛙能否成功过河（即能否在最后一步跳至最后一个石子上）。 开始时， 青蛙默认已站在第一个石子上，并可以假定它第一步只能跳跃一个单位（即只能从单元格1跳至单元格2）。

如果青蛙上一步跳跃了 k 个单位，那么它接下来的跳跃距离只能选择为 k - 1、k 或 k + 1个单位。 另请注意，青蛙只能向前方（终点的方向）跳跃。

请注意：

石子的数量 ≥ 2 且 < 1100；
每一个石子的位置序号都是一个非负整数，且其 < 231；
第一个石子的位置永远是0。
示例 1:

[0,1,3,5,6,8,12,17]

总共有8个石子。
第一个石子处于序号为0的单元格的位置, 第二个石子处于序号为1的单元格的位置,
第三个石子在序号为3的单元格的位置， 以此定义整个数组...
最后一个石子处于序号为17的单元格的位置。

返回 true。即青蛙可以成功过河，按照如下方案跳跃： 
跳1个单位到第2块石子, 然后跳2个单位到第3块石子, 接着 
跳2个单位到第4块石子, 然后跳3个单位到第6块石子, 
跳4个单位到第7块石子, 最后，跳5个单位到第8个石子（即最后一块石子）。
示例 2:

[0,1,2,3,4,8,9,11]

返回 false。青蛙没有办法过河。 
这是因为第5和第6个石子之间的间距太大，没有可选的方案供青蛙跳跃过去。

```java
/*
思路①、使用二维数组的动态规划
         动态规划
         dp[i][k] 表示能否由前面的某一个石头 j 通过跳 k 步到达当前这个石头 i ，这个 j 的范围是 [1, i - 1]
         当然，这个 k 步是 i 石头 和 j 石头之间的距离
         那么对于 j 石头来说，跳到 j 石头的上一个石头的步数就必须是这个 k - 1 || k || k + 1
         由此可得状态转移方程：dp[i][k] = dp[j][k - 1] || dp[j][k] || dp[j][k + 1]
*/
class Solution {
    public boolean canCross(int[] stones) {
        
        int len = stones.length;

        if(stones[1] != 1){
            return false;
        }
        
        boolean[][] dp = new boolean[len][len + 1];
        dp[0][0] = true;
        for(int i = 1; i < len; i++){
            for(int j = 0; j < i; j++){
                int k = stones[i] - stones[j];
                /*
                	为什么有这么个判断？
                	因为其他石头跳到第 i 个石头跳的步数 k 必定满足 k <= i
                	这又是为什么？
                	1、比如 nums = [0,1,3,5,6,8,12,17]
                	   那么第 0 个石头跳到第 1 个石头，步数肯定为 1，然后由于后续最大的步数是 k + 1，因此第 1 个石头最大只能跳 2 个单位
                 	   因此如果逐个往上加，那么第 2 3 4 5 ... 个石头最多依次跳跃的步数是 3 4 5 6...
                	2、 第 i 个石头能跳的最大的步数是 i + 1，那么就意味着其他石头 j 跳到第 i 个石头的最大步数只能是 i 或者 j + 1
                	   而 这个 k 是其他石头跳到 i 石头上来的，因此 k 必须 <= i （或者是 k <= j + 1）
                */
                if(k <= i){
                    dp[i][k] = dp[j][k - 1] || dp[j][k] || dp[j][k + 1];
                    //提前结束循环直接返回结果
                    if(i == len - 1 && dp[i][k]){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
```



# 26 [887. 鸡蛋掉落](https://leetcode-cn.com/problems/super-egg-drop/)（考过）

你将获得 K 个鸡蛋，并可以使用一栋从 1 到 N  共有 N 层楼的建筑。

每个蛋的功能都是一样的，如果一个蛋碎了，你就不能再把它掉下去。

你知道存在楼层 F ，满足 0 <= F <= N 任何从高于 F 的楼层落下的鸡蛋都会碎，从 F 楼层或比它低的楼层落下的鸡蛋都不会破。

每次移动，你可以取一个鸡蛋（如果你有完整的鸡蛋）并把它从任一楼层 X 扔下（满足 1 <= X <= N）。

你的目标是确切地知道 F 的值是多少。

无论 F 的初始值如何，你确定 F 的值的最小移动次数是多少？

 

示例 1：

输入：K = 1, N = 2
输出：2
解释：
鸡蛋从 1 楼掉落。如果它碎了，我们肯定知道 F = 0 。
否则，鸡蛋从 2 楼掉落。如果它碎了，我们肯定知道 F = 1 。
如果它没碎，那么我们肯定知道 F = 2 。
因此，在最坏的情况下我们需要移动 2 次以确定 F 是多少。
示例 2：

输入：K = 2, N = 6
输出：3
示例 3：

输入：K = 3, N = 14
输出：4

# 27 [413. 等差数列划分](https://leetcode-cn.com/problems/arithmetic-slices/)

数组 A 包含 N 个数，且索引从0开始。数组 A 的一个子数组划分为数组 (P, Q)，P 与 Q 是整数且满足 0<=P<Q<N 。

如果满足以下条件，则称子数组(P, Q)为等差数组：

元素 A[P], A[p + 1], ..., A[Q - 1], A[Q] 是等差的。并且 P + 1 < Q 。

函数要返回数组 A 中所有为等差数组的子数组个数。

示例:

A = [1, 2, 3, 4]

返回: 3, A 中有三个子等差数组: [1, 2, 3], [2, 3, 4] 以及自身 [1, 2, 3, 4]。

```java
class Solution {
    public int numberOfArithmeticSlices(int[] A) {
       
       int len = A.length;
       if(len<3) {
           return 0;
       }
       boolean dp[][] = new boolean[len+1][len+1];

       for(int i=0;i<len-2;i++)  {
          for(int j=i+2;j<len;j++) {
              if(j-i==2){
                 dp[i][j] = A[j]-A[j-1]==A[j-1]-A[j-2]; 
              }
              if(dp[i][j-1]) {
                dp[i][j] = dp[i][j]||A[j]-A[j-1]==A[j-1]-A[j-2];
              }           
          } 
       }
       int cunt =0;
        for(int i=0;i<len-2;i++)  {
          for(int j=i+2;j<len;j++) {
              if(dp[i][j]){
                cunt++;  
              }
          } 
       }
       return cunt; 

    }
}
```

# 28 [517. 超级洗衣机](https://leetcode-cn.com/problems/super-washing-machines/)

假设有 n 台超级洗衣机放在同一排上。开始的时候，每台洗衣机内可能有一定量的衣服，也可能是空的。

在每一步操作中，你可以选择任意 m （1 ≤ m ≤ n） 台洗衣机，与此同时将每台洗衣机的一件衣服送到相邻的一台洗衣机。

给定一个非负整数数组代表从左至右每台洗衣机中的衣物数量，请给出能让所有洗衣机中剩下的衣物的数量相等的最少的操作步数。如果不能使每台洗衣机中衣物的数量相等，则返回 -1。

 

示例 1：

输入: [1,0,5]

输出: 3

解释: 
第一步:    1     0 <-- 5    =>    1     1     4
第二步:    1 <-- 1 <-- 4    =>    2     1     3    
第三步:    2     1 <-- 3    =>    2     2     2   
示例 2：

输入: [0,3,0]

输出: 2

解释: 
第一步:    0 <-- 3     0    =>    1     2     0    
第二步:    1     2 --> 0    =>    1     1     1     
示例 3:

输入: [0,2,0]

输出: -1

解释: 
不可能让所有三个洗衣机同时剩下相同数量的衣物。

```java
class Solution {
    public int findMinMoves(int[] machines) {

      int len = machines.length;
      if(len ==0) return 0;
      int sum =0;
      for(int i=0;i<len;i++) {
          sum += machines[i];
      }
      if(sum%len !=0) {
          return -1;
      }
      int mid = sum/len;

      //dp[][0]  多的的
      //dp[][1]   少的
      int dp[][] = new int[len+1][2]; 
      int cunt =0;

      for(int i=1;i<=len;i++) {
           int res =0;
           //往前移动和给后面移动
           int tem =0;
           if(machines[i-1]>mid){
               //一共多出来
              res = machines[i-1] - mid + dp[i-1][0];
              //还多
              if(res>=dp[i-1][1]) {
                 tem = res; 
                 dp[i][0] = res -dp[i-1][1];
                 dp[i][1] =0;
              }
              // 还欠一点 
              else{
                 tem = dp[i-1][1];
                 dp[i][0] =0;
                 dp[i][1]= dp[i-1][1] -res;
              }




           }else if (machines[i-1]<mid) {
               //一共欠
              res = mid -machines[i-1]+ dp[i-1][1];
              //还多
              if(res<=dp[i-1][0]) {
                    tem = dp[i - 1][0];
                    dp[i][1] = 0;
                    dp[i][0] = dp[i - 1][0] - res;
              }
              // 还欠一点 
              else{
                    dp[i][1] = res - dp[i - 1][0];
                    dp[i][0] = 0;
                    tem = dp[i][1];
 
              }

           } else{
               dp[i][0] = dp[i-1][0];
               dp[i][1] = dp[i-1][1];
           }  
           cunt = Math.max(cunt,tem);

      }
      
      return cunt;


    }
}
```

# 29 边界为1的正方形问题 （必考）

#### [221. 最大正方形](https://leetcode-cn.com/problems/maximal-square/)

难度中等532收藏分享切换为英文关注反馈

在一个由 0 和 1 组成的二维矩阵内，找到只包含 1 的最大正方形，并返回其面积。

#### 理解 min(上, 左, 左上) + 1

先来阐述简单共识

 ![image.png](4 DP.assets/8c4bf78cf6396c40291e40c25d34ef56bd524313c2aa863f3a20c1f004f32ab0-image.png) 

若形成正方形（非单 1），以当前为右下角的视角看，则需要：当前格、上、左、左上都是 1
可以换个角度：当前格、上、左、左上都不能受 0 的限制，才能成为正方形


上面详解了 三者取最小 的含义：

图 1：受限于左上的 0
图 2：受限于上边的 0
图 3：受限于左边的 0
数字表示：以此为正方形右下角的最大边长
黄色表示：格子 ? 作为右下角的正方形区域



```java
输入: 

1 0 1 0 0
1 0 1 1 1
1 1 1 1 1
1 0 0 1 0

输出: 4 
    
    
    
    
    public int maximalSquare(char[][] matrix) {
    // base condition
    if (matrix == null || matrix.length < 1 || matrix[0].length < 1) return 0;

    int height = matrix.length;
    int width = matrix[0].length;
    int maxSide = 0;

    // 相当于已经预处理新增第一行、第一列均为0
    int[][] dp = new int[height + 1][width + 1];

    for (int row = 0; row < height; row++) {
        for (int col = 0; col < width; col++) {
            if (matrix[row][col] == '1') {
                dp[row + 1][col + 1] = Math.min(Math.min(dp[row + 1][col], dp[row][col + 1]), dp[row][col]) + 1;
                maxSide = Math.max(maxSide, dp[row + 1][col + 1]);
            }
        }
    }
    return maxSide * maxSide;
}

链接：https://leetcode-cn.com/problems/maximal-square/solution/li-jie-san-zhe-qu-zui-xiao-1-by-lzhlyle/

```

#### [1139. 最大的以 1 为边界的正方形](https://leetcode-cn.com/problems/largest-1-bordered-square/)

给你一个由若干 0 和 1 组成的二维网格 grid，请你找出边界全部由 1 组成的最大 正方形 子网格，并返回该子网格中的元素数量。如果不存在，则返回 0。

示例 1：

输入：grid = [[1,1,1],[1,0,1],[1,1,1]]
输出：9
示例 2：

输入：grid = [[1,1,0,0]]
输出：1


提示：

1 <= grid.length <= 100
1 <= grid[0].length <= 100
grid[i][j] 为 0 或 1

```java
class Solution {
// 使用3维数组dp[n + 1][m + 1][2](数组下标从1开始)
// dp[i][j][0]:表示第i行第j列的1往 左边 最长连续的1的个数
// dp[i][j][1]:表示第i行第j列的1往 上面 最长连续的1的个数
    public int largest1BorderedSquare(int[][] grid) {
     int m = grid.length;
     if(m==0){
         return 0;
     }
    int n = grid[0].length;
    int res =0;
    int dp[][][] = new int[m+1][n+1][2];
    for(int i =1;i<=m;i++) {
      for(int j =1;j<=n;j++) {
        int d=0;  
        if(grid[i-1][j-1]==1) {
          dp[i][j][0] = dp[i-1][j][0]+1;
          dp[i][j][1] = dp[i][j-1][1]+1;

           d = Math.min(dp[i-1][j][0],dp[i][j-1][1]);
           while(d>0) {
             if(dp[i][j-d][0]>d&&dp[i-d][j][1]>d) {
                break;
             }
             d--;
           }
 
          res = Math.max(res,d+1);
        }

      }  
    }

    return res*res;
    }
}
```

#### [1277. 统计全为 1 的正方形子矩阵](https://leetcode-cn.com/problems/count-square-submatrices-with-all-ones/)

给你一个 m * n 的矩阵，矩阵中的元素不是 0 就是 1，请你统计并返回其中完全由 1 组成的 正方形 子矩阵的个数。

示例 1：

输入：matrix =
[
  [0,1,1,1],
  [1,1,1,1],
  [0,1,1,1]
]
输出：15
解释： 
边长为 1 的正方形有 10 个。
边长为 2 的正方形有 4 个。
边长为 3 的正方形有 1 个。
正方形的总数 = 10 + 4 + 1 = 15.
示例 2：

输入：matrix = 
[
  [1,0,1],
  [1,1,0],
  [1,1,0]
]
输出：7
解释：
边长为 1 的正方形有 6 个。 
边长为 2 的正方形有 1 个。
正方形的总数 = 6 + 1 = 7.



# 30  [464. 我能赢吗](https://leetcode-cn.com/problems/can-i-win/)

在 "100 game" 这个游戏中，两名玩家轮流选择从 1 到 10 的任意整数，累计整数和，先使得累计整数和达到 100 的玩家，即为胜者。

如果我们将游戏规则改为 “玩家不能重复使用整数” 呢？

例如，两个玩家可以轮流从公共整数池中抽取从 1 到 15 的整数（不放回），直到累计整数和 >= 100。

给定一个整数 maxChoosableInteger （整数池中可选择的最大数）和另一个整数 desiredTotal（累计和），判断先出手的玩家是否能稳赢（假设两位玩家游戏时都表现最佳）？

你可以假设 maxChoosableInteger 不会大于 20， desiredTotal 不会大于 300。

示例：

输入：
maxChoosableInteger = 10
desiredTotal = 11

输出：
false

解释：
无论第一个玩家选择哪个整数，他都会失败。
第一个玩家可以选择从 1 到 10 的整数。
如果第一个玩家选择 1，那么第二个玩家只能选择从 2 到 10 的整数。
第二个玩家可以通过选择整数 10（那么累积和为 11 >= desiredTotal），从而取得胜利.
同样地，第一个玩家选择任意其他整数，第二个玩家都会赢。

```java

```









# 31 

# 32 双串问题

## 33  [115. 不同的子序列](https://leetcode-cn.com/problems/distinct-subsequences/)（必考）

给定一个字符串 S 和一个字符串 T，计算在 S 的子序列中 T 出现的个数。

一个字符串的一个子序列是指，通过删除一些（也可以不删除）字符且不干扰剩余字符相对位置所组成的新字符串。（例如，"ACE" 是 "ABCDE" 的一个子序列，而 "AEC" 不是）

题目数据保证答案符合 32 位带符号整数范围。

示例 1：

输入：S = "rabbbit", T = "rabbit"
输出：3
解释：

如下图所示, 有 3 种可以从 S 中得到 "rabbit" 的方案。
(上箭头符号 ^ 表示选取的字母)

rabbbit
^^^^ ^^
rabbbit
^^ ^^^^
rabbbit
^^^ ^^^
示例 2：

输入：S = "babgbag", T = "bag"
输出：5
解释：

如下图所示, 有 5 种可以从 S 中得到 "bag" 的方案。 
(上箭头符号 ^ 表示选取的字母)

babgbag
^^ ^
babgbag
^^    ^
babgbag
^    ^^
babgbag
  ^  ^^
babgbag
    ^^^

```java
class Solution {
    public int numDistinct(String s, String t) {
        //t的前i个数，有S的前j个 
        int dp[][] = new int[t.length()+1][s.length()+1];

        for (int i =0;i<t.length()+1;i++){
            dp[i][0] = 0;
        }
        //双串问题 初始值特别重要
        //t  c 
        //s  adc
        for (int j =0;j<s.length()+1;j++){
            dp[0][j] =1;
        }

            
            for (int j =1;j<s.length()+1;j++){
             for (int i =1;i<t.length()+1;i++){   
                if (s.charAt(j-1)==t.charAt(i-1)){
                    dp[i][j] = dp[i][j-1]+dp[i-1][j-1];
                }else {
                    dp[i][j] = dp[i][j-1];
                }
            }
        }
        return dp[t.length()][s.length()];
    }
}
```

## [97. 交错字符串](https://leetcode-cn.com/problems/interleaving-string/)（可能考）

给定三个字符串 s1, s2, s3, 验证 s3 是否是由 s1 和 s2 交错组成的。 

输入：s1 = "aabcc", s2 = "dbbca", s3 = "aadbbcbcac"
输出：true

输入：s1 = "aabcc", s2 = "dbbca", s3 = "aadbbbaccc"
输出：false

 ![image.png](https://pic.leetcode-cn.com/5b5dc439d4ec4bdb35a68607a86558ff8b820e70726eeaf4178dc44a49ea9a33-image.png) 

```java
class Solution {
    public boolean isInterleave(String s1, String s2, String s3) {

     int m = s1.length();
     int n = s2.length();
     int t = s3.length();

     if(m+n !=t){
         return false;
     }
     // i，j 形成 i+j个s3
     boolean dp[][] = new boolean[m+1][n+1];
     dp[0][0] = true;
     for(int i =1;i<=m;i++){
         if(s1.charAt(i-1) == s3.charAt(i-1)){
         dp[i][0] = true; 
         } else{
             break;
         }
     }
     for(int i =1;i<=n;i++){
         if(s2.charAt(i-1) == s3.charAt(i-1)){
         dp[0][i] = true; 
         } else{
             break;
         }
     }

     for(int i =0;i<m;i++){
      for(int j =0;j<n;j++){

        dp[i+1][j+1] = dp[i][j+1] && s1.charAt(i) ==s3.charAt(i+j+1) || dp[i+1][j] && s2.charAt(j) ==s3.charAt(i+j+1);

      }
     }
     return dp[m][n];
    }
}
```



# 34   

# 35 [1278. 分割回文串 III](https://leetcode-cn.com/problems/palindrome-partitioning-iii/)

# 36  [132. 分割回文串 II](https://leetcode-cn.com/problems/palindrome-partitioning-ii/)

给定一个字符串 *s*，将 *s* 分割成一些子串，使每个子串都是回文串。

返回符合要求的最少分割次数。

**示例:**

```java
输入: "aab"
输出: 1
解释: 进行一次分割就可将 s 分割成 ["aa","b"] 这样两个回文子串。

class Solution {
    public int minCut(String s) {
     if(s==null || s.length()==0) {
        return 0;
     }
    

    int len = s.length();
    
    int dp[] = new int [len+1];
    
    
    for(int i =0;i<=len;i++) {
        dp[i] = i-1;
        for(int j =i-1;j>=0;j--){
          if(isCyle(s.substring(j,i))) {
            dp[i] = Math.min(dp[i],dp[j]+1); 
          }
        }
    }

    return dp[len]; 

    }
    private boolean isCyle(String s) {
       int i =0;
       int j = s.length()-1;
       if(i>j){
           return false;
       }
       while(i<j){
        if(s.charAt(i) != s.charAt(j)){
            return false;
        }
        i++;
        j--;
       }
       return true;
    }
}

```

# 33 [410. 分割数组的最大值](https://leetcode-cn.com/problems/split-array-largest-sum/)（区间dp）

给定一个非负整数数组和一个整数 m，你需要将这个数组分成 m 个非空的连续子数组。设计一个算法使得这 m 个子数组各自和的最大值最小。

注意:
数组长度 n 满足以下条件:

1 ≤ n ≤ 1000
1 ≤ m ≤ min(50, n)
示例:

输入:
nums = [7,2,5,10,8]
m = 2

输出:
18

解释:
一共有四种方法将nums分割为2个子数组。
其中最好的方式是将其分为[7,2,5] 和 [10,8]，
因为此时这两个子数组各自的和的最大值为18，在所有情况中最小。

```java
class Solution {
    public int splitArray(int[] nums, int m) {

        int n = nums.length;
        int[][] f = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            Arrays.fill(f[i], Integer.MAX_VALUE);
        }
        int[] sub = new int[n + 1];
        for (int i = 0; i < n; i++) {
            sub[i + 1] = sub[i] + nums[i];
        }
        f[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= Math.min(i, m); j++) {
                for (int k = 0; k < i; k++) {
                    f[i][j] = Math.min(f[i][j], Math.max(f[k][j - 1], sub[i] - sub[k]));
                }
            }
        }
        return f[n][m];
    }
}
```

# 34 

# 35

# 37 数字拆分问题

## 279. 完全平方数

给定正整数 n，找到若干个完全平方数（比如 1, 4, 9, 16, ...）使得它们的和等于 n。你需要让组成和的完全平方数的个数最少。

示例 1:

输入: n = 12
输出: 3 
解释: 12 = 4 + 4 + 4.
示例 2:

输入: n = 13
输出: 2
解释: 13 = 4 + 9.

## 343. 整数拆分

给定一个正整数 n，将其拆分为至少两个正整数的和，并使这些整数的乘积最大化。 返回你可以获得的最大乘积。

示例 1:

输入: 2
输出: 1
解释: 2 = 1 + 1, 1 × 1 = 1。
示例 2:

输入: 10
输出: 36
解释: 10 = 3 + 3 + 4, 3 × 3 × 4 = 36。

##  [91. 解码方法](https://leetcode-cn.com/problems/decode-ways/)

一条包含字母 `A-Z` 的消息通过以下方式进行了编码：

```
'A' -> 1
'B' -> 2
...
'Z' -> 26
```

给定一个只包含数字的**非空**字符串，请计算解码方法的总数。

**示例 1:**

```
输入: "12"
输出: 2
解释: 它可以解码为 "AB"（1 2）或者 "L"（12）。
```

**示例 2:**

```
输入: "226"
输出: 3
解释: 它可以解码为 "BZ" (2 26), "VF" (22 6), 或者 "BBF" (2 2 6) 。
```

```java
class Solution {
    public int numDecodings(String s) {
     int len = s.length();
     List<String> diction = new ArrayList<>();
     diction.add("1");
     diction.add("2");
     diction.add("3");
     diction.add("4");
     diction.add("5");
     diction.add("6");
     diction.add("7");
     diction.add("8");
     diction.add("9");
     diction.add("10");
     diction.add("11");
     diction.add("12");
     diction.add("13");
     diction.add("14");
     diction.add("15");
     diction.add("16");
     diction.add("11");
     diction.add("12");
     diction.add("13");
     diction.add("14");
     diction.add("15");
     diction.add("16");
     diction.add("17");
     diction.add("18");
     diction.add("19");
     diction.add("20");
     diction.add("21");
     diction.add("22");
     diction.add("23");
     diction.add("24");
     diction.add("25");
     diction.add("26");

     int dp[] = new int[len+1];
     dp[0] = 1;
     if(len>=1 && is(s.substring(0,1),diction)){
       dp[1]=1;
     }


     for(int i =2;i<=len;i++){
        if(is(s.substring(i-1,i),diction)){
           dp[i] += dp[i-1];
        }
        if(is(s.substring(i-2,i),diction)){
           dp[i] += dp[i-2];
        }
    }
     return dp[len];
    }

    private boolean is(String s,List<String> diction){
        return diction.contains(s);
    }
}
```



# 39 673[最长递增子序列的个数](https://leetcode-cn.com/problems/number-of-longest-increasing-subsequence) 最长递增子序列的个数  

给定一个未排序的整数数组，找到最长递增子序列的个数。

示例 1:

输入: [1,3,5,4,7]
输出: 2
解释: 有两个最长递增子序列，分别是 [1, 3, 4, 7] 和[1, 3, 5, 7]。
示例 2:

输入: [2,2,2,2,2]
输出: 5
解释: 最长递增子序列的长度是1，并且存在5个子序列的长度为1，因此输出5。

```java
class Solution {
    public int findNumberOfLIS(int[] nums) {

     int len = nums.length;
     if(len==0) {
         return 0;
     } 
     int dp [] = new int [len+1];
     //用来求以nums[i]结尾的最长递增子序列的个数
     int cunt [] = new int [len+1];
     Arrays.fill(cunt,1);
     Map<Integer,Integer> map = new HashMap<>();
     int max =1;
     for(int i =1;i<=len;i++) {
         dp[i] =1;
         for(int j =i-1;j>=1;j--) {
             if(nums[i-1]>nums[j-1]){

                 if(dp[j]+1>dp[i]) {
                     cunt[i] =cunt[j];
                 } else if(dp[j]+1==dp[i]){
                     cunt[i] += cunt[j];
                 }

                 dp[i] = Math.max(dp[i],dp[j]+1);
                 max = Math.max(dp[i],max);              
             }      
         }
     }
    int ans =0;
    for(int i =1;i<=len;i++) {
             if(dp[i]== max){
               ans  += cunt[i];  
            }              
 
     }  
    return ans ;
    }
}
```



# 40 674[最长连续递增序列](https://leetcode-cn.com/problems/longest-continuous-increasing-subsequence) 

给定一个未经排序的整数数组，找到最长且连续的的递增序列，并返回该序列的长度。

 

示例 1:

输入: [1,3,5,4,7]
输出: 3
解释: 最长连续递增序列是 [1,3,5], 长度为3。
尽管 [1,3,5,7] 也是升序的子序列, 但它不是连续的，因为5和7在原数组里被4隔开。 
示例 2:

输入: [2,2,2,2,2]
输出: 1
解释: 最长连续递增序列是 [2], 长度为1。

方法一：

```java
class Solution {
    public int findLengthOfLCIS(int[] nums) {

        int len = nums.length;
        if(len==0) {
            return 0;
        } 
           
        int max =1;
        int cunt =1;

        for(int i =1;i<len;i++) {
           if(nums[i]-nums[i-1]>0){
               cunt ++;
           } else{
               cunt =1;
           } 
         max = Math.max(cunt,max);
        }

         return max;


    }
}
```

方法二：记录所有子序列的办法

# 41 [368. 最大整除子集](https://leetcode-cn.com/problems/largest-divisible-subset/)

给出一个由无重复的正整数组成的集合，找出其中最大的整除子集，子集中任意一对 (Si，Sj) 都要满足：Si % Sj = 0 或 Sj % Si = 0。

如果有多个目标子集，返回其中任何一个均可。

 

示例 1:

输入: [1,2,3]
输出: [1,2] (当然, [1,3] 也正确)
示例 2:

输入: [1,2,4,8]
输出: [1,2,4,8]

只能求出一个子序列

```java
class Solution {
    List<Integer> ans = new ArrayList<>();
    public List<Integer> largestDivisibleSubset(int[] nums) {

        int max =0;
        int len = nums.length;
        List<Integer> res = new ArrayList<>();
        if(len==0) {
            return res;
        }
        Arrays.sort(nums);
        int dp[] = new int[len+1];

        for(int i =0;i<len;i++) {
            dp[i] =1;
            for(int j =i-1;j>=0;j--) {
                if(nums[i]%nums[j]==0|| nums[j]%nums[i]==0) {
                    dp[i] = Math.max(dp[i],dp[j]+1);
                }
            }
        }

        int maxIndex =dp[0];
        for(int i =0;i<len;i++) {
          //  System.out.println("dp"+dp[i]);
            if(dp[i]>max) {
                max = dp[i];
                maxIndex = i;
            }
        }
       // System.out.println("max"+max);

        int maxValue = nums[maxIndex];
        for(int i= maxIndex;i>=0;i--){
            if(dp[i] == max && maxValue%nums[i]==0){
                res.add(nums[i]);
                maxValue =nums[i];
                max--;
            }
        }
        return res;
    }


}
```



# 42 [363. 矩形区域不超过 K 的最大数值和](https://leetcode-cn.com/problems/max-sum-of-rectangle-no-larger-than-k/)

# 面试题 17.24. 最大子矩阵(大厂考过)

给定一个正整数和负整数组成的 N × M 矩阵，编写代码找出元素总和最大的子矩阵。

返回一个数组 [r1, c1, r2, c2]，其中 r1, c1 分别代表子矩阵左上角的行号和列号，r2, c2 分别代表右下角的行号和列号。若有多个满足条件的子矩阵，返回任意一个均可。

注意：本题相对书上原题稍作改动

示例:

输入:
[
   [-1,0],
   [0,-1]
]
输出: [0,1,0,1]
解释: 输入中标粗的元素即为输出所表示的矩阵
说明：

1 <= matrix.length, matrix[0].length <= 200

```java
class Solution {
    public int majorityElement(int[] nums) {
     
     int cunt = 0;
     
     Integer con = null;

     for(int num : nums){
        if(cunt ==0){
            con = num;
        }
        cunt  = cunt + (con == num? 1:-1);
     }
     return con;


    }
}
```



# 43 [357. 计算各个位数不同的数字个数](https://leetcode-cn.com/problems/count-numbers-with-unique-digits/)

给定一个非负整数 n，计算各位数字都不同的数字 x 的个数，其中 0 ≤ x < 10n 。

示例:

输入: 2
输出: 91 
解释: 答案应为除去 11,22,33,44,55,66,77,88,99 外，在 [0,100) 区间内的所有数字。

```java
class Solution {
    /**
     * 排列组合：n位有效数字 = 每一位都从 0~9 中选择，且不能以 0 开头
     * 1位数字：0~9                      10
     * 2位数字：C10-2，且第一位不能是0      9 * 9
     * 3位数字：C10-3，且第一位不能是0      9 * 9 * 8
     * 4位数字：C10-4，且第一位不能是0      9 * 9 * 8 * 7
     * ... ...
     * 最后，总数 = 所有 小于 n 的位数个数相加
     */
    public int countNumbersWithUniqueDigits(int n) {
        if (n == 0) return 1;
        int first = 10, second = 9 * 9;
        int size = Math.min(n, 10);
        for (int i = 2; i <= size; i++) {
            first += second;
            second *= 10 - i;
        }
        return first;
    }
}
```













# 44 [LCP 19. 秋叶收藏集](https://leetcode-cn.com/problems/UlBDOe/)

