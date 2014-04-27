package org.fivecardstud.graphics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface GameSound extends ClientBundle {
	 @Source("org/fivecardstud/resource/pieceDown.mp3")
     DataResource pieceDownMp3();

	 @Source("org/fivecardstud/resource/pieceDown.wav")
     DataResource pieceDownWav();
}
