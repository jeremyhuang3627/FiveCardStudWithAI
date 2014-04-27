package org.fivecardstud.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.dom.client.Style.Unit;

public class FiveCardStudAnimation extends Animation {
	Image image;
	double _end;
	
	public FiveCardStudAnimation(Image imageToMove,double endLeftPos)
	{
		image = imageToMove;
		_end = endLeftPos;
	}
	
     @Override
     protected void onUpdate(double progress) {
    	 	image.getParent().getElement().getStyle().setLeft(_end * progress, Unit.PX);
     }

     @Override
     protected void onCancel() {
             
     }

     @Override
     protected void onComplete() {
     }
	
}
