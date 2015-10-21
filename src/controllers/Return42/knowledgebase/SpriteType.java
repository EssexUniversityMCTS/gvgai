package controllers.Return42.knowledgebase;

public enum SpriteType {
    TYPE_AVATAR,
    TYPE_RESOURCE,
    TYPE_PORTAL,
    TYPE_NPC,
    TYPE_STATIC,
    TYPE_FROMAVATAR,
    TYPE_MOVABLE,
    TYPE_UNKNOWN;
    
	public boolean isEqualOrUnknown(SpriteType otherType) {
		if (this.equals(otherType)) {
			return true;
		} else if (otherType.equals(SpriteType.TYPE_UNKNOWN)) {
			return true;
		} else if (this.equals(SpriteType.TYPE_UNKNOWN)) {
			return true;
		} else {
			return false;
		}
	}
}