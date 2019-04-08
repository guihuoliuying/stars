package com.stars.modules.raffle.define;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang by 2017/4/21
 */

public class SumRewardRate implements Comparable<SumRewardRate> {
    private int minnum;
    private List<Weight> weights;

    public SumRewardRate() {
    }

    SumRewardRate(int minnum, List<Weight> weights) {
        this.minnum = minnum;
        this.weights = weights;
    }

    public int getMinnum() {
        return minnum;
    }

    public void setMinnum(int minnum) {
        this.minnum = minnum;
    }

    public String getWeight() {
        StringBuffer sb = new StringBuffer();
        for (Weight weight : weights) {
            sb.append(weight.toString()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public void setWeight(String weightContent) {
        String[] weightArr = weightContent.split(",");
        ArrayList<Weight> weightList = new ArrayList<Weight>();
        for (String weighStr : weightArr) {
            Weight weigh = Weight.parse(weighStr);
            weightList.add(weigh);
        }
        this.weights = weightList;
    }

    public List<Weight> getWeighList() {
        return weights;
    }

    @Override
    public int compareTo(SumRewardRate o) {
        return minnum - o.minnum;
    }

    public static class Weight {
        public final float rate;
        public final int power;

        Weight(float rate, int power) {
            this.rate = rate;
            this.power = power;
        }

        public String toString() {
            return String.format("%s+%s", rate, power);
        }

        public static Weight parse(String weightContent) {
            String[] params = weightContent.split("\\+");
            if (params.length != 2) {
                throw new RuntimeException("<raffle.define.SumRewardRate.Weight> parse() params is not match!");
            }
            float rate = Float.parseFloat(params[0]);
            rate = rate / 100;
            int power = Integer.parseInt(params[1]);
            return new Weight(rate, power);
        }
    }

}
