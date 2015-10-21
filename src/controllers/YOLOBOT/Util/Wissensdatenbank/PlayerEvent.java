package controllers.YOLOBOT.Util.Wissensdatenbank;

import controllers.YOLOBOT.Agent;

public class PlayerEvent implements YoloEventController {

	
	private static final boolean DEBUG = false;
	private TriggerConditionWithInventory cancelTrigger;
	private TriggerConditionWithInventory specialEventTrigger;
	private YoloEvent specialEvent;
	private YoloEvent defaultEvent;

	private short observeCount;
	private short cancelCount;
	private short eventCount;
	
	public PlayerEvent() {
		observeCount = 0;
		cancelCount = 0;
		eventCount = 0;
		specialEvent = new YoloEvent();
		defaultEvent = new YoloEvent();
		cancelTrigger = new TriggerConditionWithInventory();
		specialEventTrigger = new TriggerConditionWithInventory();
	}
	
	public void learnCancelEvent(byte[] inventoryItems, boolean canceled){
		observeCount++;
		if(canceled)
			cancelCount++;
		cancelTrigger.update(inventoryItems, canceled);
		if(!Agent.UPLOAD_VERSION && DEBUG){
			System.out.println("Cancel Event: " + canceled);
		}
	}

	public void learnEventHappened(byte[] inventoryItems, byte newItype, boolean move, byte scoreDelta, boolean killed, byte spawnedItype, byte teleportTo, boolean winGame, byte addInventory, byte removeInventory){
		eventCount++;
		if(move && !specialEvent.hasMovedOnce && !defaultEvent.hasMovedOnce){
			//Das erste mal, dass dieses Event als push-Event gesehen wird!
			//Daher muessen bisherige cancels als falsch angesehen werden und als nicht ausfuehrbare push-versuche interpretiert werden!
			eventCount += cancelCount;
			cancelCount = 0;
			cancelTrigger.reset();
			cancelTrigger.update(inventoryItems, false);
		}
		
		//Herausfinden, ob default, oder special event!

		boolean isPropablyDefaultEvent = defaultEvent.hasValues(newItype, move, scoreDelta, killed, spawnedItype, teleportTo, winGame, addInventory, removeInventory);
		if(!isPropablyDefaultEvent){
			//Gucken, ob das inventar sich zu letzten default nicht geaendert hat:
			//TODO: man koennte auch gucken, ob das derzeitige Inventar ein Grenzwert des specialEventTriggers ist...
			/*if(specialEventTrigger.lastInventoryWhereNotOccurred == inventoryItems){
				//Mit diesem Inventar wurde letztens Default ausgeloest!!
				isPropablyDefaultEvent = true;
			}*/
		}
		specialEventTrigger.update(inventoryItems, !isPropablyDefaultEvent);
		if(isPropablyDefaultEvent){
			if(!Agent.UPLOAD_VERSION && DEBUG){
				System.out.println("Learn as Default-Event");
			}
			defaultEvent.update(newItype, move, scoreDelta, killed, spawnedItype, teleportTo, winGame, addInventory, removeInventory);
		}else{
			if(!Agent.UPLOAD_VERSION && DEBUG){
				System.out.println("Learn as Special-Event");
			}
			specialEvent.update(newItype, move, scoreDelta, killed, spawnedItype, teleportTo, winGame, addInventory, removeInventory);
			
		}
	}
	
	public boolean willCancel(byte[] inventoryItems){
		if(observeCount == 0)
			return false;
		else{
			if(cancelCount == observeCount)
				return true;
			else
				return cancelTrigger.willTrigger(inventoryItems);
		}
	}
	
	public boolean willTriggerSpecialEvent(byte[] inventoryItems){
		if(eventCount == observeCount - cancelCount)
			return true;
		else
			return specialEventTrigger.willTrigger(inventoryItems);
	}
	
	public YoloEvent getSpecialEvent(){
		return specialEvent;
	}
	
	public YoloEvent getDefaultEvent() {
		return defaultEvent;
	}
	
	@Override
	public String toString() {
		String retVal =  "############################\nCurrent Knowledge: ";
		
		retVal += "\n\t Special Event Triggering = " + (eventCount == observeCount - cancelCount);
		retVal += "\n\n\t Default Event Description: \n" + defaultEvent.toString();
		retVal += "\n\t Special Event Description: \n" + specialEvent.toString();
		retVal += "###############################";
		
		return retVal;
	}
	
	public short getObserveCount() {
		return observeCount;
	}
	
	public short getCancelCount() {
		return cancelCount;
	}

	public YoloEvent getEvent(byte[] inventoryItems) {
		if(specialEventTrigger.willTrigger(inventoryItems))
			return specialEvent;
		else
			return defaultEvent;
	}

	public void update(byte[] inventory, boolean kill) {
		observeCount++;
		cancelTrigger.update(inventory, false);
		YoloEvent currentlyExpected = getEvent(inventory); 
		if(currentlyExpected.getKill() != kill){
			//Derzeitige annahme ist falsch!
			boolean specialShouldTrigger = currentlyExpected == defaultEvent;
			specialEventTrigger.update(inventory, specialShouldTrigger);
			if(specialShouldTrigger){
				specialEvent.learnKill(kill);
				specialEvent.learnNotWin();
			}else{
				defaultEvent.learnKill(kill);
				defaultEvent.learnNotWin();
			}
		}
	}
	
}
