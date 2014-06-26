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
     * Type id of the object that triggers the event (either the avatar,
     * or something created by the avatar).
     */
    public int activeTypeId;

    /**
     * Type id of the object that received the event (what did the avatar,
     * or something created by the avatar, collided with?).
     */
    public int passiveTypeId;

    /**
     * Sprite ID of the object that triggers the event (either the avatar,
     * or something created by the avatar).
     */
    public int activeSpriteId;

    /**
     * Sprite ID of the object that received the event (what did the avatar,
     * or something created by the avatar, collided with?).
     */
    public int passiveSpriteId;


    /**
     * Position where the event took place.
     */
    public Vector2d position;

    /**
     * Constructor
     * @param gameStep when the event happened.
     * @param fromAvatar did the avatar trigger the event (true), or something created by him (false)?
     * @param activeTypeId type of the sprite (avatar or from avatar).
     * @param passiveTypeId type of the sprite that collided with activeTypeId.
     * @param activeSpriteId sprite ID of the avatar (or something created by the avatar).
     * @param passiveSpriteId sprite ID of the other object.
     * @param position where did the event take place.
     */
    public Event(int gameStep, boolean fromAvatar, int activeTypeId, int passiveTypeId,
                 int activeSpriteId, int passiveSpriteId, Vector2d position)
    {
        this.gameStep = gameStep;
        this.fromAvatar = fromAvatar;
        this.activeTypeId = activeTypeId;
        this.passiveTypeId = passiveTypeId;
        this.activeSpriteId = activeSpriteId;
        this.passiveSpriteId = passiveSpriteId;
        this.position = position;
    }

    /**
     * Creates a copy of this event.
     * @return the copy.
     */
    public Event copy()
    {
        return new Event(gameStep, fromAvatar, activeTypeId, passiveTypeId, activeSpriteId, passiveSpriteId, position.copy());
    }

    @Override
    public int compareTo(Event o) {
        if(this.gameStep < o.gameStep)       return -1;   //First tie break: gameStep.
        if(this.gameStep > o.gameStep)       return 1;
        if(this.fromAvatar && !o.fromAvatar) return -1;   //Second tie break: who triggered.
        if(!this.fromAvatar && o.fromAvatar) return 1;
        if(this.passiveTypeId < o.passiveTypeId)     return -1;   //Third tie break: against what.
        if(this.passiveTypeId > o.passiveTypeId)     return 1;
        if(this.activeTypeId < o.activeTypeId)       return -1;   //Fourth tie break: who triggered it
        if(this.activeTypeId > o.activeTypeId)       return 1;
        return 0;
    }
}
