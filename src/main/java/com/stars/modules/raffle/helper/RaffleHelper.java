package com.stars.modules.raffle.helper;

import com.stars.modules.raffle.define.RaffleDefineManager;
import com.stars.modules.raffle.define.RaffleReward;
import com.stars.modules.raffle.define.Range;
import com.stars.modules.raffle.define.SumRewardRate;
import com.stars.modules.raffle.define.SumRewardRate.Weight;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author likang by 2017/4/22
 */

public class RaffleHelper {

    public static int mapReffleRewardGroup(int vipLevel) {
        RaffleReward reward = RaffleDefineManager.instance.getRaffleRewardBy(vipLevel);
        if (reward == null) {
            return 0;
        }
        return reward.getRewardIndex();
    }

    /**
     * 1 5 9 10  14 17 18 19 15
     *
     * @param
     * @return
     */
//    public static List<Integer> createSpeed(int rewardIndex) {
//        // 实现种子的算法
//        int size = RaffleDefineManager.instance.getCommonConfig().times;
//        if (size <= 1) {
//            throw new RuntimeException("Raffle createSpeed size must biggger 1.");
//        }
//        int total = RaffleDefineManager.instance.getRewardEntrysSize(rewardIndex);
//        // 每一步的范围是[-4,-1] 或 [1,4]
//        int positive = 4, negative = -4;
//        List<Integer> result = new ArrayList<Integer>(size);
//        int sum = 0, index = 0;
//        while (index < size - 1) {
//            int min = Math.max(1 - sum, negative);
//            int maxStep = (size - index - 1) * positive + sum - total;
//            if (maxStep <= 0) {
//                min = -maxStep;
//            } else {
//                if (min < 0 && Math.abs(min) > maxStep) {
//                    min = -maxStep;
//                }
//            }
//            int max = Math.min(total - sum - 1, positive);
//            int value = randomExcludeZero(min, max);
//            sum += value;
//            result.add(index++, value);
//        }
//        result.add(size - 1, total - sum);
//        LogUtil.info(String.format("RaffleHelper.createSpeed() result=%s", StringUtil.makeString(result, ',')));
//        return result;
//    }
    public static int randomExcludeZero(int min, int max) {
        if (min > max) {
            throw new RuntimeException("RaffleHelper.randomExcludeZero params is error!");
        }
        if (min == 0) {
            return RandomUtil.randLeft(min, max);
        }
        if (max == 0) {
            return -RandomUtil.randLeft(max, -min);
        }
        if (min > 0) {
            return RandomUtil.rand(min, max);
        }
        if (max < 0) {
            return -RandomUtil.rand(-max, -min);
        }
        // 在跨了正负区间是就分两个区间[min,0)并(0,max]
        boolean flag = Math.random() < ((float) (-min) * 1.0f) / (max - min);
        if (flag) {
            return -RandomUtil.randLeft(0, -min);
        } else {
            return RandomUtil.randLeft(0, max);
        }
    }

    public static int getRandomMoney(Range range) {
        return RandomUtil.rand(range.left, range.right);
    }

    public static float findNearRate(int totalMoney) {
        List<SumRewardRate> rewardRates = RaffleDefineManager.instance.getRewardRates();
        if (rewardRates == null) {
            com.stars.util.LogUtil.error("RaffleDefineManager.findNearRate() rewardRates is null。");
            return 0.1f;
        }
        int index = rewardRates.size() - 1;
        while (index > 0) {
            SumRewardRate rate = rewardRates.get(index);
            if (rate.getMinnum() <= totalMoney) {
                break;
            }
            index--;
        }
        if (index < 0) {
            com.stars.util.LogUtil.error(String.format("RaffleDefineManager.findNearRate() could not find rate by %s", index));
        }
        SumRewardRate rate = rewardRates.get(index);
        List<Weight> weights = rate.getWeighList();
        List<Weight> result = RandomUtil.powerRandom(weights, "power", 1, false);
        if (result.size() != 1) {
            com.stars.util.LogUtil.error("RaffleDefineManager.findNearRate() could not find rate");
        }
        Weight weight = result.get(0);
        LogUtil.info(String.format("RaffleDefineManager.findNearRate() rate=%s", weight.rate));
        return weight.rate;
    }

    public static void main(String[] args) throws IOException {
        int size = 10;
        int total = 20;
        int segment = 2;
        int _size = size - 1;

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("avaliable.txt"));
        for (int index = 0; index < 1000000; index++) {
            int tmp = RandomUtil.rand(total - 3, total - 1);
            List<Integer> sizeArr = split(_size, segment);
            List<Integer> totalArr = split(tmp, segment);
            List<Integer> speed = new ArrayList<>();
            for (int i = 0; i < sizeArr.size(); i++) {
                List<Integer> subSpeed;
                do {
                    subSpeed = createSubSpeed(sizeArr.get(i), totalArr.get(i));

                }
                while (speed.size() != 0 && ((speed.get(speed.size() - 2) == speed.get(speed.size() - 1) && speed.get(speed.size() - 2) == subSpeed.get(0)) ||
                        (speed.get(speed.size() - 1) == subSpeed.get(0) && subSpeed.get(0) == subSpeed.get(1))));
                speed.addAll(subSpeed);
            }
            speed.add(total - tmp);
            bufferedWriter.append(String.format("%s\n", StringUtil.makeString(speed, ',')));

        }
        bufferedWriter.close();

    }

    public static List<Integer> split(int num, int section) {
        int unit = num / section;
        ArrayList<Integer> splitArr = new ArrayList<>();
        for (int i = 1; i <= section; i++) {
            int number = i * unit;
            if (number <= num && i < section) {
                splitArr.add(unit);
            } else {
                splitArr.add(num - (i - 1) * unit);
            }
        }
        return splitArr;
    }

    public static List<Integer> createSpeed(int rewardIndex) {
        int segment = 2;
        // 实现种子的算法
        int size = RaffleDefineManager.instance.getCommonConfig().times;
        if (size <= 1) {
            throw new RuntimeException("Raffle createSpeed size must biggger 1.");
        }
        int total = RaffleDefineManager.instance.getRewardEntrysSize(rewardIndex);
        int tmp = RandomUtil.rand(total - 3, total - 1);
        int _size = size - 1;
        List<Integer> sizeArr = split(_size, segment);
        List<Integer> totalArr = split(tmp, segment);
        List<Integer> speed = new ArrayList<>();
        for (int i = 0; i < sizeArr.size(); i++) {
            List<Integer> subSpeed;
            do {
                subSpeed = createSubSpeed(sizeArr.get(i), totalArr.get(i));
            }
            while (speed.size() != 0 && ((speed.get(speed.size() - 2) == speed.get(speed.size() - 1) && speed.get(speed.size() - 2) == subSpeed.get(0)) ||
                    (speed.get(speed.size() - 1) == subSpeed.get(0) && subSpeed.get(0) == subSpeed.get(1))));
            speed.addAll(subSpeed);
        }
        speed.add(total - tmp);
        return speed;
    }

    public static List<Integer> createSubSpeed(int size, int total) {
        // 实现种子的算法
        if (size <= 1) {
            throw new RuntimeException("Raffle createSpeed size must biggger 1.");
        }
        // 每一步的范围是[-4,-1] 或 [1,4]
        int positive = 4, negative = -4;
        List<Integer> result = new ArrayList<Integer>(size);
        int sum = 0, index = 0;
        while (index < size - 1) {
            int min = Math.max(1 - sum, negative);
            int maxStep = (size - index - 1) * positive + sum - total;
            if (maxStep <= 0) {
                min = -maxStep;
            } else {
                if (min < 0 && Math.abs(min) > maxStep) {
                    min = -maxStep;
                }
            }
            int max = Math.min(total - sum - 1, positive);
            int value;
            int time = 10;
            do {
                value = randomExcludeZero(min, max);
                time--;
            }
            while (result.size() > 2 && time > 0 && Math.abs(value) == Math.abs(result.get(index - 2)) && Math.abs(value) == Math.abs(result.get(index - 1)));
            sum += value;
            result.add(index++, value);
        }
        result.add(size - 1, total - sum);
        return result;
    }

}
