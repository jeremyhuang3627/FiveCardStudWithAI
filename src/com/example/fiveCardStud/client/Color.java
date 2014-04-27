package com.example.fiveCardStud.client;

public enum Color {
	  W,B,G,R,Y;

	  public boolean isWhite() {
	    return this == W;
	  }

	  public boolean isBlack() {
	    return this == B;
	  }
	  
	  public boolean isGrey() {
		    return this == G;
	  }
	  
	  public boolean isRed() {
		    return this == R;
	  }
	  
	  public boolean isYellow() {
		    return this == Y;
	  }

	  public Color getNextColor() {
		  return Color.values()[(this.ordinal() + 1)%Color.values().length];
	  }
}
