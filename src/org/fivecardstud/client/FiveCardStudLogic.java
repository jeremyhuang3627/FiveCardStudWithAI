package org.fivecardstud.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;


import org.fivecardstud.client.Card.Rank;
import org.fivecardstud.client.Card.Suit;
import org.game_api.GameApi.Delete;
import org.game_api.GameApi.EndGame;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.Shuffle;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


// in my version of the game no one is allowed to fold :D

public class FiveCardStudLogic {
	private static final String THISTURN = "thisturn";
	private static final String NEXTTURN = "nextturn";
	private static final String W = "W"; // White hand
	private static final String B = "B"; // Black hand
	private static final String G = "G"; // Grey hand   in this version grey is always the dealer
	private static final String R = "R"; // Red hand
	private static final String Y = "Y"; // Yellow hand
	private static final String C = "C"; // Card key (C0...C51)
	private static final String MONEY = "money";
	private static final String DEALER = "dealer";
	public static final String HAND = "hand";
	private static final String BIG = "bigblind";   // and in this version big blind and small blind is always the same person
	private static final String SMALL = "smallblind";
	private static final String BIGANTE = "bigante";
	private static final String SMALLANTE = "smallante";
	private static final String CURRENTCARD = "currentcard";  //the next card on the deck that will be dealt
	private static final String MINIMUM = "minimum";  //the minimum amoutn to call
	private static final String POT = "pot";
	private static final String DOWNCARD = "downcard";
	private static final String ENDGAME = "endgame";
	/*  constant for stage  */
	private static final String STAGE = "stage";
	private static final String DEAL = "deal";
	private static final String BET = "bet";
	private static final String BETEND = "betend";
	private static final String PUTBIGANTE = "putbigante";
	private static final String PUTSMALLANTE = "putsmallante";
	private static final String ASSIGNANTE = "assignante";
	private static final String INITDEAL = "initdeal";
	private static final String WINNER = "winner";
	private static final String DEALERID = "44"; // default setting of the game
	
  public VerifyMoveDone verify(VerifyMove verifyMove) {
	try{
		checkMoveIsLegal(verifyMove);
		return new VerifyMoveDone();
	}catch(Exception e){
		return new VerifyMoveDone(verifyMove.getLastMovePlayerId(),e.getMessage());
	}
  }
  
  public void checkMoveIsLegal(VerifyMove verifyMove)
  {
	  List<Operation> lastMove = verifyMove.getLastMove();
	  System.out.println("Last move");
	  System.out.println(lastMove);
	  Map<String, Object> lastState = verifyMove.getLastState();
	//  System.out.println(lastState.get('W'));
	  List<Operation> expectedOperations = getExpectedOperations(
		        lastState, lastMove, verifyMove.getPlayerIds(),verifyMove.getLastMovePlayerId(),"" + DEALERID);
	  System.out.println("Expected Operation");
	  System.out.println(expectedOperations);
	  System.out.println("Last move");
	  System.out.println(lastMove);
	  check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
  }
  
  List<Operation> putBigAnteMove(FiveCardStudState state,int anteAmt,List<String> playerIds,String lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  int big = state.bigAnte;
	  check(anteAmt == big);
	  check(state.stage == PUTSMALLANTE); // the state before PUTSMALLANTE must be ASSIGNANTE
	  String playerString = Color.values()[playerIds.indexOf(state.nextTurn)].toString();
	  Map<String,Object> player = (Map)state.players.get(playerString);
	  int money =(Integer)player.get(MONEY);  // current amount of money that the player have
	  check(anteAmt <= money);
	  operation.add(new Set(NEXTTURN,""+DEALERID));
	  operation.add(new Set(THISTURN,state.big));
	  operation.add(new Set(STAGE,PUTBIGANTE));
	  int newAmt = money - anteAmt;
	  Map<String,Object> playerMove = new HashMap<>();
	  System.out.println("putBigAnte bet " + anteAmt);
	  playerMove.put(BET,anteAmt);
	  playerMove.put(MONEY,newAmt);
	  playerMove.put(HAND,Lists.newArrayList());
	  operation.add(new Set(playerString,playerMove));
	  return operation;
  }
  
  List<Operation> putSmallAnteMove(FiveCardStudState state,int anteAmt,List<String> playerIds,String lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  int min = state.minimum;
	  check(anteAmt == min);
	  check(state.stage == ASSIGNANTE);
	  String playerString = Color.values()[playerIds.indexOf(state.nextTurn)].toString();
	  Map<String,Object> player = (Map)state.players.get(playerString);
	  int money =(Integer)player.get(MONEY);  // current amount of money that the player have
	  check(anteAmt <= money);
	  String thisIndex = state.small;
	  String nextIndex = state.big;
	  operation.add(new Set(NEXTTURN,nextIndex));
	  operation.add(new Set(THISTURN,thisIndex));
	  operation.add(new Set(STAGE,PUTSMALLANTE));
	  int newAmt = money - anteAmt;
	  Map<String,Object> playerMove = new HashMap<>();
	  System.out.println("putSmallAnte bet " + anteAmt);
	  playerMove.put(BET,anteAmt);
	  playerMove.put(MONEY,newAmt);
	  playerMove.put(HAND,Lists.newArrayList());
	  operation.add(new Set(playerString,playerMove));
	  return operation;
  }
  
  List<Operation> betMove(FiveCardStudState state,int betAmt, List<String> playerIds, String lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  int min = state.minimum;
	  String playerString = Color.values()[playerIds.indexOf(state.nextTurn)].toString();
	  int money =(Integer)((Map)state.players.get(playerString)).get(MONEY);  // current amount of money that the player have
	  int nextTurnId = 0;
	  int thisTurnId = 0;
	  if (state.stage == BET){
		  nextTurnId = (Integer.parseInt(state.nextTurn) - 42 -1 +5 ) % playerIds.size() + 42;
		  thisTurnId = Integer.parseInt(state.nextTurn);
	  }else if(state.stage == DEAL || state.stage == INITDEAL){
		  nextTurnId = (Integer.parseInt(state.nextTurn) - 42 - 1 +5) % playerIds.size() + 42; // left of the big blind player id start from 1
		  thisTurnId = Integer.parseInt(state.nextTurn);
	  }

	  if(thisTurnId == Integer.parseInt(state.big)){
		  nextTurnId = Integer.parseInt(DEALERID);
		  operation.add(new Set(STAGE,BETEND));
	  }else{
		  operation.add(new Set(STAGE,BET));
	  }
	  
	  operation.add(new Set(NEXTTURN,"" + nextTurnId));
	  operation.add(new Set(THISTURN,"" + thisTurnId));

	  int newAmt = money - betAmt;
	  Map<String,Object> player = (Map)state.players.get(playerString);
	  List<Integer> cards = (List<Integer>)player.get(HAND);
	  Map<String,Object> playerMove;
	  if(cards.size()>0){
		  playerMove = new HashMap<>();
		  playerMove.put(BET,betAmt);
		  playerMove.put(MONEY,newAmt);
		  playerMove.put(HAND,cards);
		  playerMove.put(DOWNCARD,cards.get(0)); // first card is facing down
		}else{
		  playerMove = new HashMap<>();
		  playerMove.put(BET,betAmt);
		  playerMove.put(MONEY,newAmt);
		  playerMove.put(HAND,cards);
		}
	  operation.add(new Set(playerString,playerMove));
	  return operation;
  }
  
  List<Operation> dealMove(FiveCardStudState state,String dealerId,List<String> playerIds,String lastMovePlayerId)  // deal only one card
  {
	  List<Operation> operation = Lists.newArrayList();
	  operation.add(new Set(NEXTTURN,"" + (Integer.parseInt(dealerId)-2)));
	  operation.add(new Set(THISTURN,"" + dealerId));
	  operation.add(new Set(STAGE,DEAL));
	  
	  check(dealerId.equals(state.dealer));
	  int currentCardMax = playerIds.size() * 5;
	  int topCardIndex = state.currentCard;
	  check(topCardIndex <= currentCardMax);
	  int betTotal = 0;
	  for(int i=0;i<playerIds.size();i++){
		  String playerString = playerIndexToString(playerIds.get(i),playerIds);
		  Map<String,Object> playerState = (Map)state.players.get(playerString);
		  System.out.println(playerString + " bet " + (Integer)playerState.get(BET));
		  System.out.println("betTotal is " + betTotal);
		  betTotal += (Integer)playerState.get(BET);
		  List<Integer> cards = (List<Integer>)playerState.get(HAND);
		  if(cards == null)
		  {
			  System.out.println("card is null");
		  }
		  topCardIndex ++;
		  if (!cards.contains(topCardIndex))
		  {
			  cards.add(topCardIndex);// deal one card
		  }
		  playerState.put(HAND, cards);
		  operation.add(new Set(playerString,playerState));
	  }
	  topCardIndex ++;
	  int pot = state.pot;
	  pot += betTotal;
	  operation.add(new Set(POT,pot));
	  operation.add(new Set(CURRENTCARD,topCardIndex));
	  return operation;
  }
  
  List<Operation> initDealMove(FiveCardStudState state, String dealerId, List<String> playerIds, String lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  operation.add(new Set(NEXTTURN,"" + (Integer.parseInt(dealerId)-2)));
	  operation.add(new Set(THISTURN,dealerId));
	  operation.add(new Set(STAGE,INITDEAL));
//	  check(dealerId == state.thisTurn);
	  check(dealerId.equals(state.dealer));
	  int currentCardMax = playerIds.size() * 5;
	  int topCardIndex = state.currentCard;
	  check(topCardIndex < currentCardMax);
	  int betTotal = 0;
	  for(int i=0;i<playerIds.size();i++){
		  String playerString = playerIndexToString(playerIds.get(i), playerIds);
		  Map<String,Object> playerState = (Map)state.players.get(playerString);
		  betTotal += (Integer)playerState.get(BET);
		  System.out.println("initDealMove playerString  " + playerString + " Bet " + (Integer)playerState.get(BET) + " betTotal " + betTotal );
		  List<Integer> cards = Lists.newArrayList();
		  topCardIndex ++;
		  cards.add(topCardIndex); // deal one card
		  playerState.put(DOWNCARD, topCardIndex);
		  topCardIndex ++;
		  cards.add(topCardIndex); // deal another card
		  playerState.put(HAND, cards);
		  operation.add(new Set(playerString,playerState));
	  }
	  topCardIndex ++;
	  int pot = state.pot;
	  System.out.println("initDealMove pot " + pot + " betTotal " + betTotal);
	  pot += betTotal;
	  operation.add(new Set(POT,pot));
	  operation.add(new Set(CURRENTCARD,topCardIndex));
	  return operation;
  }
  
  List<Operation> endGame(FiveCardStudState state,List<String> playerIds)
  {
	List<Operation> operation = Lists.newArrayList();
	operation.add(new Set(NEXTTURN,DEALERID));
	operation.add(new Set(THISTURN,DEALERID));
	operation.add(new Set(STAGE,ENDGAME));
	check(state.stage == BETEND);
	List<Optional<Card>> cards = state.cards;
	
	Iterator it = state.players.entrySet().iterator();
	Map.Entry<String,Object> topPlayer = (Map.Entry<String, Object>)it.next();
	it.remove();
	while(it.hasNext())
	{
		Map.Entry<String,Object> nextPlayer = (Map.Entry<String, Object>)it.next();
		int res = compareTwoPlayer(topPlayer,nextPlayer,cards);
		if ((res < 0)){
			topPlayer = nextPlayer;
		}
		it.remove();
	}
	
	String winnerString = topPlayer.getKey();
	operation.add(new Set(WINNER,winnerString));
	operation.add(new EndGame((Map)topPlayer.getValue()));
	return operation;
  }
  
  int compareTwoPlayer(Map.Entry<String,Object> pairs1,Map.Entry<String,Object> pairs2,List<Optional<Card>> cards){
	  System.out.println("Comparing " + pairs1.getKey() + " and " + pairs2.getKey());
	  String playerString1 = pairs1.getKey();
	  Map<String,Object> player = (Map)pairs1.getValue();
	  List<Card> playerCards1 = cardIndexesToCard((List<Integer>)player.get(HAND),cards);
	  String playerString2 = pairs2.getKey();
	  List<Card> playerCards2 = cardIndexesToCard((List<Integer>)((Map)pairs2.getValue()).get(HAND),cards);
	  return compareTwoHand(playerCards1,playerCards2);
  }
  
  public List<Card> cardIndexesToCard(List<Integer> cardIndexes,List<Optional<Card>> cards)
  {
	  //System.out.println(cards);
	  List<Card> cardHand = Lists.newArrayList();
	  for(Integer cardIndex :cardIndexes){
		   cardHand.add(cards.get(cardIndex).get());
	  }
	  return cardHand;
  }
  
  int compareTwoHand(List<Card> cardHand1,List<Card> cardHand2)
  {
	  Map<String, Object> type1 = checkType(cardHand1);
	  System.out.println("type1 is " + type1.get("type"));
	  Map<String, Object> type2 = checkType(cardHand2);
	  System.out.println("type2 is " + type2.get("type"));
	  
	  int res = 0; //default is zero i.e. two hands are the same rank
	  if(type1.get("type")== type2.get("type"))
	  {
		  Card keyCard1;
		  Card keyCard2;
		  Comparator<Card> cardComparator =  new Comparator<Card>(){
			  public int compare(Card c1,Card c2)
			  {
				  return c2.getRank().ordinal() - c1.getRank().ordinal();  //descending order
			  }
		  };
		  
		  switch((String)type1.get("type")){
		  case "royalflush":
			  keyCard1 = (Card)type1.get("keycard");
			  keyCard2 = (Card)type2.get("keycard");
			  res = keyCard1.getRank().ordinal() - keyCard2.getRank().ordinal();
			  break;
		  case "straight":
			  keyCard1 = (Card)type1.get("keycard");
			  keyCard2 = (Card)type2.get("keycard");
			  res = keyCard1.getRank().ordinal() - keyCard2.getRank().ordinal();
			  break;
		  case "flush":
			  // sort the two card by rank first
			  Collections.sort(cardHand1,cardComparator);
			  Collections.sort(cardHand2,cardComparator);
			  for(int i=0;i<cardHand1.size();i++)
			  {
				int rank1 = cardHand1.get(i).getRank().ordinal();
				int rank2 = cardHand2.get(i).getRank().ordinal();
				if(rank1 != rank2){
					res = rank1 - rank2;
					break;
				}
			  }
			  break;
		  case "fullhouse":
			  keyCard1 = (Card)type1.get("keycard");
			  keyCard2 = (Card)type2.get("keycard");
			  res = keyCard1.getRank().ordinal() - keyCard2.getRank().ordinal();
			  break;
		  case "fourofakind":
			  keyCard1 = (Card)type1.get("keycard");
			  keyCard2 = (Card)type2.get("keycard");
			  res = keyCard1.getRank().ordinal() - keyCard2.getRank().ordinal();
			  break;
		  case "threeofakind":
			  keyCard1 = (Card)type1.get("keycard");
			  keyCard2 = (Card)type2.get("keycard");
			  res = keyCard1.getRank().ordinal() - keyCard2.getRank().ordinal();
			  break;
		  case "twopairs":
			  List<Card> cardList1 = Lists.newArrayList();
			  Card paironeHand1 = (Card)type1.get("pairone");
			  Card pairtwoHand1 = (Card)type1.get("pairtwo");
			  cardList1.add(paironeHand1);
			  cardList1.add(pairtwoHand1);
			  
			  List<Card> cardList2 = Lists.newArrayList();
			  Card paironeHand2 = (Card)type1.get("pairone");
			  Card pairtwoHand2 = (Card)type1.get("pairtwo");
			  cardList2.add(paironeHand2);
			  cardList2.add(pairtwoHand2);
			  
			  Collections.sort(cardList1,cardComparator);
			  Collections.sort(cardList2,cardComparator);
			  
			  for(int i=0;i<cardHand1.size();i++)
			  {
				  int rank1 = cardList1.get(i).getRank().ordinal();
				  int rank2 = cardList2.get(i).getRank().ordinal();
				  if(rank1 != rank2)
				  {
					  res = rank1 - rank2;
					  break;
				  }
			  }
			  break;
		  case "pair":
			  Card pairHand1 = (Card)type1.get("pair");
			  Card pairHand2 = (Card)type2.get("pair");
			  res = pairHand1.getRank().ordinal() - pairHand2.getRank().ordinal();
			  break;
		  default: // high card
			  Card topCard1 = (Card)type1.get("keycard");
			  Card topCard2 = (Card)type2.get("keycard");
			  res = topCard1.getRank().ordinal() - topCard2.getRank().ordinal();
			  break;
		  }
	  }else{
		  List<String> typeRank = Lists.newArrayList();
		  typeRank.add("highcard");
		  typeRank.add("pair");
		  typeRank.add("twopair");
		  typeRank.add("threeofakind");
		  typeRank.add("straight");
		  typeRank.add("flush");
		  typeRank.add("fullhouse");
		  typeRank.add("fourofakind");
		  typeRank.add("royalflush");
		  
		  int index1 = typeRank.indexOf(type1.get("type"));
		  int index2 = typeRank.indexOf(type2.get("type"));
		  res = index1 - index2;
	  }
	  return res;
  }
  
  public Map<String,Object> checkType(List<Card> cardHand)
  {
	  Map<String,Object> type = new HashMap<String, Object>();
	  boolean sameSuit = true;
	  boolean straight = true;
	  int[] rankCountArr = new int[13];
	  List<Integer> rankList = new ArrayList<Integer>();
	  // check if hand is a flush
	  
	  Collections.sort(cardHand,new Comparator<Card>(){
		  public int compare(Card c1, Card c2){
			  return c1.getRank().ordinal() - c2.getRank().ordinal();
		  }
	  });
	  	  
/*	  System.out.println("after sort");
	  for(Card c : cardHand)
	  {
		  System.out.println(c);
	  }  */
	  
	  for(int i = 0;i < cardHand.size();i++){
		  rankList.add(cardHand.get(i).getRank().ordinal());
		  if(i < cardHand.size() -1 && cardHand.get(i).getSuit() != cardHand.get(i+1).getSuit())
		  {
			  System.out.println("not same suit");
			  sameSuit = false;
		  }
	  }
	  
	  int firstIndex = cardHand.get(0).getRank().ordinal();
	  rankCountArr[firstIndex] ++;
	  int maxCount = 1;
	  int maxIndex = 0;  // the index of the card with the highest occurrence in cardHand;
	  int secondMaxCount = 0;
	  int secondMaxIndex = -1;
	  for(int i=1;i < rankList.size();i++)
	  {
		  int cur = cardHand.get(i).getRank().ordinal();
		  int prev = cardHand.get(i-1).getRank().ordinal();
		  rankCountArr[cur] ++;
		  
		  if(rankCountArr[cur] >= maxCount)
		  {
			maxCount = rankCountArr[cur];
			maxIndex = i;
		  }else if(rankCountArr[cur] > secondMaxCount){
			  secondMaxCount = rankCountArr[cur];
			  secondMaxIndex = i;
		  }

		  if (cur != (prev + 1) % 13){
			  System.out.println("not straight");
			  straight = false;
		  }
	  }
	/*  
	  System.out.println(" maxIndex " + maxIndex);
	  System.out.println(" maxCount " + maxCount);
	  System.out.println(" secondMaxIndex " + secondMaxIndex);
	  System.out.println(" secondMaxCount " + secondMaxCount);
	 // System.out.println("first top card " + cardHand.get(maxIndex).toString());
	 // System.out.println("second top card " + cardHand.get(secondMaxIndex).toString());
	  */
	  if(straight && sameSuit){
		  type.put("type","royalflush");
		  type.put("keycard",cardHand.get(cardHand.size()-1));  // you return the necessary info to compare the hand if they are the same type
	  }else if(straight){
		  type.put("type","straight");
		  type.put("keycard",cardHand.get(cardHand.size()-1));
	  }else if(sameSuit)
	  {
		  type.put("type","flush");
	  }else{
		  if(maxCount == 1){
			  type.put("type","highcard");
			  type.put("keycard",cardHand.get(cardHand.size()-1));
		  }else if(maxCount == 4){
			  type.put("type","fourofakind");
			  type.put("keycard",cardHand.get(maxIndex));
		  }else if(maxCount==3){
			  if(secondMaxCount == 2){
				  type.put("type","fullhouse");
				  type.put("keycard", cardHand.get(maxIndex));
			  }else{
				  type.put("type","threeofakind");
				  type.put("keycard", cardHand.get(maxIndex));
			  }
		  }else if(maxCount==2){
			  if (secondMaxCount == 2)
			  {
				  type.put("type","twopairs");
				  type.put("pairone",cardHand.get(maxIndex));
				  type.put("pairtwo",cardHand.get(secondMaxIndex));
			  }else{
				  type.put("type","pair");
				  type.put("pair",cardHand.get(maxIndex));
			  }
		  } 
	  }
	  return type;
  }
  
  @SuppressWarnings("unchecked")
  public List<Operation> getExpectedOperations(
	      Map<String, Object> lastApiState, List<Operation> lastMove, List<String> playerIds,String lastMovePlayerId,String dealerId) {
	  if(lastApiState.isEmpty()){
		  System.out.println("Initial Move logic");
		  System.out.println("lastMovePlayerId " + lastMovePlayerId + " dealerId " + dealerId);
		  check(lastMovePlayerId.equals(dealerId));
		  return getInitialMove(playerIds,dealerId);
	  }
	  
	  FiveCardStudState lastState = gameApiToFiveCardStudState(lastApiState,Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
	  System.out.println("getExpectedMove ");
	  System.out.println(lastApiState);
	  if(lastMove.contains(new Set(STAGE,PUTSMALLANTE)))
		{
		  System.out.println("PUTSMALLANTE logic");
		  Set last = (Set)lastMove.get(lastMove.size()-1);
		  Map<String,Object> value = (Map<String,Object>)last.getValue();
		  int anteAmt = (Integer)value.get(BET);
		  return putSmallAnteMove(lastState,anteAmt,playerIds,lastMovePlayerId);
		}else if(lastMove.contains(new Set(STAGE,PUTBIGANTE)))
		{
			System.out.println("PUTBIGANTE logic");
			int anteAmt = (Integer)((Map<String,Object>)((Set)lastMove.get(lastMove.size()-1)).getValue()).get(BET);
			return putBigAnteMove(lastState,anteAmt,playerIds, lastMovePlayerId);
		}else if(lastMove.contains(new Set(STAGE,INITDEAL)))
		{
			System.out.println("PUTINITDEAL logic");
			return initDealMove(lastState,dealerId,playerIds,lastMovePlayerId);
		}else if(lastMove.contains(new Set(STAGE,BET)) || lastMove.contains(new Set(STAGE,BETEND)))
		{
			System.out.println("BET logic");
			int betAmt = (Integer)((Map<String,Object>)((Set)lastMove.get(lastMove.size()-1)).getValue()).get(BET);
			return betMove(lastState,betAmt,playerIds,lastMovePlayerId);
		}else if(lastMove.contains(new Set(STAGE,ENDGAME)))
		{
			System.out.println("ENDGAME logic");
			return endGame(lastState,playerIds);
		}else
		{
			System.out.println("DEALMOVE logic");
			return dealMove(lastState,dealerId,playerIds,lastMovePlayerId);
		}
	}
  
  @SuppressWarnings("unchecked")
  FiveCardStudState gameApiToFiveCardStudState(Map<String, Object> lastApiState,Color turnOfColor,List<String> playerIds)
  {
	  List<Optional<Card>> cards = Lists.newArrayList();
	  
	  for(int i=0;i<52;i++)
	  {
		  String cardString = (String) lastApiState.get(C + i);
		  Card card;
		  if(cardString == null)
		  {
			  card = null;
		  }else{
			  Rank rank = Rank.fromFirstLetter(cardString.substring(0,cardString.length()-1));
			  Suit suit = Suit.fromFirstLetterLowerCase(cardString.substring(cardString.length()-1));
			  card = new Card(suit,rank);
		  }
		  cards.add(Optional.fromNullable(card));
	  }
	  
	  FiveCardStudState state = new FiveCardStudState(
			  					(String)lastApiState.get(NEXTTURN),
			  					(String)lastApiState.get(THISTURN),
			  					(String)lastApiState.get(STAGE),
			  					(String)lastApiState.get(DEALER),
			  					(String)lastApiState.get(BIG),
			  					(String)lastApiState.get(SMALL),
			  					(Integer)lastApiState.get(BIGANTE),
			  					(Integer)lastApiState.get(SMALLANTE),
			  					(Integer)lastApiState.get(CURRENTCARD),
			  					(Integer)lastApiState.get(MINIMUM),
			  					(Integer)lastApiState.get(POT),
			  					(Map<String,Object>)lastApiState.get(W),
			  					(Map<String,Object>)lastApiState.get(B),
			  					(Map<String,Object>)lastApiState.get(G),
			  					(Map<String,Object>)lastApiState.get(R),
			  					(Map<String,Object>)lastApiState.get(Y),
			  					cards
			  					);
	 return state;
  }
  
  int stringToPlayerId(String playerString,List<Integer> playerIds)
  {
	  return playerIds.get(Color.valueOf(playerString).ordinal());
  }
  
  String playerIndexToString(String playerIndex,List<String> playerIds)
  {
	  return Color.values()[playerIds.indexOf(playerIndex)].toString();
  }
  
  public List<Operation> getInitialMove(List<String> playerIds,String dealerId)
  {
	  // playerId 3 is the dealer in this game
	  int numPlayers = playerIds.size();
	  String smallId = "45";
	  String bigId = "43";
	  List<Operation> operations = Lists.newArrayList();
	  // the player to the right of the dealer is small blind
	  operations.add(new Set(NEXTTURN,"45"));
	  operations.add(new Set(THISTURN,"" + dealerId));
	  operations.add(new Set(DEALER,"" + dealerId));
	  operations.add(new Set(SMALL, smallId));
	  operations.add(new Set(BIG, bigId));
	  operations.add(new Set(BIGANTE,50));
	  operations.add(new Set(SMALLANTE,25));
	  operations.add(new Set(STAGE,ASSIGNANTE));
	  operations.add(new Set(CURRENTCARD,0));
	  operations.add(new Set(MINIMUM,25));
	  operations.add(new Set(POT,0));

	  Map<String,Object> mapW = new HashMap<>();
	  mapW.put(BET,0);
	  mapW.put(MONEY,10000);
	  mapW.put(HAND,Lists.newArrayList());

	  Map<String,Object> mapB = new HashMap<>();
	  mapB.put(BET,0);
	  mapB.put(MONEY,10000);
	  mapB.put(HAND,Lists.newArrayList());
	  
	  Map<String,Object> mapG = new HashMap<>();
	  mapG.put(BET,0);
	  mapG.put(MONEY,10000);
	  mapG.put(HAND,Lists.newArrayList());
	  
	  Map<String,Object> mapR = new HashMap<>();
	  mapR.put(BET,0);
	  mapR.put(MONEY,10000);
	  mapR.put(HAND,Lists.newArrayList());
	  
	  Map<String,Object> mapY = new HashMap<>();
	  mapY.put(BET,0);
	  mapY.put(MONEY,10000);
	  mapY.put(HAND,Lists.newArrayList());

	  operations.add(new Set(W,mapW));
	  operations.add(new Set(B,mapB));
	  operations.add(new Set(G,mapG));
	  operations.add(new Set(R,mapR));
	  operations.add(new Set(Y,mapY));

	  for (int i = 0; i < 52; i++) {
	      operations.add(new Set(C + i, cardIdToString(i)));
	    }
	  // shuffle(C0,...,C51)
	  operations.add(new Shuffle(getCardsInRange(0, 51)));
	  return operations;
  }
  
  
  public List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
	    List<Integer> keys = Lists.newArrayList();
	    for (int i = fromInclusive; i <= toInclusive; i++) {
	      keys.add(i);
	    }
	    return keys;
   }

	  List<String> getCardsInRange(int fromInclusive, int toInclusive) {
	    List<String> keys = Lists.newArrayList();
	    for (int i = fromInclusive; i <= toInclusive; i++) {
	      keys.add(C + i);
	    }
	    return keys;
	  }

	public String cardIdToString(int cardId) {
	    checkArgument(cardId >= 0 && cardId < 52);
	    int rank = (cardId / 4);
	    String rankString = Rank.values()[rank].getFirstLetter();
	    int suit = cardId % 4;
	    String suitString = Suit.values()[suit].getFirstLetterLowerCase();
	    return rankString + suitString;
	}	
	  
  private void check(boolean val, Object... debugArguments) {
	    if (!val) {
	      throw new RuntimeException("We have a hacker! debugArguments="
	          + Arrays.toString(debugArguments));
	    }
	  }
}
