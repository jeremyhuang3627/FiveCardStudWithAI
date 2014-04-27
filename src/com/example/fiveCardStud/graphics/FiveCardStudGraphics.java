package com.example.fiveCardStud.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.example.fiveCardStud.client.Card;
import com.example.fiveCardStud.client.FiveCardStudPresenter;
import com.example.fiveCardStud.client.Card.Rank;
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
  
  @UiField Label pool;
  @UiField Label roundInfoSpan;
  
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
	  		int minimum
		  )
  {
	  String roundInfo = "next turn player id : " + nextTurn + " this turn player id : " + thisTurn
			  			+ " stage " + stage + " dealer id " + dealer + " big blind id " + big + " small blind id "
			  			+ " big ante " + bigAnte + " small ante " + smallAnte + " current card index " + currentCard
			  			+ " minimum bet " + minimum;
    System.out.println("Setting round info ");
	  roundInfoSpan.setText(roundInfo);
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
  
  private void updatePool(int amt)
  {
  		pool.setText("" + amt);
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
  		Map<String,Object> players,
  		List<Optional<Card>> cards
		  )
  {
	  
	  List<Card> handW = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("W")).get("hand"),cards);
	  List<Card> handB = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("B")).get("hand"),cards);
	  List<Card> handG = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("G")).get("hand"),cards);
	  List<Card> handR = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("R")).get("hand"),cards);
	  List<Card> handY = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("Y")).get("hand"),cards);
	  
	  setRoundInfo(nextTurn,thisTurn,stage,dealer,big,small,bigAnte,smallAnte,currentCard,minimum);
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
  		Map<String,Object> players,
  		List<Optional<Card>> cards
		  )
  {
	  
	  setRoundInfo(nextTurn,thisTurn,stage,dealer,big,small,bigAnte,smallAnte,currentCard,minimum);
    List<Card> handW = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("W")).get("hand"),cards);
    List<Card> handB = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("B")).get("hand"),cards);
    List<Card> handG = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("G")).get("hand"),cards);
    List<Card> handR = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("R")).get("hand"),cards);
    List<Card> handY = cardIndexesToCards((List<Integer>)((Map<String,Object>)players.get("Y")).get("hand"),cards);
	  
    placeImages(playerAreaW,createPlayerCardImages(handW,thisTurn,wId));
	  placeImages(playerAreaB,createPlayerCardImages(handB,thisTurn,bId));
	  placeImages(playerAreaG,createPlayerCardImages(handG,thisTurn,gId));
	  placeImages(playerAreaR,createPlayerCardImages(handR,thisTurn,rId));
	  placeImages(playerAreaY,createPlayerCardImages(handY,thisTurn,yId));
  }
  
  @Override
  public void bet(int amt,int playerId,int poolAmt,int nextTurn)
  {	
      System.out.println("Calling bet on view");
  	  String message = "Please put a chip";
  	  poolAmt = amt + poolAmt;
  	  updatePool(poolAmt);
  	  final int id = playerId;
      final int nextId = playerId;
  	  new PopupChoices(message,chipValues,
  	  	new PopupChoices.OptionChosen(){
  	  		@Override
  	  		public void optionChosen(String option){
  	  			if(option != "Done"){
  	  				presenter.bet(Integer.parseInt(option),id,nextId);
  	  			}else{
  	  				presenter.betFinished();
  	  			}
  	  		}
  	  	}
  	  ).center();
  }
  
}
