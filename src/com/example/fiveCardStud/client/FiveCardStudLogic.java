package com.example.fiveCardStud.client;

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


import com.example.fiveCardStud.client.Card.Rank;
import com.example.fiveCardStud.client.Card.Suit;
import com.example.fiveCardStud.client.GameApi.Delete;
import com.example.fiveCardStud.client.GameApi.EndGame;
import com.example.fiveCardStud.client.GameApi.Operation;
import com.example.fiveCardStud.client.GameApi.Set;
import com.example.fiveCardStud.client.GameApi.SetTurn;
import com.example.fiveCardStud.client.GameApi.SetVisibility;
import com.example.fiveCardStud.client.GameApi.Shuffle;
import com.example.fiveCardStud.client.GameApi.VerifyMove;
import com.example.fiveCardStud.client.GameApi.VerifyMoveDone;
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
	private static final int DEALERID = 44; // default setting of the game
	
  public VerifyMoveDone verify(VerifyMove verifyMove) {
	try{
		checkMoveIsLegal(verifyMove);
		return new VerifyMoveDone();
	}catch(Exception e){
		return new VerifyMoveDone(verifyMove.getLastMovePlayerId(),e.getMessage());
	}
  }
  
  void checkMoveIsLegal(VerifyMove verifyMove)
  {
	  List<Operation> lastMove = verifyMove.getLastMove();
	  Map<String, Object> lastState = verifyMove.getLastState();
	//  System.out.println(lastState.get('W'));
	  List<Operation> expectedOperations = getExpectedOperations(
		        lastState, lastMove, verifyMove.getPlayerIds(),verifyMove.getLastMovePlayerId(),DEALERID);
	  System.out.println("Expected Operation");
	  System.out.println(expectedOperations);
	  System.out.println("Last move");
	  System.out.println(lastMove);
	  check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
  }
  
  List<Operation> putBigAnteMove(FiveCardStudState state,int anteAmt,List<Integer> playerIds,int lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  int big = state.bigAnte;
	  System.out.println("anteAmt " + anteAmt);
	  System.out.println("big " + big);
	  check(anteAmt == big);
	  check(state.stage == PUTSMALLANTE); // the state before PUTSMALLANTE must be ASSIGNANTE
	  check(lastMovePlayerId == state.thisTurn);
	  String playerString = Color.values()[playerIds.indexOf(state.thisTurn)].toString();
	  Map<String,Object> player = (Map)state.players.get(playerString);
	  int money =(Integer)player.get(MONEY);  // current amount of money that the player have
	  check(anteAmt <= money);
	  operation.add(new Set(NEXTTURN,state.nextTurn));
	  operation.add(new Set(THISTURN,state.thisTurn));
	  int newAmt = money - anteAmt;
	  Map<String,Object> playerMove = ImmutableMap.<String,Object>builder()
			  .put(BET,anteAmt)
			  .put(MONEY,newAmt).build();
	  operation.add(new Set(playerString,playerMove));
	  return operation;
  }
  
  List<Operation> putSmallAnteMove(FiveCardStudState state,int anteAmt,List<Integer> playerIds,int lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  int small = state.minimum;
	  
	  check(anteAmt == small);
	  check(state.stage == ASSIGNANTE);
	  check(lastMovePlayerId == state.thisTurn);
	  String playerString = Color.values()[playerIds.indexOf(state.nextTurn)].toString();
	  Map<String,Object> player = (Map)state.players.get(playerString);
	  int money =(Integer)player.get(MONEY);  // current amount of money that the player have
	  check(anteAmt <= money);
	  int thisIndex = state.small;
	  int nextIndex = state.big;
	  operation.add(new Set(NEXTTURN,nextIndex));
	  operation.add(new Set(THISTURN,thisIndex));
	  operation.add(new Set(STAGE,PUTSMALLANTE));
	  int newAmt = money - anteAmt;
	  Map<String,Object> playerMove = ImmutableMap.<String,Object>builder()
			  .put(BET,anteAmt)
			  .put(MONEY,newAmt).build();
	  operation.add(new Set(playerString,playerMove));
	  return operation;
  }
  
  List<Operation> betMove(FiveCardStudState state,int betAmt, List<Integer> playerIds, int lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  int min = state.minimum;
	  check(betAmt >= min);
	  System.out.println("lastMovePlayerId " + lastMovePlayerId + " nextTurn " + state.nextTurn);
	  check(lastMovePlayerId == state.thisTurn);
	  String playerString = Color.values()[playerIds.indexOf(state.nextTurn)].toString();
	  int money =(Integer)((Map)state.players.get(playerString)).get(MONEY);  // current amount of money that the player have
	  check(betAmt <= money);
	  if (state.stage == BET){
		  int nextTurnId = (state.thisTurn + 1) % playerIds.size();
		  int thisTurnId = state.nextTurn;
		  operation.add(new Set(NEXTTURN,nextTurnId));
		  operation.add(new Set(THISTURN,thisTurnId));
	  }else if(state.stage == DEAL){
		  int nextTurnId = (state.thisTurn + 2) % playerIds.size() + 1; // left of the big blind player id start from 1
		  int thisTurnId = (state.thisTurn + 1) % playerIds.size() + 1;
		  operation.add(new Set(NEXTTURN,nextTurnId));
		  operation.add(new Set(THISTURN,thisTurnId));
	  }

	  if(state.nextTurn == state.big){
	  	operation.add(new Set(STAGE,BETEND));
	  }else{
	  	operation.add(new Set(STAGE,BET));
	  }
	  
	  operation.add(new Set(MINIMUM,betAmt));
	  int newAmt = money - betAmt;
	  Map<String,Object> player = (Map)state.players.get(playerString);
	  List<Integer> cards = (List<Integer>)player.get(HAND);
	  Map<String,Object> playerMove;
	  if(cards.size()>0){
		  playerMove = ImmutableMap.<String,Object>builder()
				  .put(BET,betAmt)
				  .put(MONEY,newAmt)
				  .put(HAND,cards)
				  .put(DOWNCARD,cards.get(0)) // first card is facing down
				  .build();
		}else{
		  playerMove = ImmutableMap.<String,Object>builder()
				  .put(BET,betAmt)
				  .put(MONEY,newAmt)
				  .put(HAND,cards)
				  .build();
		}
	  operation.add(new Set(playerString,playerMove));
	  return operation;
  }
  
  List<Operation> dealMove(FiveCardStudState state,int dealerId,List<Integer> playerIds,int lastMovePlayerId)  // deal only one card
  {
	  List<Operation> operation = Lists.newArrayList();
	  operation.add(new Set(NEXTTURN,dealerId));
	  operation.add(new Set(THISTURN,dealerId));
	  operation.add(new Set(STAGE,DEAL));
	  System.out.println("dealerId " + dealerId);
	  System.out.println("Next turn " + state.nextTurn);
	  check(dealerId == state.thisTurn);
	  check(dealerId == state.dealer);
	  int currentCardMax = playerIds.size() * 5;
	  int topCardIndex = state.currentCard;
	  check(topCardIndex <= currentCardMax);
	  for(int i=0;i<playerIds.size();i++){
		  String playerString = playerIndexToString(playerIds.get(i),playerIds);
		  Map<String,Object> playerState = (Map)state.players.get(playerString);
		  List<Integer> cards = (List<Integer>)playerState.get(HAND);
		  if(cards == null)
		  {
			  System.out.println("card is null");
		  }
		  
		  topCardIndex ++;
		  cards.add(topCardIndex); // deal one card
		  playerState.put(HAND, cards);
		  operation.add(new Set(playerString,playerState));
	  }
	  topCardIndex ++;
	  operation.add(new Set(CURRENTCARD,topCardIndex));
	  return operation;
  }
  
  List<Operation> initDealMove(FiveCardStudState state, int dealerId, List<Integer> playerIds, int lastMovePlayerId)
  {
	  List<Operation> operation = Lists.newArrayList();
	  operation.add(new Set(NEXTTURN,dealerId));
	  operation.add(new Set(THISTURN,dealerId));
	  operation.add(new Set(STAGE,INITDEAL));
	  check(dealerId == state.thisTurn);
	  check(dealerId == state.dealer);
	  int currentCardMax = playerIds.size() * 5;
	  int topCardIndex = state.currentCard;
	  check(topCardIndex < currentCardMax);
	  for(int i=0;i<playerIds.size();i++){
		  String playerString = playerIndexToString(playerIds.get(i), playerIds);
		  Map<String,Object> playerState = (Map)state.players.get(playerString);
		  List<Integer> cards = (List<Integer>)playerState.get(HAND);
		  topCardIndex ++;
		  cards.add(topCardIndex); // deal one card
		  playerState.put(DOWNCARD, topCardIndex);
		  topCardIndex ++;
		  cards.add(topCardIndex); // deal another card
		  playerState.put(HAND, cards);
		  operation.add(new Set(playerString,playerState));
	  }
	  topCardIndex ++;
	  operation.add(new Set(CURRENTCARD,topCardIndex));
	  return operation;
  }
  
  List<Operation> endGame(FiveCardStudState state,List<Integer> playerIds)
  {
	List<Operation> operation = Lists.newArrayList();
	operation.add(new Set(NEXTTURN,DEALERID));
	operation.add(new Set(THISTURN,DEALERID));
	operation.add(new Set(STAGE,ENDGAME));
	
	check(state.stage == BET);
	List<Optional<Card>> cards = state.cards;
	Iterator it = state.players.entrySet().iterator();
	Map.Entry<String,Object> playerTop = (Map.Entry<String, Object>)it.next();
	Map<String,Object> newPlayerTopState = (Map<String,Object>)playerTop.getValue();
	newPlayerTopState.put(DOWNCARD,-1);
	operation.add(new Set(playerTop.getKey(),newPlayerTopState));
	List<Map.Entry<String,Object>> winningPlayers = Lists.newArrayList();
	winningPlayers.add(playerTop);
	while(it.hasNext())
	{
		Map.Entry<String,Object> nextPlayer = (Map.Entry<String, Object>)it.next();
		int res = compareTwoPairs(playerTop,nextPlayer,cards);
		if(res == 0){
			winningPlayers.add(playerTop);
			winningPlayers.add(nextPlayer);
		}else if(res > 0){
			winningPlayers = Lists.newArrayList();
			winningPlayers.add(nextPlayer);
			playerTop = nextPlayer;
		}
		// changing the downcard to -1 i.e. making it visible to all;
		String nextPlayerString = nextPlayer.getKey();
		Map<String,Object> newPlayerState = (Map<String,Object>)nextPlayer.getValue();
		newPlayerState.put(DOWNCARD,-1);
		operation.add(new Set(nextPlayerString,newPlayerState));
		it.remove();
	}
	
	// see how many players are there
	if(winningPlayers.size() == 1){
		String winnerString = winningPlayers.get(0).getKey();
		operation.add(new EndGame(stringToPlayerId(winnerString,playerIds)));
	}else{  //multiple winners for this round
		Map<Integer,Integer> winnerMap = new HashMap<Integer,Integer>();
		for(int i=0;i<winningPlayers.size();i++)
		{
			String winnerString = winningPlayers.get(i).getKey();
			int winnerId = stringToPlayerId(winnerString,playerIds);
			winnerMap.put(winnerId,1);
		}
		operation.add(new EndGame(winnerMap));
	}
	return operation;
  }
  
  int compareTwoPairs(Map.Entry<String,Object> pairs1,Map.Entry<String,Object> pairs2,List<Optional<Card>> cards){
	  String playerString1 = pairs1.getKey();
	  Map<String,Object> player = (Map)pairs1.getValue();
	  List<Card> playerCards1 = cardIndexesToCard((List<Integer>)player.get(HAND),cards);
	  String playerString2 = pairs2.getKey();
	  List<Card> playerCards2 = cardIndexesToCard((List<Integer>)((Map)pairs2.getValue()).get(HAND),cards);
	  return compareTwoHand(playerCards1,playerCards2);
  }
  
  List<Card> cardIndexesToCard(List<Integer> cardIndexes,List<Optional<Card>> cards)
  {
	  System.out.println(cards);
	  List<Card> cardHand = Lists.newArrayList();
	  for(Integer cardIndex :cardIndexes){
		   cardHand.add(cards.get(cardIndex).get());
	  }
	  return cardHand;
  }
  
  int compareTwoHand(List<Card> cardHand1,List<Card> cardHand2)
  {
	  Map<String, Object> type1 = checkType(cardHand1);
	  Map<String, Object> type2 = checkType(cardHand2);
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
			  Card topCard1 = (Card)type1.get("keyCard");
			  Card topCard2 = (Card)type2.get("keyCard");
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
  
  Map<String,Object> checkType(List<Card> cardHand)
  {
	  Map<String,Object> type = new HashMap<String, Object>();
	  boolean sameSuit = true;
	  boolean straight = true;
	  int[] rankCountArr = new int[13];
	  List<Integer> rankList = new ArrayList<Integer>();
	  // check if hand is a flush
	  for(int i = 0;i < cardHand.size() - 1;i++){
		  rankList.add(cardHand.get(i).getRank().ordinal());
		  if(cardHand.get(i).getSuit() != cardHand.get(i).getSuit())
		  {
			  sameSuit = false;
		  }
	  }
	  
	  Collections.sort(cardHand,new Comparator<Card>(){
		  public int compare(Card c1, Card c2){
			  return c1.getRank().ordinal() - c2.getRank().ordinal();
		  }
	  });
	  
	  int firstIndex = cardHand.get(0).getRank().ordinal();
	  rankCountArr[firstIndex] ++;
	  int maxCount = 1;
	  int maxIndex = firstIndex;
	  int secondMaxCount = 0;
	  int secondMaxIndex = 0;
	  for(int i=1;i< rankList.size();i++)
	  {
		  int cur = cardHand.get(i).getRank().ordinal();
		  rankCountArr[cur] ++;
		  
		  if(rankCountArr[cur] >= maxCount)
		  {
			secondMaxCount = maxCount;
			secondMaxIndex = maxIndex;
			maxCount = rankCountArr[cur];
			maxIndex = cur;
		  }
		  
		  int prev = cardHand.get(i-1).getRank().ordinal();
		  if (cur != ((prev + 1) % rankList.size())){
			  straight = false;
		  }
	  }
	  
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
			  type.put("topcard",cardHand.get(cardHand.size()-1));
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
  List<Operation> getExpectedOperations(
	      Map<String, Object> lastApiState, List<Operation> lastMove, List<Integer> playerIds,int lastMovePlayerId,int dealerId) {
	  if(lastApiState.isEmpty()){
		  System.out.println("Initial Move logic");
		  check(lastMovePlayerId == dealerId);
		  return getInitialMove(playerIds,dealerId);
	  }
	  
	  FiveCardStudState lastState = gameApiToFiveCardStudState(lastApiState,Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);

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
		}else if(lastMove.contains(new Set(STAGE,BET)))
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
  FiveCardStudState gameApiToFiveCardStudState(Map<String, Object> lastApiState,Color turnOfColor,List<Integer> playerIds)
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
			  					(Integer)lastApiState.get(NEXTTURN),
			  					(Integer)lastApiState.get(THISTURN),
			  					(String)lastApiState.get(STAGE),
			  					(Integer)lastApiState.get(DEALER),
			  					(Integer)lastApiState.get(BIG),
			  					(Integer)lastApiState.get(SMALL),
			  					(Integer)lastApiState.get(BIGANTE),
			  					(Integer)lastApiState.get(SMALLANTE),
			  					(Integer)lastApiState.get(CURRENTCARD),
			  					(Integer)lastApiState.get(MINIMUM),
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
  
  String playerIndexToString(int playerIndex,List<Integer> playerIds)
  {
	  return Color.values()[playerIds.indexOf(playerIndex)].toString();
  }
  
  List<Operation> getInitialMove(List<Integer> playerIds,int dealerId)
  {
	  // playerId 3 is the dealer in this game
	  int numPlayers = playerIds.size();
	  int smallId = (dealerId + 1 - 42)%numPlayers + 42;
	  int bigId = (dealerId + numPlayers -1) % numPlayers;
	  List<Operation> operations = Lists.newArrayList();
	  // the player to the right of the dealer is small blind
	  operations.add(new Set(NEXTTURN,smallId));
	  operations.add(new Set(THISTURN,dealerId));
	  operations.add(new Set(DEALER,dealerId));
	  operations.add(new Set(SMALL,smallId));
	  operations.add(new Set(BIG,bigId));
	  operations.add(new Set(BIGANTE,50));
	  operations.add(new Set(SMALLANTE,25));
	  operations.add(new Set(STAGE,ASSIGNANTE));
	  operations.add(new Set(CURRENTCARD,0));
	  operations.add(new Set(MINIMUM,25));

	  Map<String,Object> mapW = new HashMap<>();
	  mapW.put(MONEY,10000);
	  mapW.put(HAND,new Lists.newArrayList());

	  Map<String,Object> mapB = new HashMap<>();
	  mapB.put(MONEY,10000);
	  mapB.put(HAND,new Lists.newArrayList());
	  Map<String,Object> mapG = new HashMap<>();
	  mapG.put(MONEY,10000);
	  mapG.put(HAND,new Lists.newArrayList());
	  Map<String,Object> mapR = new HashMap<>();
	  mapR.put(MONEY,10000);
	  mapR.put(HAND,new Lists.newArrayList());
	  Map<String,Object> mapY = new HashMap<>();
	  mapY.put(MONEY,10000);
	  mapY.put(HAND,new Lists.newArrayList());

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
  
  
  List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
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

	  String cardIdToString(int cardId) {
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
