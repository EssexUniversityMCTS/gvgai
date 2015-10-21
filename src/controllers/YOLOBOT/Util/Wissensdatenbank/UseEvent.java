package controllers.YOLOBOT.Util.Wissensdatenbank;



public class UseEvent extends Event{
	
	public UseEvent() {
		super(1,1);
	}
	
	public void update(byte scoreDelta, boolean wall){
		updateByteEvents(scoreDelta);
		updateBoolEvents(wall);
	}
	
	
	@Override
	public String toString() {
		String retVal = "Event ist:";
		if(byteEventsPropability[0] > Byte.MIN_VALUE && byteEvents[0] != -1)
			retVal += "\t Score change by: " + byteEvents[0];		
		return retVal;
	}

	public int getScoreDelta(){
		return byteEvents[0];
	}
	
	public boolean getWall(){
		return boolEvents[0];
	}
	
}
