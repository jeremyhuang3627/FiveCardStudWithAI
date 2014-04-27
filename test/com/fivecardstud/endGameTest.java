package com.fivecardstud;

import static org.junit.Assert.assertEquals;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.fivecardstud.client.Card;
import org.fivecardstud.client.FiveCardStudLogic;
import org.fivecardstud.client.FiveCardStudState;
import org.fivecardstud.client.Card.Rank;
import org.fivecardstud.client.Card.Suit;
import org.game_api.GameApi.Delete;
import org.game_api.GameApi.EndGame;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.Shuffle;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ibm.icu.util.BytesTrie.Iterator;

@RunWith(JUnit4.class)
public class endGameTest {
	
	FiveCardStudLogic fiveCardStudLogic = new FiveCardStudLogic();
	
	private void assertMoveOk(VerifyMove verifyMove)
	{
		fiveCardStudLogic.checkMoveIsLegal(verifyMove);
	}
	
	private void assertHacker(VerifyMove verifyMove) {
	    VerifyMoveDone verifyDone = fiveCardStudLogic.verify(verifyMove);
	    assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	private static final String PLAYER_ID = "playerId";
	private static final String THISTURN = "thisturn";
	private static final String NEXTTURN = "nextturn";
	private static final String W = "W"; // White hand
	private static final String B = "B"; // Black hand
	private static final String G = "G";
	private static final String R = "R";
	private static final String Y = "Y"; 
	private static final String C = "C"; // Card key (C0...C51)
	private static final String STAGE = "stage";
	private static final String DEAL = "deal";
	private static final String BET = "bet";
	private static final String BETEND = "betend";
	private static final String MONEY = "money";
	private static final String DEALER = "dealer";
	private static final String BIG = "bigblind";
	private static final String SMALL = "smallblind";
	private static final String HAND = "hand";
	private static final String DOWNCARD = "downcard"; // card that is hidden
	private static final String ANTE = "ante";
	private static final String BIGANTE = "bigante";
	private static final String SMALLANTE = "smallante";
	private static final String ASSIGNANTE = "assignante";
	private static final String CURRENTCARD = "currentcard";  //the next card on the deck that will be dealt
	private static final String MINIMUM = "minimum";  //the minimum amoutn to call
	private static final String POT = "pot";
	private static final String INITDEAL = "initdeal";
	private static final String ENDGAME = "endgame";
	private static final String PUTSMALLANTE = "putsmallante";
	private static final String PUTBIGANTE = "putbigante";
	private final String wId = "42";
	private final String bId = "43";
	private final String gId = "44"; // dealer
	private final String rId = "45";
	private final String yId = "46";
	private final ImmutableMap<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYER_ID, wId);
	private final ImmutableMap<String, Object> bInfo = ImmutableMap.<String, Object>of(PLAYER_ID, bId);
	private final ImmutableMap<String, Object> gInfo = ImmutableMap.<String, Object>of(PLAYER_ID, gId);
	private final ImmutableMap<String, Object> rInfo = ImmutableMap.<String, Object>of(PLAYER_ID, rId);
	private final ImmutableMap<String, Object> yInfo = ImmutableMap.<String, Object>of(PLAYER_ID, yId);
	private final ImmutableList<Map<String, Object>> playersInfo = ImmutableList.<Map<String, Object>>of(wInfo, bInfo, gInfo, rInfo, yInfo);
	private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
	

	private final List<String> playerIds = Lists.newArrayList();
	{
		playerIds.add(wId);
		playerIds.add(bId);
		playerIds.add(gId);
		playerIds.add(rId);
		playerIds.add(yId);
	}
	
	private VerifyMove move(
		      int lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove) {
		    return new VerifyMove(playersInfo,emptyState,lastState, lastMove, lastMovePlayerId,ImmutableMap.<Integer, Integer>of());
	}
	
	private List<Operation> getInitialOperations(List<Integer> playersId,int dealerId) { // G is dealer, R is BIG , B is small
		List<Operation> initialMove = fiveCardStudLogic.getInitialMove(playersId, dealerId);
		return initialMove;
	}
	
	private void fillMapWithCards(Map<String,Object> state){
		for(int i=0;i<52;i++){
			state.put(C+i,fiveCardStudLogic.cardIdToString(i));
		}
		
		for(int i=0;i<20;i++){
			int ran = (int)(Math.random()*52);
			String c1 = (String)state.get(C+i);
			String c2 = (String)state.get(C+ran);
			state.put(C+i,c2);
			//System.out.println("putting " + c2 + " to " + C + i);
			state.put(C+ran,c1);
			//System.out.println("putting " + c1 + " to " + C + ran);
		}
	}
	
	private List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
	    return fiveCardStudLogic.getIndicesInRange(fromInclusive, toInclusive);
	}
	
	
	Map<String,Object> bettingR  = new HashMap<String,Object>();
	{
		bettingR.put(BET,30);
		bettingR.put(MONEY,50);
		bettingR.put(HAND,getIndicesInRange(0,4));
	}
	
	Map<String,Object> bettingG  = new HashMap<String,Object>();
	{
		bettingG.put(BET,30);
		bettingG.put(MONEY,60);
		bettingG.put(HAND,getIndicesInRange(5,9));
	}
	
	Map<String,Object> bettingB  = new HashMap<String,Object>();
	{
		bettingB.put(BET,30);
		bettingB.put(MONEY,50);
		bettingB.put(HAND,getIndicesInRange(10,14));
	}
	
	Map<String,Object> bettingW  = new HashMap<String,Object>();
	{
		bettingW.put(BET,30);
		bettingW.put(MONEY,20);
		bettingW.put(HAND,getIndicesInRange(15,19));
	}
	
	Map<String,Object> bettingY  = new HashMap<String,Object>();
	{
		bettingY.put(BET,30);
		bettingY.put(MONEY,90);
		bettingY.put(HAND,getIndicesInRange(20,24));
	}
	
	Map<String,Object> endGameState = new HashMap<String,Object>();
	{
		endGameState.put(NEXTTURN, gId);
		endGameState.put(THISTURN, bId);
		endGameState.put(STAGE, BETEND);
		endGameState.put(BIGANTE,50);
		endGameState.put(SMALLANTE,25);
		endGameState.put(DEALER,gId);
		endGameState.put(CURRENTCARD,25);
		endGameState.put(MINIMUM,50);
		endGameState.put(BIG,bId);
		endGameState.put(SMALL,rId);
		endGameState.put(POT,1000);
		endGameState.put(R, bettingR);
		endGameState.put(B, bettingB);
		endGameState.put(G, bettingG);
		endGameState.put(Y, bettingY);
		endGameState.put(W, bettingW);
        fillMapWithCards(endGameState);
	}
	
	List<Operation> lastMove = Lists.newArrayList();
	{
		lastMove.add(new Set(STAGE,ENDGAME));
	}
	
	
	 List<Optional<Card>> deck = Lists.newArrayList();
	 {
	  for(int i=0;i<52;i++)
	  {
		  String cardString = (String) endGameState.get(C + i);
		  Card card;
		  if(cardString == null)
		  {
			  card = null;
		  }else{
			  Rank rank = Rank.fromFirstLetter(cardString.substring(0,cardString.length()-1));
			  Suit suit = Suit.fromFirstLetterLowerCase(cardString.substring(cardString.length()-1));
			  card = new Card(suit,rank);
		  }
		  deck.add(Optional.fromNullable(card));
	  }
	}
	
	private Card cardStringToCard(String cardString)
	{
		
		Rank rank = Rank.fromFirstLetter(cardString.substring(0,cardString.length()-1));
		Suit suit = Suit.fromFirstLetterLowerCase(cardString.substring(cardString.length()-1));
		Card card = new Card(suit,rank);
		return card;
	}
	
	//List<Card> handR = fiveCardStudLogic.cardIndexesToCard((List<Integer>)bettingR.get(HAND),deck);
	List<Card> flush = Lists.newArrayList();
	{
		flush.add(cardStringToCard("7h"));
		flush.add(cardStringToCard("6h"));
		flush.add(cardStringToCard("5h"));
		flush.add(cardStringToCard("4h"));
		flush.add(cardStringToCard("3h"));
	}
	
	List<Card> high = Lists.newArrayList();
	{
		high.add(cardStringToCard("2s"));
		high.add(cardStringToCard("7h"));
		high.add(cardStringToCard("9h"));
		high.add(cardStringToCard("3h"));
		high.add(cardStringToCard("Ah"));
	}
	
	List<Card> fourOfAKind = Lists.newArrayList();
	{
		fourOfAKind.add(cardStringToCard("2s"));
		fourOfAKind.add(cardStringToCard("7s"));
		fourOfAKind.add(cardStringToCard("7h"));
		fourOfAKind.add(cardStringToCard("7c"));
		fourOfAKind.add(cardStringToCard("7d"));
	}
	
	List<Card> fullhouse = Lists.newArrayList();
	{
		fullhouse.add(cardStringToCard("6h"));
		fullhouse.add(cardStringToCard("5s"));
		fullhouse.add(cardStringToCard("5h"));
		fullhouse.add(cardStringToCard("10c"));
		fullhouse.add(cardStringToCard("5d"));
	}
	
	List<Card> twopairs = Lists.newArrayList();
	{
		twopairs.add(cardStringToCard("5h"));
		twopairs.add(cardStringToCard("5s"));
		twopairs.add(cardStringToCard("7h"));
		twopairs.add(cardStringToCard("7c"));
		twopairs.add(cardStringToCard("9d"));
	}
	
	List<Card> pair = Lists.newArrayList();
	{
		pair.add(cardStringToCard("3h"));
		pair.add(cardStringToCard("5s"));
		pair.add(cardStringToCard("7h"));
		pair.add(cardStringToCard("7c"));
		pair.add(cardStringToCard("9d"));
	}
 /*
	@Test
	public void logCheckType()
	{
		Map<String,Object> type = fiveCardStudLogic.checkType(fullhouse);
		System.out.println("Type is " + type.get("type"));
		//System.out.println("keycard is " + type.get("keycard"));
	}  */
	

	@Test
	public void logOutput(){
		
		List<Integer> handW = (List<Integer>)bettingW.get(HAND);
		String wString = "Hand W ";
		for(Integer i : handW)
		{
			wString += endGameState.get(C+i);
		}
		System.out.println(wString); 
		
		List<Integer> handB = (List<Integer>)bettingB.get(HAND);
		String bString = "Hand B ";
		for(Integer i : handB)
		{
			bString += endGameState.get(C+i);
		}
		System.out.println(bString);
		
		List<Integer> handG = (List<Integer>)bettingG.get(HAND);
		String gString = "Hand G ";
		for(Integer i : handG)
		{
			gString += endGameState.get(C+i);
		}
		System.out.println(gString); 
		
		List<Integer> handR = (List<Integer>)bettingR.get(HAND);
		String rString = "Hand R ";
		for(Integer i : handR)
		{
			rString += endGameState.get(C+i);
		}
		System.out.println(rString); 
		
		List<Integer> handY = (List<Integer>)bettingY.get(HAND);
		String yString = "Hand Y ";
		for(Integer i : handY)
		{
			yString += endGameState.get(C+i);
		}
		System.out.println(yString); 
		
		System.out.println(fiveCardStudLogic.getExpectedOperations(endGameState,lastMove,playerIds,bId,gId));
	}
}

