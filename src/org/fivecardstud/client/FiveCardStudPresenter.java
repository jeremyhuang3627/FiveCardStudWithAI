package org.fivecardstud.client;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

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
	    void bet(int betAmt,int poolAmt,String msg);
	    
	    void putBig(String msg);
	    
	    void putSmall(String msg);
	    void initDeal(String msg);
	    void deal(String msg);
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
	private static final Set<String> AI_IDs = new HashSet<String>(Arrays.asList(
		     new String[] {"42","43","44","45"}
	));
	
	
	public FiveCardStudPresenter(View view, Container container){
		System.out.println("Constructing presenter");
		this.view = view;
		this.container = container;
		view.setPresenter(this);
	}
	
	public static native void randomBet()/*-{
		$wnd.setTimeout(function(){
		  var btnArr = $doc.getElementsByClassName("gwt-Button");
		  var ranIndex;
		  if(btnArr.length>1){
		  	//randomly click a button;
		  	ranIndex = Math.floor(Math.random() * (btnArr.length - 1));
		  	if(ranIndex != btnArr.length-1){
		  		btnArr[ranIndex].click();
		  	}
		  }
		},3000);
	}-*/;
	
	public static native void clickOk()/*-{
		$wnd.setTimeout(function(){
		var btnArr = $doc.getElementsByClassName("gwt-Button");
		btnArr[btnArr.length-1].click();
		},3000);
}-*/;
	
	public void updateUI(UpdateUI updateUI){
		
		playerIds = updateUI.getPlayerIds();
		lastMovePlayerId = updateUI.getLastMovePlayerId();
		System.out.println("presenter.updateUI lastMovePlayerId " + lastMovePlayerId);
	    String yourPlayerId = updateUI.getYourPlayerId();
	    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
	    Color turnOfColor = null;
	    if (updateUI.getState().isEmpty()) {
	          // The G player sends the initial setup move.
	    //	System.out.println("Empty state. yourPlayerId " + yourPlayerId + " dealerId " + dealerId);
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
	    System.out.println("state");
	    System.out.println(updateUI.getState());
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
	    
	    String stage = fiveCardStudState.stage;
	    System.out.println("thisTurn " + fiveCardStudState.thisTurn + " playerIds " + playerIds);
	    String playerString = Color.values()[playerIds.indexOf(fiveCardStudState.thisTurn)].toString();
	    System.out.println("playerString " + playerString + " stage " + stage );
	    //AI_IDs.contains(yourPlayerId)
	   if (AI_IDs.contains(yourPlayerId)) {
	        //container.sendMakeMove(..);
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
		   
		   System.out.println("yourPlayerId " + yourPlayerId + " fiveCardStudState.nextTurn " + fiveCardStudState.nextTurn);
		    	if(yourPlayerId.equals(fiveCardStudState.nextTurn)){
		    		System.out.println("entering AI " + stage);
		    		String AI_msg = "AI Turn";
			    switch(stage){
			    case PUTBIGANTE:
			    		initDeal(AI_msg);
			    		clickOk();
			    		break;
			    case BETEND:
			    		if( fiveCardStudState.currentCard >=24){
			    			endGame();
			    			clickOk();
			    		}else{
			    			deal(AI_msg);
			    			clickOk();
			    		}
			    		break;
			    case DEAL:
			    case BET:
			    		bet(betAmt,AI_msg);
			    		randomBet();
			    		break;
			    case PUTSMALLANTE:
			    		putBig(AI_msg);
			    		clickOk();
			    		break;
			    case ASSIGNANTE:
			    		putSmall(AI_msg);
			    		clickOk();
			    		break;
			    case INITDEAL:
			    		bet(betAmt,"AI Turn");
			    		clickOk();
			    		break;
			    case ENDGAME:
			    		showWinner((String)updateUI.getState().get(WINNER));
			    		clickOk();
			    		break;
			    }
		    }
		    	
		    	
		    	
	    }else{
	    
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
		    
		   
		    if(yourPlayerId.equals(fiveCardStudState.nextTurn)){
		    		System.out.println("stage is " + stage);
		    		String player_msg = "Your Turn";
			    switch(stage){
			    case PUTBIGANTE:
			    		initDeal(player_msg);
			    		break;
			    case BETEND:
			    		if( fiveCardStudState.currentCard >=24){
			    			endGame();
			    		}else{
			    			deal(player_msg);
			    		}
			    		break;
			    case DEAL:
			    case BET:
			    		bet(betAmt,player_msg);
			    		break;
			    case PUTSMALLANTE:
			    		putBig(player_msg);
			    		break;
			    case ASSIGNANTE:
			    		putSmall(player_msg);
			    		break;
			    case INITDEAL:
			    		bet(betAmt,player_msg);
			    		break;
			    case ENDGAME:
			    		showWinner((String)updateUI.getState().get(WINNER));
			    		break;
			    }
		    }
	    }
	}
	
	private void sendInitialMove(List<String> playerIds) {
		System.out.println("in presenter sendInitialMove calling container.sendMakeMove");
	    container.sendMakeMove(fiveCardStudLogic.getInitialMove(playerIds,dealerId));
	}

	public void bet(int amt,String msg){
			betAmt += amt;
			poolAmt += betAmt;
			view.bet(betAmt, poolAmt, msg);
	}
	
	public void betFinished(){
		System.out.println("in presenter betFinished() calling container.sendMakeMove");
		container.sendMakeMove(fiveCardStudLogic.betMove(fiveCardStudState, betAmt, playerIds, lastMovePlayerId));
		betAmt = 0;
	}
	
	private void deal(String msg){
		view.deal(msg);
	}
	
	public void dealFinished(){
		System.out.println("in presenter dealFinished() calling container.sendMakeMove");
		container.sendMakeMove(fiveCardStudLogic.dealMove(fiveCardStudState, dealerId, playerIds, lastMovePlayerId));
	}
	
	private void initDeal(String msg){
		view.initDeal(msg);
	}
	
	public void initDealFinished(){
		System.out.println("in presenter initDealFinished() calling container.sendMakeMove");
		container.sendMakeMove(fiveCardStudLogic.initDealMove(fiveCardStudState, dealerId, playerIds, lastMovePlayerId));
	}
	
	private void putBig(String msg){
		view.putBig(msg);
	}
	
	private void putSmall(String msg){
		view.putSmall(msg);
	}
	
	public void putBigFinished(int big){
		System.out.println("in presenter putBigFinished() calling container.sendMakeMove");
		container.sendMakeMove(fiveCardStudLogic.putBigAnteMove(fiveCardStudState, big, playerIds, lastMovePlayerId));
	}
	
	public void putSmallFinished(int small){
		System.out.println("in presenter putSmallFinished() calling container.sendMakeMove");
		container.sendMakeMove(fiveCardStudLogic.putSmallAnteMove(fiveCardStudState, small, playerIds, lastMovePlayerId));
	}
	
	private void endGame(){
		container.sendMakeMove(fiveCardStudLogic.endGame(fiveCardStudState,playerIds));
	}
	
	private void showWinner(String winner){
		view.showWinner(winner);
		
	}
}
