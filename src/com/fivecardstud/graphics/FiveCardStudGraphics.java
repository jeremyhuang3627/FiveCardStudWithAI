package com.fivecardstud.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fivecardstud.client.Card;
import com.fivecardstud.client.FiveCardStudPresenter;
import com.fivecardstud.client.Card.Rank;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.dom.client.SpanElement;
import com.google.common.base.Optional;

public class FiveCardStudGraphics extends Composite implements FiveCardStudPresenter.View{
	public interface FiveCardStudGraphicsUiBinder extends UiBinder<Widget,FiveCardStudGraphics>{
	}
	
  @UiField
  HorizontalPanel playerAreaW;
  @UiField
  HorizontalPanel playerAreaB;
  @UiField
  HorizontalPanel playerAreaG;
  @UiField
  HorizontalPanel playerAreaR;
  @UiField
  HorizontalPanel playerAreaY;
  
  @UiField Label potInfo;
  @UiField Label debugInfo;
  @UiField Label gameInfo;
  
  private boolean enableClicks = false;
  private final CardImageSupplier cardImageSupplier;
  private FiveCardStudPresenter presenter;
  private int viewerId = -1;
  private int wId = 42;
  private int bId = 43;
  private int gId = 44;
  private int rId = 45;
  private int yId = 46;
  List<String> chipValues = Lists.newArrayList();
  {
  	chipValues.add("10");
  	chipValues.add("50");
  	chipValues.add("100");
  	chipValues.add("200");
  	chipValues.add("500");
    chipValues.add("Done");
  }
  
  public FiveCardStudGraphics() {
	CardImages cardImages = GWT.create(CardImages.class);
	this.cardImageSupplier = new CardImageSupplier(cardImages);
	FiveCardStudGraphicsUiBinder uiBinder = GWT.create(FiveCardStudGraphicsUiBinder.class);
	initWidget(uiBinder.createAndBindUi(this));
  }
  
  private void setRoundInfo(
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
	  		int pot
		  )
  {
	  String roundInfo = "next turn player id : " + nextTurn + " this turn player id : " + thisTurn
			  			+ " stage " + stage + " dealer id " + dealer + " big blind id " + big + " small blind id "
			  			+ " big ante " + bigAnte + " small ante " + smallAnte + " current card index " + currentCard
			  			+ " minimum bet " + minimum;
	  System.out.println("Setting round info "); 
	  debugInfo.setText(roundInfo);
	  String player = getPlayerNameFromId(nextTurn);
	  gameInfo.setText("Next round is " + player);
	  String potmsg = "Pot size is : " + pot;
	  potInfo.setText(potmsg);
  }
  
  private String getPlayerNameFromId(int id)
  {
	  String player = "";
	  switch(id){
	  case 42:
		  player = "White";
		  break;
	  case 43:
		  player = "Black";
		  break;
	  case 44:
		  player = "Grey";
		  break;
	  case 45:
		  player = "Red";
		  break;
	  case 46:
		  player = "Yellow";
		  break;
	  default:
		  player = "Viewer";
		  break;
	  }
	  
	  return player;
  }
  
  
  
  private List<Image> createPlayerCardImages(List<Card> cards,int playerId, int yourId)
  {
	  List<CardImage> images = Lists.newArrayList();
	  for(int i=0;i<cards.size();i++)
	  {
		  if(playerId != yourId){  // is a viewer first card should face down
			  if(i==0){
				  images.add(CardImage.Factory.getBackOfCardImage());
			  }else{
				  images.add(CardImage.Factory.getCardImage(cards.get(i)));
			  }
	  	  }else{  //must be a player
	  		  images.add(CardImage.Factory.getCardImage(cards.get(i))); 
	  	  }
	  }
	  return createImages(images);
  }
  
  private List<Image> createImages(List<CardImage> images) {
	    List<Image> res = Lists.newArrayList();
	    for (CardImage img : images) {
	      final CardImage imgFinal = img;
	      Image image = new Image(cardImageSupplier.getResource(img));
	      res.add(image);
	    }
	    return res;
  }
  
  
  private void placeImages(HorizontalPanel panel, List<Image> images) {
	    panel.clear();
	    Image last = images.isEmpty() ? null : images.get(images.size() - 1);
	    for (Image image : images) {
	      FlowPanel imageContainer = new FlowPanel();
	      imageContainer.setStyleName(image != last ? "imgShortContainer" : "imgContainer");
	      imageContainer.add(image);
	      panel.add(imageContainer);
	    }
  }
  
  private List<Card> cardIndexesToCards(List<Integer> cardIndexes,List<Optional<Card>> fullDeck)
  {
	  List<Card> hand = Lists.newArrayList();
    if(cardIndexes != null){
  	  for(int i=0;i<cardIndexes.size();i++)
  	  {
  		  Optional<Card> card = fullDeck.get(cardIndexes.get(i));
  		  if(card.isPresent()){
  			  hand.add(card.get());
  		  }
  	  }
    }
	  return hand;
  }

  @Override
  public void setPresenter(FiveCardStudPresenter presenter) {
    this.presenter = presenter;
  }
  
  @Override
  public void setViewerState(
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
  		int pot,
  		Map<String,Object> players,
  		List<Optional<Card>> cards
		  )
  {
	  
	  List<Card> handW = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("W")).get("hand"),cards);
	  List<Card> handB = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("B")).get("hand"),cards);
	  List<Card> handG = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("G")).get("hand"),cards);
	  List<Card> handR = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("R")).get("hand"),cards);
	  List<Card> handY = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("Y")).get("hand"),cards);
	  
	  setRoundInfo(nextTurn,thisTurn,stage,dealer,big,small,bigAnte,smallAnte,currentCard,minimum,pot);
	  placeImages(playerAreaW,createPlayerCardImages(handW,thisTurn,viewerId));
	  placeImages(playerAreaB,createPlayerCardImages(handB,thisTurn,viewerId));
	  placeImages(playerAreaG,createPlayerCardImages(handG,thisTurn,viewerId));
	  placeImages(playerAreaR,createPlayerCardImages(handR,thisTurn,viewerId));
	  placeImages(playerAreaY,createPlayerCardImages(handY,thisTurn,viewerId));
  }
  
  @Override
  public void setPlayerState(
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
  		int pot,
  		Map<String,Object> players,
  		List<Optional<Card>> cards,
  		int yourPlayerId
		  )
  {
	  
	setRoundInfo(nextTurn,thisTurn,stage,dealer,big,small,bigAnte,smallAnte,currentCard,minimum,pot);
    List<Card> handW = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("W")).get("hand"),cards);
    List<Card> handB = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("B")).get("hand"),cards);
    List<Card> handG = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("G")).get("hand"),cards);
    List<Card> handR = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("R")).get("hand"),cards);
    List<Card> handY = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("Y")).get("hand"),cards);
	  
    if(stage != "endgame"){
	    placeImages(playerAreaW,createPlayerCardImages(handW,yourPlayerId,wId));
		placeImages(playerAreaB,createPlayerCardImages(handB,yourPlayerId,bId));
		placeImages(playerAreaG,createPlayerCardImages(handG,yourPlayerId,gId));
		placeImages(playerAreaR,createPlayerCardImages(handR,yourPlayerId,rId));
		placeImages(playerAreaY,createPlayerCardImages(handY,yourPlayerId,yId));
    }else{ //show all cards
    		placeImages(playerAreaW,createPlayerCardImages(handW,wId,wId));
		placeImages(playerAreaB,createPlayerCardImages(handB,bId,bId));
		placeImages(playerAreaG,createPlayerCardImages(handG,gId,gId));
		placeImages(playerAreaR,createPlayerCardImages(handR,rId,rId));
		placeImages(playerAreaY,createPlayerCardImages(handY,yId,yId));
    }
  }
  
  @Override
  public void bet(int amt,int poolAmt)
  {	
      System.out.println("Calling bet on view");
  	  String message = "Please put a chip";
  	  poolAmt = amt + poolAmt;
  	  new PopupChoices(message,chipValues,
  	  	new PopupChoices.OptionChosen(){
  	  		@Override
  	  		public void optionChosen(String option){
  	  			if(option != "Done"){
  	  				presenter.bet(Integer.parseInt(option));
  	  			}else{
  	  				presenter.betFinished();
  	  			}
  	  		}
  	  	}
  	  ).center();
  }
  
  @Override
  public void putBig(){
	  String message = "Please please put big ante 50 bucks";
	  List<String> option = Lists.newArrayList();
	  option.add("OK");
	  new PopupChoices(message,option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK"){
		  	  				presenter.putBigFinished(50);
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void putSmall(){
	  String message = "Please please put small ante 25 bucks";
	  List<String> option = Lists.newArrayList();
	  option.add("OK");
	  new PopupChoices(message,option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK"){
		  	  				presenter.putSmallFinished(25);
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void deal(){
	  String message = "Dealer, please deal the cards";
	  List<String> option = Lists.newArrayList();
	  option.add("OK");
	  new PopupChoices(message,option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK"){
		  	  				presenter.dealFinished();
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void initDeal(){
	  String message = "Dealer, please deal all players two cards";
	  List<String> option = Lists.newArrayList();
	  option.add("OK");
	  new PopupChoices(message,option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK"){
		  	  				presenter.initDealFinished();
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void showWinner(String winner)
  {	  
	  String message = "Game Over and the winner is ... " + winner ;
	  List<String> option = Lists.newArrayList();
	  option.add("OK");
	  new PopupChoices(message,option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK"){
		  	  				presenter.initDealFinished();
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
}
