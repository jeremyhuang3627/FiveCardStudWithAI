package com.example.fiveCardStud.client;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.fiveCardStud.client.Card.Rank;
import com.example.fiveCardStud.client.GameApi.Container;
import com.example.fiveCardStud.client.GameApi.Operation;
import com.example.fiveCardStud.client.GameApi.SetTurn;
import com.example.fiveCardStud.client.GameApi.UpdateUI;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class FiveCardStudPresenter {
	
	private static final String STAGE = "stage";
	private static final String DEAL = "deal";
	private static final String BET = "bet";
	private static final String BETEND = "betend";
	private static final String PUTBIGANTE = "putbigante";
	private static final String PUTSMALLANTE = "putsmallante";
	private static final String ASSIGNANTE = "assignante";
	private static final String INITDEAL = "initdeal";
	private static final String MONEY = "money";
	private static final String BIGANTE = "bigante";
	
	public interface View {
	    void setPresenter(FiveCardStudPresenter cheatPresenter);
	    
	    /** pass in one state the method should figure out how to display the views */
	    /** Sets the state for a viewer, i.e., not one of the players. */
	    void setViewerState(
	    		int nextTurn,
	    		int thisTurn,
	    		String stage,
	    		int dealer,
	    		int big,
	    		int small,
	    		int bigAnte,
	    		int smallAnte,
	    		int currentCard,
	    		int minimum,
	    		Map<String,Object> players,
	    		List<Optional<Card>> cards
	    		);

	    /**
	     * Sets the state for a player (whether the player has the turn or not).
	     * 
	     */
	    void setPlayerState(
	    		int nextTurn,
	    		int thisTurn,
	    		String stage,
	    		int dealer,
	    		int big,
	    		int small,
	    		int bigAnte,
	    		int smallAnte,
	    		int currentCard,
	    		int minimum,
	    		Map<String,Object> players,
	    		List<Optional<Card>> cards
	    		);
	    
	    /**
	     * Ask the player to put in chips. Each time a chip is put in the method calls the private
	     * bet method until betFinished is called or player's money is used up
	     * 
	     */
	    void bet(int betAmt, int playerId,int poolAmt,int nextTurn);
	}
	
	private final FiveCardStudLogic fiveCardStudLogic = new FiveCardStudLogic();
	private final View view;
	private final Container container;
	private final int dealerId = 44;
	private int myId;
	private FiveCardStudState fiveCardStudState;
	private List<Card> selectedCards;
	private int betAmt;
	private List<Integer> playerIds;
	private int lastMovePlayerId;
	private int poolAmt;
	
	public FiveCardStudPresenter(View view, Container container){
		System.out.println("Constructing presenter");
		this.view = view;
		this.container = container;
		view.setPresenter(this);
	}
	
	public void updateUI(UpdateUI updateUI){
		System.out.println("calling updateUI");
		playerIds = updateUI.getPlayerIds();
		lastMovePlayerId = updateUI.lastMovePlayerId;
	    int yourPlayerId = updateUI.getYourPlayerId();
	    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
	    Color turnOfColor = null;
	    
	    if (updateUI.getState().isEmpty()) {
	          // The G player sends the initial setup move.
	    	System.out.println("Empty state. yourPlayerId " + yourPlayerId + " dealerId " + dealerId);
	    	if(yourPlayerId == dealerId){
	    		System.out.println("Sending initial moves");
	    		sendInitialMove(playerIds);
		    }
          return;
	    }
	    
	    for (Operation operation : updateUI.getLastMove()) {
	      if (operation instanceof SetTurn) {
	        turnOfColor = Color.values()[playerIds.indexOf(((SetTurn) operation).getPlayerId())];
	      }
	    }
	    
	    fiveCardStudState = fiveCardStudLogic.gameApiToFiveCardStudState(updateUI.getState(), turnOfColor, playerIds);
	    if (updateUI.isViewer()) {
	        view.setViewerState(fiveCardStudState.nextTurn,
	        						fiveCardStudState.thisTurn,
	        						fiveCardStudState.stage,
	        						fiveCardStudState.dealer,
	        						fiveCardStudState.big,
	        						fiveCardStudState.small,
	        						fiveCardStudState.bigAnte,
	        						fiveCardStudState.smallAnte,
	        						fiveCardStudState.currentCard,
	        						fiveCardStudState.minimum,
	        						fiveCardStudState.players,
	        						fiveCardStudState.cards
	        		);
	        return;
	    }
	    if (updateUI.isAiPlayer()) {
	        // TODO: implement AI in a later HW!
	        //container.sendMakeMove(..);
	        return;
	    }
	    
	    // must be player
	    view.setPlayerState(fiveCardStudState.nextTurn,
				fiveCardStudState.thisTurn,
				fiveCardStudState.stage,
				fiveCardStudState.dealer,
				fiveCardStudState.big,
				fiveCardStudState.small,
				fiveCardStudState.bigAnte,
				fiveCardStudState.smallAnte,
				fiveCardStudState.currentCard,
				fiveCardStudState.minimum,
				fiveCardStudState.players,
				fiveCardStudState.cards
	    		);
	    String stage = fiveCardStudState.stage;
	    System.out.println("thisTurn " + fiveCardStudState.thisTurn + " playerIds " + playerIds);
	    String playerString = Color.values()[playerIds.indexOf(fiveCardStudState.thisTurn)].toString();
	    System.out.println("playerString " + playerString + " stage " + stage );
	    switch(stage){
	    case DEAL:
	    		deal();
	    		break;
	    case BETEND:
	    case BET:
	    		bet(betAmt,yourPlayerId,fiveCardStudState.nextTurn);
	    		break;
	    case PUTSMALLANTE:
	    		int smallAmt = (Integer)((Map)fiveCardStudState.players.get(playerString)).get(MONEY);
	    		putSmall(smallAmt);
	    		break;
	    case PUTBIGANTE:
	    		int bigAmt = (Integer)((Map)fiveCardStudState.players.get(playerString)).get(MONEY);
	    		putBig(bigAmt);
	    		break;
	    case ASSIGNANTE:
	    		bet(betAmt,yourPlayerId,fiveCardStudState.nextTurn);
	    		break;
	    case INITDEAL:
	    		initDeal();
	    		break;
	    }
	}
	
	private void sendInitialMove(List<Integer> playerIds) {
	    container.sendMakeMove(fiveCardStudLogic.getInitialMove(playerIds,dealerId));
	}

	public void bet(int amt, int yourPlayerId,int nextTurn){
		if(yourPlayerId == nextTurn){
			betAmt += amt;
			poolAmt += betAmt;
			System.out.println("Calling bet");
			view.bet(betAmt,yourPlayerId,poolAmt,nextTurn);
		}
	}
	
	public void betFinished(){
		container.sendMakeMove(fiveCardStudLogic.betMove(fiveCardStudState, betAmt, playerIds, lastMovePlayerId));
	}
	
	private void deal(){
		container.sendMakeMove(fiveCardStudLogic.dealMove(fiveCardStudState, dealerId, playerIds, lastMovePlayerId));
	}
	
	private void initDeal(){
		container.sendMakeMove(fiveCardStudLogic.initDealMove(fiveCardStudState, dealerId, playerIds, lastMovePlayerId));
	}
	
	private void putBig(int big){
		container.sendMakeMove(fiveCardStudLogic.putBigAnteMove(fiveCardStudState, big, playerIds, lastMovePlayerId));
	}
	
	private void putSmall(int small){
		container.sendMakeMove(fiveCardStudLogic.putSmallAnteMove(fiveCardStudState, small, playerIds, lastMovePlayerId));
	}
}
