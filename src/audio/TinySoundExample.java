package audio;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

public class TinySoundExample {

	public static void main(String[] args) {
		//initialize TinySound
		TinySound.init();
		//load a audio and music
		//note: you can also load with Files, URLs and InputStreams
		Music song = TinySound.loadMusic("pio.wav");
//		Sound coin = TinySound.loadSound("pio.wav");
		//start playing the music on loop
		song.play(true);

		// Sleep not a lot for a audio
//		//play the audio a few times in a loop
//		for (int i = 0; i < 20; i++) {
//			coin.play();
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {}
//		}


		// Sleep a lot for music
		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}

		//be sure to shutdown TinySound when done
		TinySound.shutdown();
	}
	
}
