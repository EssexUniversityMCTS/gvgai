package audio;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

/**
 * Created by Carlotes(User) on 23/05/2017.
 *
 * Handles the audio implementation. Only WAV files supported at the moment
 */
public class AudioCtrl {

    private String audioSource;

    private Sound soundObject;

    private Music musicObject;

    public AudioCtrl() {

    }

    public AudioCtrl(String audioSource) {
        this.audioSource = audioSource;
    }

    /**
     * Loads into the instance the sound
     * @param audioSource
     */
    public void LoadSound(String audioSource) {
        if (CheckInit()) {
            this.soundObject = TinySound.loadSound(audioSource);
        }
    }

    /**
     * Loads into the instance the music file
     * @param audioSource the path to load
     */
    public void LoadMusic(String audioSource) {
        if (CheckInit()) {
            this.musicObject = TinySound.loadMusic(audioSource);
        }
    }

    /**
     * Loads a sound into the reference passed in
     * @param soundToPlay the reference to load the audio into
     * @param soundSource the path to load
     */
    public void PlaySound(Sound soundToPlay, String soundSource) {
        // We check that the audiosystem is initialized
        if (CheckInit()) {

            if (soundToPlay == null) {
                // We load the sound
                soundToPlay = TinySound.loadSound(soundSource);
            }

            // We play it
            PlaySound(soundToPlay);
        }
    }

    /**
     * Plays the sound passed in
     * @param soundToPlay
     */
    public void PlaySound(Sound soundToPlay) {
        if (soundToPlay == null) {
//            System.out.println("soundToPlay = " + soundToPlay);
            return;
        }

        if (CheckInit()) {
            soundToPlay.play();
        }

    }

    /**
     * Loads and plays the sound according to a path
     * @param audioSource
     */
    public void PlaySound(String audioSource) {
        if (audioSource != null) {
            LoadSound(audioSource);
            PlaySound();
        }
    }

    /**
     * Plays the sound contained in the instance of this object
     */
    public void PlaySound() {
        PlaySound(this.soundObject);
    }

    public void PlayMusic(Music musicToPlay, String musicSource) {
        // We check that the audiosystem is initialized
        if (CheckInit()) {

            if (musicToPlay == null) {
                // We load the music
                musicToPlay = TinySound.loadMusic(musicSource, true);
            }

            // We play it
            PlayMusic(musicToPlay);
        }
    }

    public void PlayMusic (Music musicToPlay) {
        if (musicToPlay == null) {
//            System.out.println("musicToPlay = " + musicToPlay);
            return;
        }

        if (CheckInit()) {
            musicToPlay.play(true);
        }
    }

    public void PlayMusic () {
        PlayMusic(this.musicObject);
    }

    /**
     *  Check if the audio system is initialized, and if not, initializes it
     * @return false if the system was not initialized
     */
    private boolean CheckInit() {

        if (!TinySound.isInitialized()) {
            InitializeSystem();
        }
        return TinySound.isInitialized();

    }

    /**
     * Initializes the entire system. Call on begin
     */
    public void InitializeSystem() {
        //initialize TinySound
        TinySound.init();
    }

    /**
     * Terminates the entire system. Call on end
     */
    public void ShutDownSystem() {
        //be sure to shutdown TinySound when done
        TinySound.shutdown();
    }





}
