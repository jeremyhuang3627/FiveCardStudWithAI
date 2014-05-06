package org.fivecardstud.graphics;

import org.fivecardstud.client.FiveCardStudLogic;
import org.fivecardstud.client.FiveCardStudPresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.Element;
import java.math.BigDecimal;
import com.google.gwt.user.client.Timer;

public class FiveCardStudEntryPoint implements EntryPoint {
	IteratingPlayerContainer container;
  	FiveCardStudPresenter fiveCardStudPresenter;
  	private PopUpMessages msg = (PopUpMessages)GWT.create(PopUpMessages.class);
  	
  	@Override
  	public void onModuleLoad(){
	    Game game = new Game(){
	      @Override
	      public void sendVerifyMove(VerifyMove verifyMove) {
	        container.sendVerifyMoveDone(new FiveCardStudLogic().verify(verifyMove));
	      }

	      @Override
	      public void sendUpdateUI(UpdateUI updateUI) {
	    	  	final UpdateUI _updateUI = updateUI;
	    	  	System.out.println("calling updateUi in game.sendUpdateUI");
	        fiveCardStudPresenter.updateUI(updateUI);
	        
	      }
	    };
	    container = new IteratingPlayerContainer(game, 5);
	    FiveCardStudGraphics fiveCardStudGraphics = new FiveCardStudGraphics(container);
		fiveCardStudPresenter = new FiveCardStudPresenter(fiveCardStudGraphics, container);
		final ListBox playerSelect = new ListBox();
		    playerSelect.addItem(msg.whitePlayer());
		    playerSelect.addItem(msg.blackPlayer());
		    playerSelect.addItem(msg.greyPlayer());
		    playerSelect.addItem(msg.redPlayer());
		    playerSelect.addItem(msg.yellowPlayer());
		    playerSelect.addItem(msg.viewer());
		    playerSelect.addChangeHandler(new ChangeHandler() {
		    @Override
		      public void onChange(ChangeEvent event) {
		        int selectedIndex = playerSelect.getSelectedIndex();
		        String playerId = selectedIndex == 5d ? GameApi.VIEWER_ID
		            : container.getPlayerIds().get(selectedIndex);
		        container.updateUi(playerId);
		      }
		    }); 
	    FlowPanel flowPanel = new FlowPanel();
	    flowPanel.add(fiveCardStudGraphics);
	  //  flowPanel.add(playerSelect);
	    RootPanel.get("mainDiv").add(flowPanel);
	    container.sendGameReady();
	    System.out.println("calling container.updateUi in entrypoint");
	    container.updateUi(container.getPlayerIds().get(2)); 
  }
}