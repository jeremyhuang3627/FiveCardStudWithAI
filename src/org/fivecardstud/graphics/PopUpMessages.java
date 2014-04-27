package org.fivecardstud.graphics;

import com.google.gwt.i18n.client.Messages;;

public interface PopUpMessages extends Messages{
	
	String betMsg();
	
	String putBigMsg(int amt);
	
	String putSmallMsg(int amt);
	
	String okMsg();
	
	String dealMsg();
	
	String initDealMsg();
	
	String showWinnerMsg(String winner);
	
	String blackPlayer();
	
	String yellowPlayer();
	
	String greyPlayer();
	
	String redPlayer();
	
	String whitePlayer();
	
	String viewer();
	
	String roundMsg();
	
}
