package com.example.fiveCardStud.client;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class FiveCardStudState {
	public final int nextTurn;
	public final int thisTurn;
	public final String stage;
	public final int dealer;
	public final int big;
	public final int small;
	public final int bigAnte;
	public final int smallAnte;
	public final int currentCard;
	public final int minimum;
	public final Map<String,Object> players = new HashMap<String,Object>();
	public final List<Optional<Card>> cards;
	
	public FiveCardStudState(int nextTurn,
						int thisTurn,
						String stage,
						int dealer,
						int big,
						int small,
						int bigAnte,
						int smallAnte,
						int currentCard,
						int minimum,
						Map<String,Object> W,
						Map<String,Object> B,
						Map<String,Object> G,
						Map<String,Object> R,
						Map<String,Object> Y,
						List<Optional<Card>> cards
						){
		this.nextTurn = nextTurn;
		this.thisTurn = thisTurn;
		this.stage = stage;
		this.dealer = dealer;
		this.big = big;
		this.small = small;
		this.bigAnte = bigAnte;
		this.smallAnte = smallAnte;
		this.currentCard = currentCard;
		this.minimum = minimum;
		this.players.put("W", W);
		this.players.put("B", B);
		this.players.put("G", G);
		this.players.put("R", R);
		this.players.put("Y", Y);
		this.cards = cards;
	}
	
	
}
