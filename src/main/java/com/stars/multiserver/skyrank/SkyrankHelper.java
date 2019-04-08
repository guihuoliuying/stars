package com.stars.multiserver.skyrank;

import com.stars.services.skyrank.SkyRankKFService;
import com.stars.services.skyrank.SkyRankLocalService;

/**
 * 天梯排行
 * 
 * @author xieyuejun
 *
 */
public class SkyrankHelper {
	static SkyRankKFService skyRankKFService;
	static SkyRankLocalService skyRankLocalService;

	public static SkyRankKFService getSkyRankKFService() {
		return skyRankKFService;
	}
	
    public static SkyRankLocalService skyRankLocalService() {
        return skyRankLocalService;
    }
}
