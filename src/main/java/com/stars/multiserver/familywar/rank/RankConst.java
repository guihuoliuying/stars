package com.stars.multiserver.familywar.rank;

/**
 * Created by chenkeyu on 2017-06-29.
 */
public class RankConst {

    public static int period = 600;
    public static int type;
    public static int size = 100;
    public static final int W_TYPE_QUALIFY = 2;
    public static final int W_TYPE_REMOTE = 3;
    public static final int W_TYPE_NONE = 0;

    public static final String NONE_QUALIFY = "无海选资格";
    public static final String NONE_REMOTE = "无决赛资格";
    public static final String HAVE_QUALIFY = "可参与海选";
    public static final String HAVE_REMOTE = "可参与决赛";
    public static final String CHAMPION = "跨服冠军";
    public static final String RUNNER_UP = "跨服亚军";
    public static final String THIRD_RUNNER = "跨服季军";
    public static final String FOURTH_RUNNER = "跨服殿军";
    public static final String TOP_EIGHT = "跨服八强";
    public static final String TOP_SIXTEEN = "跨服十六强";
    public static final String TOP_THIRTY_TWO = "跨服三十二强";
    public static final String NONE = "";

    public static String getRank(int rank) {
        switch (rank) {
            case 1:
                return CHAMPION;
            case 2:
                return RUNNER_UP;
            case 3:
                return THIRD_RUNNER;
            case 4:
                return FOURTH_RUNNER;
            case 8:
                return TOP_EIGHT;
            case 16:
                return TOP_SIXTEEN;
            case 32:
                return TOP_THIRTY_TWO;
            default:
                return HAVE_REMOTE;
        }
    }
}
