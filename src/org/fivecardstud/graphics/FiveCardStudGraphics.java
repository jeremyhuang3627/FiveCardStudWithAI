package org.fivecardstud.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.fivecardstud.client.Card;
import org.fivecardstud.client.FiveCardStudPresenter;
import org.fivecardstud.client.Card.Rank;
import org.fivecardstud.client.FiveCardStudAnimation;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.media.client.Audio;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.common.base.Optional;
import com.google.gwt.dom.client.Style.Position;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;
import com.google.gwt.event.shared.EventHandler;

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
  
  @UiField Label gameInfo;
  
  private boolean enableClicks = false;
  private final CardImageSupplier cardImageSupplier;
  private final GameSound gameSounds;
  private Audio pieceDown;
  private FiveCardStudPresenter presenter;
  private PickupDragController dragController;
  private SimpleDropController dropController;
  private String viewerId = "-1";
  private String wId = "42";
  private String bId = "43";
  private String gId = "44";
  private String rId = "45";
  private String yId = "46";
  private boolean opendeck = false;
  private PopUpMessages msg = (PopUpMessages)GWT.create(PopUpMessages.class);
  
  List<String> chipValues = Lists.newArrayList();
  {
  	chipValues.add("10");
  	chipValues.add("50");
  	chipValues.add("100");
  	chipValues.add("200");
  	chipValues.add("500");
    chipValues.add(msg.okMsg());
  }
  
  public FiveCardStudGraphics() {
	CardImages cardImages = GWT.create(CardImages.class);
	this.cardImageSupplier = new CardImageSupplier(cardImages);
	this.gameSounds = GWT.create(GameSound.class);
	if(Audio.isSupported()){
		this.pieceDown = Audio.createIfSupported();
		pieceDown.addSource(gameSounds.pieceDownMp3().getSafeUri()
                .asString(), AudioElement.TYPE_MP3);
		pieceDown.addSource(gameSounds.pieceDownWav().getSafeUri()
	                	.asString(), AudioElement.TYPE_WAV);
	}
	
	FiveCardStudGraphicsUiBinder uiBinder = GWT.create(FiveCardStudGraphicsUiBinder.class);
	initWidget(uiBinder.createAndBindUi(this));
	CardImage cardimage = CardImage.Factory.getBackOfCardImage();
	Image image = new Image(cardImageSupplier.getResource(cardimage));
  }
  
  private String translatePlayerStr(String playerStr)
  {
	  if(playerStr.equals("White"))
	  {
		  return msg.whitePlayer();
	  }else if(playerStr.equals("Grey"))
	  {
		  return msg.greyPlayer();
	  }else if(playerStr.equals("Black"))
	  {
		  return msg.blackPlayer();
	  }else if(playerStr.equals("Yellow"))
	  {
		  return msg.yellowPlayer();
	  }else if(playerStr.equals("Red"))
	  {
		  return msg.redPlayer();
	  }else{
		  return playerStr;
	  }
  }
  
  private void setRoundInfo(
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
	  		int pot
		  )
  {
	  String player = getPlayerNameFromId(Integer.parseInt(nextTurn));
	  gameInfo.setText(msg.roundMsg() + " " + translatePlayerStr(player));
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
  
  private List<Image> updateCardDeck(int remain)
  {
	  List<CardImage> images = Lists.newArrayList();
	  for(int i=0;i<remain;i++)
	  {
		  images.add(CardImage.Factory.getBackOfCardImage());
	  }
	  return createImages(images);
  }
  
  private void initDeckAnimation(HorizontalPanel panel, int remain){
	  List<Image> images = updateCardDeck(remain);
	  int i=0;
	  for(Image img : images)
	  {
		  i += 20;
		  AbsolutePanel imageContainer = new AbsolutePanel();
		  imageContainer.add(img);
		  imageContainer.getElement().getStyle().setPosition(Position.ABSOLUTE);
		  double width = img.getWidth();
		  imageContainer.getElement().getStyle().setLeft(-width, Unit.PX);
		  panel.add(imageContainer);
		  FiveCardStudAnimation animation = new FiveCardStudAnimation(img,i);
		  animation.run(5000);
	  }
  }
  
  private List<Image> createPlayerCardImages(List<Card> cards,String playerId, String yourId)
  {
	  List<CardImage> images = Lists.newArrayList();
	  for(int i=0;i<cards.size();i++)
	  {
		  if(!playerId.equals(yourId)){  // is a viewer first card should face down
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
  
  private void putLabel()
  {
	Label bl = new Label(msg.blackPlayer());
	playerAreaB.add(bl);
	Label wl = new Label(msg.whitePlayer());
	playerAreaW.add(wl);
	Label gl = new Label(msg.greyPlayer());
	playerAreaG.add(gl);
	Label rl = new Label(msg.redPlayer());
	playerAreaR.add(rl);
	Label yl = new Label(msg.yellowPlayer());
	playerAreaY.add(yl);
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
	  
	  if(stage.equals("putbigante") || stage.equals("putsmallante") || stage.equals("assignante")){
		  System.out.println("putting label");
		  putLabel();
	  }
	  
	  pieceDown.play();
  }
  
  @Override
  public void setPlayerState(
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
		  )
  {
	System.out.println("set player state ");
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
    
    if(stage.equals("putbigante") || stage.equals("putsmallante") || stage.equals("assignante")){
		  putLabel();
	}
    
    pieceDown.play();
  }
  
  @Override
  public void bet(int amt,int poolAmt)
  {	
  	  poolAmt = amt + poolAmt;
  	  new PopupChoices(msg.betMsg(),chipValues,
  	  	new PopupChoices.OptionChosen(){
  	  		@Override
  	  		public void optionChosen(String option){
  	  			if(!option.equals(msg.okMsg())){
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
	  List<String> option = Lists.newArrayList();
	  option.add(msg.okMsg());
	  new PopupChoices(msg.putBigMsg(50),option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK" || option == "\u597D\u7684"){
		  	  				presenter.putBigFinished(50);
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void putSmall(){
	  List<String> option = Lists.newArrayList();
	  option.add(msg.okMsg());
	  new PopupChoices(msg.putSmallMsg(25),option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK" || option == "\u597D\u7684"){
		  	  				presenter.putSmallFinished(25);
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void deal(){
	  List<String> option = Lists.newArrayList();
	  option.add(msg.okMsg());
	  new PopupChoices(msg.dealMsg(),option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK" || option == "\u597D\u7684"){
		  	  				presenter.dealFinished();
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void initDeal(){
	  List<String> option = Lists.newArrayList();
	  option.add(msg.okMsg());
	  new PopupChoices(msg.initDealMsg(),option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK" || option == "\u597D\u7684"){
		  	  				presenter.initDealFinished();
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
  
  @Override
  public void showWinner(String winner)
  {	 
	  List<String> option = Lists.newArrayList();
	  option.add(msg.okMsg());
	  new PopupChoices(msg.showWinnerMsg(translatePlayerStr(winner)),option,
		  	  	new PopupChoices.OptionChosen(){
		  	  		@Override
		  	  		public void optionChosen(String option){
		  	  			if(option == "OK" || option == "\u597D\u7684"){
		  	  				presenter.initDealFinished();
		  	  			}
		  	  		}
		  	  	}
		  	  ).center();
  }
}
