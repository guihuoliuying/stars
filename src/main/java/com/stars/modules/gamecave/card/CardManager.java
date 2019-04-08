package com.stars.modules.gamecave.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by gaopeidian on 2017/1/13.
 */
public class CardManager {
    private static List<Integer> allCardsId = new ArrayList<Integer>();
    
    public static List<Integer> getAllCardsId(){
    	if (allCardsId.size() <= 0) {
    		CardColorEnum[] colors = CardColorEnum.values();
			for (CardColorEnum colorE : colors) {
				int colorType = colorE.getColor();
				for (int i = 1; i <= 9; i++) {
					allCardsId.add(colorType * 10 + i);
				}
			}
		}
    	
    	return allCardsId;
    }
    
    /**
     * 
     * @param randomCount : 要随机出来的牌数
     * @return 随机出来的牌的id列表
     */
    public static List<Integer> randomCards(int randomCount){
    	List<Integer> ret = new ArrayList<Integer>();
    	List<Integer> tempAllCards = new ArrayList<Integer>(getAllCardsId());
    	Random random = new Random();
    	for (int i = 0; i < randomCount; i++) {
			int size = tempAllCards.size();
			if (size <= 0) {
				return ret;
			}
			int randonIndex = random.nextInt(size);
			int randomCardId = tempAllCards.get(randonIndex);
			ret.add(randomCardId);
			tempAllCards.remove((Integer)randomCardId);
		}
    	
    	return ret;
    }
    
    /**
     * 判断获取牌型，有七种：同花顺，清一色，同花，三张，飞机，对子，其他
     * ps:只适用于三张牌的情况
     * @return 返回-1代表参数有误
     */
    public static int getCardGroupType(List<Integer> cardIds){
    	if (cardIds.size() != 3) {
			return -1;
		}
    	
    	int baseGroupType = getBaseCardGroupType(cardIds);
    	boolean isFlush = isFlush(cardIds);
    	
    	if (baseGroupType == CardGroupEnum.Straight.getGroudType() && isFlush) {//同花顺
			return CardGroupEnum.StraightFlush.getGroudType();
		}else if (baseGroupType == CardGroupEnum.Three.getGroudType() && isFlush) {//清一色
			return CardGroupEnum.AllSame.getGroudType();
		}else if (isFlush) {
			return CardGroupEnum.Flush.getGroudType();
		}else{
			return baseGroupType;
		}
    }
    
    /**
     * 判断获取不包含花色的基础牌型，有四种：三张，飞机，对子，其他
     * ps:只适用于三张牌的情况
     * @return 返回-1代表参数有误
     */
    public static int getBaseCardGroupType(List<Integer> cardIds){
    	if (cardIds.size() != 3) {
			return -1;
		}
    	
    	//先转化成纯数字，无花色的
    	List<Integer> numList = new ArrayList<Integer>();
    	for (Integer cardId : cardIds) {
			numList.add(getCardNumByCardId(cardId));
		}
    	
    	//按数字从小到大排序
    	Collections.sort(numList);
    	
    	//算法：依次后数减前数,通过得出来的是0或1或其他数来判断属于什么牌型
    	List<Integer> calResults = new ArrayList<Integer>();
    	int size = numList.size();
    	for (int i = 0; i <= size - 2; i++) {
			calResults.add(numList.get(i + 1) - numList.get(i));
		}
    	
    	int result1 = calResults.get(0);
    	int result2 = calResults.get(1);
    	
    	//按牌型由大到小判断，优先判断大牌，顺序依次是 三张，飞机，对子，其他
    	if (result1 == 0 && result2 == 0) {
			return CardGroupEnum.Three.getGroudType();
		}else if (result1 == 1 && result2 == 1) {
			return CardGroupEnum.Straight.getGroudType();
		}else if (result1 == 0 || result2 == 0) {
			return CardGroupEnum.Piar.getGroudType();
		}else{
			return CardGroupEnum.HighCard.getGroudType();
		}
    }
    
    /**
     * 判断获取是否是同花
     */
    public static boolean isFlush(List<Integer> cardIds){
    	int size = cardIds.size();
    	for (int i = 0; i <= size - 2; i++) {
			int cardId1 = cardIds.get(i);
			int cardId2 = cardIds.get(i + 1);
			if (getCardColorByCardId(cardId1) != getCardColorByCardId(cardId2)) {
				return false;
			}
		}
    	
    	return true;
    }
    
    public static int getCardNumByCardId(int cardId){
    	return cardId % 10;
    }
    
    public static int getCardColorByCardId(int cardId){
    	return cardId / 10;
    }
    
    
    public static void main(String args[]){
    	for (int i = 0; i < 10; i++) {
    		List<Integer> cardIds = new ArrayList<Integer>();
        	Random random = new Random();
        	cardIds.add(random.nextInt(3) + 1);
        	cardIds.add(random.nextInt(3) + 1);
        	cardIds.add(random.nextInt(3) + 1);
		}
    	
    }
}
