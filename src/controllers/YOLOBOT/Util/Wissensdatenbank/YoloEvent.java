package controllers.YOLOBOT.Util.Wissensdatenbank;



public class YoloEvent extends Event {
	
	/**
	 * Speichert Byte events.
	 * <br>Eintraege gehoeren zu:<br>
	 * <ul>
	 * <li> 0 = itype </li>
	 * <li> 1 = scoreDelta </li>
	 * <li> 2 = spawnedItype </li>
	 * <li> 3 = teleportTo </li>
	 * <li> 4 = addInventory </li>
	 * <li> 5 = removeInventory </li>
	 * </ul>
	 */

	/**
	 * Speichert Boolean events.
	 * <br>Eintraege gehoeren zu:<br>
	 * <ul>
	 * <li> 0 = killed </li>
	 * <li> 1 = winGame </li>
	 * <li> 2 = move </li>
	 * </ul>
	 */
	
	/**
	 * Push wird gesondert von den restlichen Events behandelt:<br>
	 * Wird einmal ein erfolgreicher Push bemerkt, so wird diesers Event immer als pushbar angesehen.<br>
	 * Ob der push tatsaechlich ausfuehrbar ist wird allerdings nicht direkt ermittelt.
	 * Dazu muss rekursiv weiter nachgeforscht werden (ob der zu pushende block da hin kann wo er hin muesste)
	 */
	boolean hasMovedOnce;
	
	public YoloEvent() {
		super(6,3);
		byteEvents[0] = -1;	//Standartmaessig keine IType aenderung!
		byteEvents[3] = -1;	//Standartmaessig kein Teleport!
		byteEvents[4] = -1;	//Standartmaessig keine Inventarerhoehungen
		byteEvents[5] = -1;	//Standartmaessig keine Inventarsenkungen
		
		
		boolEvents[2] = true; 	//Initial wird Bewegen auf ja geschaetzt
		
	}
	
	public void update(byte newItype, boolean move, byte scoreDelta, boolean killed, byte spawnedItype, byte teleportToItype, boolean winGame, byte addInventory, byte removeInventory){
		updateByteEvents(newItype, scoreDelta, spawnedItype, teleportToItype, addInventory, removeInventory);
		updateBoolEvents(killed, winGame, move);

		this.hasMovedOnce |= move;
	}
	
	@Override
	public String toString() {
		String retVal = "Event ist:";
		if(byteEventsPropability[0] > MIN_VALUE && byteEvents[0] != -1)
			retVal += "\t iType change to: " + byteEvents[0];
		if(byteEventsPropability[1] > MIN_VALUE && byteEvents[1] != 0)
			retVal += "\n\t Score Aenderung: " + byteEvents[1];
		if(byteEventsPropability[2] > MIN_VALUE && byteEvents[2] != -1)
			retVal += "\n\t Spawn Object: " + byteEvents[2];
		if(byteEventsPropability[4] > MIN_VALUE && byteEvents[4] != -1)
			retVal += "\n\t Add Inventory: " + byteEvents[2];
		if(byteEventsPropability[5] > MIN_VALUE && byteEvents[5] != -1)
			retVal += "\n\t Remove Inventory: " + byteEvents[2];

		if(boolEventsPropability[0] > MIN_VALUE && boolEvents[0])
			retVal += "\n\t kill this object!";
		if(boolEventsPropability[1] > MIN_VALUE && boolEvents[1])
			retVal += "\n\t Win the game!";
		if(boolEventsPropability[2] > MIN_VALUE && boolEvents[2])
			retVal += "\n\t Move";
		
//		if(!hasMovedOnce)
//			retVal += "\n\t Object will not move!";
		
		
		return retVal;
	}

	public int likelyValue(byte newItype, boolean push, byte scoreDelta,
			boolean killed, byte spawnedItype, byte teleportTo, boolean win, byte addInventory, byte removeInventory) {
		int likely = 0;
		//updateByteEvents(newItype, scoreDelta, spawnedItype);
		//updateBoolEvents(killed);
		
		if(addInventory == byteEvents[4] || byteEventsPropability[4] == MIN_VALUE)
			likely+= 1<<0;
		
		if(removeInventory == byteEvents[5] || byteEventsPropability[5] == MIN_VALUE)
			likely+= 1<<1;
		
		if(scoreDelta == byteEvents[1] || byteEventsPropability[1] == MIN_VALUE)
			likely+= 1<<2;
		
		if(spawnedItype == byteEvents[2] || byteEventsPropability[2] == MIN_VALUE)
			likely+= 1<<3;
		
		if(teleportTo == byteEvents[3] || byteEventsPropability[3] == MIN_VALUE)
			likely+= 1<<4;
		
		if(newItype == byteEvents[0] || byteEventsPropability[0] == MIN_VALUE)
			likely+= 1<<5;
		
		if(push == boolEvents[2] || boolEventsPropability[2] == MIN_VALUE)	//Pushing is very likely
			likely += 1<<6;
		
		if(killed == boolEvents[0] || boolEventsPropability[0] == MIN_VALUE)	//Killing is likely
			likely+= 1<<7;
		
		if(win == boolEvents[1] || boolEventsPropability[1] == MIN_VALUE)	//Winning is very very likely
			likely += 1<<8;
		
		return likely;
	}
	
	public int getRemoveInventorySlotItem(){
		return byteEvents[5];
	}
	
	public int getAddInventorySlotItem(){
		return byteEvents[4];
	}
	
	public int getIType(){
		return byteEvents[0];
	}
	
	public int getScoreDelta(){
		return byteEvents[1];
	}
	
	public int getSpawns(){
		return byteEvents[2];
	}
	
	public int getTeleportTo(){
		return byteEvents[3];
	}
	
	public boolean getKill(){
		return boolEvents[0];
	}
	
	public boolean getMove(){
		return boolEvents[2];
	}
	
	public boolean getWinGame(){
		return boolEvents[1];
	}

	public void learnKill(boolean kill) {
		updateBoolEvent(5,0, kill);
	}

	public void learnNotWin() {
		updateBoolEvent(5,1, false);
	}

	public boolean hasValues(byte newItype, boolean move, byte scoreDelta,
			boolean killed, byte spawnedItype, byte teleportTo,
			boolean winGame, byte addInventory, byte removeInventory) {

		if(addInventory != byteEvents[4] && byteEventsPropability[4] != MIN_VALUE)
			return false;
		
		if(removeInventory != byteEvents[5] && byteEventsPropability[5] != MIN_VALUE)
			return false;
		
		if(scoreDelta != byteEvents[1] && byteEventsPropability[1] != MIN_VALUE)
			return false;
		
		if(spawnedItype != byteEvents[2] && byteEventsPropability[2] != MIN_VALUE)
			return false;
		
		if(teleportTo != byteEvents[3] && byteEventsPropability[3] != MIN_VALUE)
			return false;
		
		if(newItype != byteEvents[0] && byteEventsPropability[0] != MIN_VALUE)
			return false;
		
		if(move != boolEvents[2] && boolEventsPropability[2] != MIN_VALUE)
			return false;
		
		if(killed != boolEvents[0] && boolEventsPropability[0] != MIN_VALUE)
			return false;
		
		if(winGame != boolEvents[1] && boolEventsPropability[1] != MIN_VALUE)
			return false;
		
		return true;
	}
	
}
