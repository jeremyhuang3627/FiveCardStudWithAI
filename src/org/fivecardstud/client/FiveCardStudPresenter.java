package org.fivecardstud.client;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fivecardstud.client.Card.Rank;
import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;
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
	private static final String MINIMUM = "minimum";
	private static final String WINNER = "winner";
	private static final String ENDGAME = "endgame";
	
	public interface View {
	    void setPresenter(FiveCardStudPresenter cheatPresenter);
	    
	    /** pass in one state the method should figure out how to display the views */
	    /** Sets the state for a viewer, i.e., not one of the players. */
	    void setViewerState(
	    		String nextTurn,
	    		String thisTurn,
	    		String stage,
	    		String dealer,
	    		String big,
	    		String small,
	    		int bigAnte,
	    		int smallAnte,
	    		int currentCard,
	    		int minimum,
	    		int pot,
	    		Map<String,Object> players,
	    		List<Optional<Card>> cards
	    		);

	    /**
	     * Sets the state for a player (whether the player has the turn or not).
	     * 
	     */
	    void setPlayerState(
	    		String nextTurn,
	    		String thisTurn,
	    		String stage,
	    		String dealer,
	    		String big,
	    		String small,
	    		int bigAnte,
	    		int smallAnte,
	    		int currentCard,
	    		int minimum,
	    		int pot,
	    		Map<String,Object> players,
	    		List<Optional<Card>> cards,
	    		String yourPlayerId
	    		);
	    
	    /**
	     * Ask the player to put in chips. Each time a chip is put in the method calls the private
	     * bet method until betFinished is called or player's money is used up
	     * 
	     */
	    void bet(int betAmt,int poolAmt);
	    
	    void putBig();
	    
	    void putSmall();
	    void initDeal();
	    void deal();
	    void showWinner(String winner);
	}
	
	private final FiveCardStudLogic fiveCardStudLogic = new FiveCardStudLogic();
	private final View view;
	private final Container container;
	private final String dealerId = "44";
	private int myId;
	private FiveCardStudState fiveCardStudState;
	private List<Card> selectedCards;
	private int betAmt;
	private List<String> playerIds;
	private String lastMovePlayerId;
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
		lastMovePlayerId = updateUI.getLastMovePlayerId();
	    String yourPlayerId = updateUI.getYourPlayerId();
	    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
	    Color turnOfColor = null;
	    
	    if (updateUI.getState().isEmpty()) {
	          // The G player sends the initial setup move.
	    	System.out.println("Empty state. yourPlayerId " + yourPlayerId + " dealerId " + dealerId);
	    	if(yourPlayerId.equals(dealerId)){
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
	        						fiveCardStudState.pot,
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
				fiveCardStudState.pot,
				fiveCardStudState.players,
				fiveCardStudState.cards,
				yourPlayerId
	    		);
	    
	    String stage = fiveCardStudState.stage;
	  //  System.out.println("thisTurn " + fiveCardStudState.thisTurn + " playerIds " + playerIds);
	    String playerString = Color.values()[playerIds.indexOf(fiveCardStudState.thisTurn)].toString();
	  //  System.out.println("playerString " + playerString + " stage " + stage );
	    if(yourPlayerId.equals(fiveCardStudState.nextTurn)){
	    		System.out.println("stage is " + stage);
		    switch(stage){
		    case PUTBIGANTE:
		    		initDeal();
		    		break;
		    case BETEND:
		    		if( fiveCardStudState.currentCard >=24){
		    			endGame();
		    		}else{
		    			deal();
		    		}
		    		break;
		    case DEAL:
		    case BET:
		    		bet(betAmt);
		    		break;
		    case PUTSMALLANTE:
		    		putBig();
		    		break;
		    case ASSIGNANTE:
		    		putSmall();
		    		break;
		    case INITDEAL:
		    		bet(betAmt);
		    		break;
		    case ENDGAME:
		    		showWinner((String)updateUI.getState().get(WINNER));
		    		break;
		    }
	    }
	}
	
	private void sendInitialMove(List<String> playerIds) {
	    container.sendMakeMove(fiveCardStudLogic.getInitialMove(playerIds,dealerId));
	}

	public void bet(int amt){
			betAmt += amt;
			poolAmt += betAmt;
			view.bet(betAmt,poolAmt);
	}
	
	public void betFinished(){
		container.sendMakeMove(fiveCardStudLogic.betMove(fiveCardStudState, betAmt, playerIds, lastMovePlayerId));
		betAmt = 0;
	}
	
	private void deal(){
		view.deal();
	}
	
	public void dealFinished(){
		container.sendMakeMove(fiveCardStudLogic.dealMove(fiveCardStudState, dealerId, playerIds, lastMovePlayerId));
	}
	
	private void initDeal(){
		view.initDeal();
	}
	
	public void initDealFinished(){
		container.sendMakeMove(fiveCardStudLogic.initDealMove(fiveCardStudState, dealerId, playerIds, lastMovePlayerId));
	}
	
	private void putBig(){
		view.putBig();
	}
	
	private void putSmall(){
		view.putSmall();
	}
	
	public void putBigFinished(int big){
		container.sendMakeMove(fiveCardStudLogic.putBigAnteMove(fiveCardStudState, big, playerIds, lastMovePlayerId));
	}
	
	public void putSmallFinished(int small){
		System.out.println("putsmallfinished " + lastMovePlayerId);
		container.sendMakeMove(fiveCardStudLogic.putSmallAnteMove(fiveCardStudState, small, playerIds, lastMovePlayerId));
	}
	
	private void endGame(){
		container.sendMakeMove(fiveCardStudLogic.endGame(fiveCardStudState,playerIds));
	}
	
	private void showWinner(String winner){
		view.showWinner(winner);
	}
}
