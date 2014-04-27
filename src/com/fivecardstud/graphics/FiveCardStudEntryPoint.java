package com.fivecardstud.graphics;

import com.fivecardstud.client.FiveCardStudLogic;
import com.fivecardstud.client.FiveCardStudPresenter;
import com.fivecardstud.client.GameApi;
import com.fivecardstud.client.GameApi.Game;
import com.fivecardstud.client.GameApi.IteratingPlayerContainer;
import com.fivecardstud.client.GameApi.UpdateUI;
import com.fivecardstud.client.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

public class FiveCardStudEntryPoint implements EntryPoint {
	IteratingPlayerContainer container;
  	FiveCardStudPresenter fiveCardStudPresenter;

  	@Override
  	public void onModuleLoad(){
	    Game game = new Game(){
	      @Override
	      public void sendVerifyMove(VerifyMove verifyMove) {
	        container.sendVerifyMoveDone(new FiveCardStudLogic().verify(verifyMove));
	      }

	      @Override
	      public void sendUpdateUI(UpdateUI updateUI) {
	        fiveCardStudPresenter.updateUI(updateUI);
	      }
	    };
	    container = new IteratingPlayerContainer(game, 5);
	    FiveCardStudGraphics fiveCardStudGraphics = new FiveCardStudGraphics();
	    fiveCardStudPresenter =
	        new FiveCardStudPresenter(fiveCardStudGraphics, container);
	    final ListBox playerSelect = new ListBox();
	    playerSelect.addItem("White");
	    playerSelect.addItem("Black");
	    playerSelect.addItem("Grey");
	    playerSelect.addItem("Red");
	    playerSelect.addItem("Yellow");
	    playerSelect.addItem("Viewer");
	    playerSelect.addChangeHandler(new ChangeHandler() {
	    @Override
	      public void onChange(ChangeEvent event) {
	        int selectedIndex = playerSelect.getSelectedIndex();
	        int playerId = selectedIndex == 5d ? GameApi.VIEWER_ID
	            : container.getPlayerIds().get(selectedIndex);
	        container.updateUi(playerId);
	      }
	    });
	    FlowPanel flowPanel = new FlowPanel();
	    flowPanel.add(fiveCardStudGraphics);
	    flowPanel.add(playerSelect);
	    RootPanel.get("mainDiv").add(flowPanel);
	    container.sendGameReady();
	    System.out.println("in entrypoint playerId " + container.getPlayerIds().get(2));
	    container.updateUi(container.getPlayerIds().get(2)); 
  }
}