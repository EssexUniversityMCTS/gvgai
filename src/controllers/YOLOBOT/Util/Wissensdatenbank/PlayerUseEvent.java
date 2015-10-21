package controllers.YOLOBOT.Util.Wissensdatenbank;


public class PlayerUseEvent implements YoloEventController {
	
	private UseEvent triggerEvent;
	private boolean eventSeen;
	
	public PlayerUseEvent() {
	}

	public void learnTriggerEvent(byte scoreDelta, boolean wall){
		if(triggerEvent == null)
			triggerEvent = new UseEvent();
		eventSeen = true;
		triggerEvent.update(scoreDelta, wall);
	}
	
	public UseEvent getTriggerEvent(){
		return triggerEvent;
	}
	
	public boolean willTrigger(){
		return eventSeen;
	}
	
}
