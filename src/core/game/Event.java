package core.game;

import tools.Vector2d;

/**
 * Created by diego on 24/03/14.
 */
public class Event implements Comparable<Event>
{
    /**
     * Game step when the event happened.
     */
    public int gameStep;

    /**
     * True if the avatar triggered the event, false
     * otherwise (example: something that is thrown by the
     * avatar hits a sprite).
     */
    public boolean fromAvatar;

    /**
     * Sprite id of the object that triggers the event (either the avatar,
     * or something created by the avatar).
     */
    public int activeId;


    /**
     * Sprite id of the object that received the event (what did the avatar,
     * or something created by the avatar, collided with?).
     */
    public int passiveId;

    /**
     * Position where the event took place.
     */
    public Vector2d position;

    /**
     * Constructor
     * @param gameStep when the event happened.
     * @param fromAvatar did the avatar trigger the event.
     * @param passiveId against what type of object.
     * @param position where did the event take place.
     */
    public Event(int gameStep, boolean fromAvatar, int activeId, int passiveId, Vector2d position)
    {
        this.gameStep = gameStep;
        this.fromAvatar = fromAvatar;
        this.activeId = activeId;
        this.passiveId = passiveId;
        this.position = position;
    }

    /**
     * Creates a copy of this event.
     * @return the copy.
     */
    public Event copy()
    {
        return new Event(gameStep, fromAvatar, activeId, passiveId, position.copy());
    }

    @Override
    public int compareTo(Event o) {
        if(this.gameStep < o.gameStep)       return -1;   //First tie break: gameStep.
        if(this.gameStep > o.gameStep)       return 1;
        if(this.fromAvatar && !o.fromAvatar) return -1;   //Second tie break: who triggered.
        if(!this.fromAvatar && o.fromAvatar) return 1;
        if(this.passiveId < o.passiveId)     return -1;   //Third tie break: against what.
        if(this.passiveId > o.passiveId)     return 1;
        if(this.activeId < o.activeId)       return -1;   //Fourth tie break: who triggered it
        if(this.activeId > o.activeId)       return 1;
        return 0;
    }
}
