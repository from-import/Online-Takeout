package com.xxx.takeout;

public class Test {
    public static void main(String[] args) {
        int[] nums = {2,0,0};
        Test test = new Test();
        System.out.println(test.canJump(nums));
    }
    public boolean canJump(int[] nums) {
        int numLength = nums.length;
        int distance;
        for (int i = 0;  i<(numLength); i++){
            distance = numLength - i;
            if(nums[i] == 0){
                if(numLength == 1){
                    return true;
                }
                return false;
            }

            else{
                if(nums[i] >= distance){
                    return true;
                }
            }
        }
        return false;
    }
}
