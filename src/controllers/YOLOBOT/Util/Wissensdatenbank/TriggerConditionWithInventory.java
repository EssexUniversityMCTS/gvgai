package controllers.YOLOBOT.Util.Wissensdatenbank;

public class TriggerConditionWithInventory {

	private enum TriggerType{
		conditional,
		ever,
		never;
	}

	private boolean occurredOnce, notOccurredOnce;
	protected byte[] minOccurred;
	protected byte[] maxOccurred;
	protected byte[] minNotOccurred;
	protected byte[] maxNotOccurred;

	protected boolean[] isIrrelevant;
	private TriggerType trigger;
	private byte irrelevantCount;
	private byte irrelevantEverProbybility;
	
	public TriggerConditionWithInventory() {
		reset();
	}
	
	public double getTriggerPropability(byte[] inventoryItems){
		byte occur = 0;
		for (int index = 0; index < inventoryItems.length; index++) {
			if(minOccurred[index] <= inventoryItems[index] && inventoryItems[index] <= maxOccurred[index]){ //XOR
				occur++;
			}else{
				occur--;
			}
		}
		return (double)occur/inventoryItems.length;
	}
	
	public boolean willTrigger(byte[] inventoryItems){
		switch (trigger) {
		case conditional:
			return getConditionalTrigger(inventoryItems);
		default:
			return trigger == TriggerType.ever;
		}
		
	}
	
	private boolean getConditionalTrigger(byte[] inventoryItems) {
		for (int index = 0; index < inventoryItems.length; index++) {
			if(!isIrrelevant[index]){
				if(minNotOccurred[index] <= inventoryItems[index] && inventoryItems[index] <= maxNotOccurred[index]){
					return false;
				}
			}
		}
		return true;
	}

	public void update(byte[] inventoryItems, boolean occurred){
		if(trigger != TriggerType.conditional){
			if((occurred && trigger == TriggerType.ever)||(!occurred && trigger == TriggerType.never)){
				if(irrelevantEverProbybility < 5)
					irrelevantEverProbybility++;
			}else if(irrelevantEverProbybility > -5){
				irrelevantEverProbybility--;
			}else{
				trigger = occurred?TriggerType.ever:TriggerType.never;
			}
		}
		if(irrelevantCount == inventoryItems.length)
			return;
		for (int itemIndex = 0; itemIndex < inventoryItems.length; itemIndex++) {
			byte inventoryCount = inventoryItems[itemIndex];
			
			if(isIrrelevant[itemIndex])
				continue;
			
			if(occurred){
				occurredOnce = true;
				minOccurred[itemIndex] = (byte) Math.min(minOccurred[itemIndex], inventoryCount);
				maxOccurred[itemIndex] = (byte) Math.max(maxOccurred[itemIndex], inventoryCount);
			}else{
				notOccurredOnce = true;
				minNotOccurred[itemIndex] = (byte) Math.min(minNotOccurred[itemIndex], inventoryCount);
				maxNotOccurred[itemIndex] = (byte) Math.max(maxNotOccurred[itemIndex], inventoryCount);
			}
			

			if(occurredOnce && notOccurredOnce && (minOccurred[itemIndex] <= minNotOccurred[itemIndex] && minNotOccurred[itemIndex] <= maxOccurred[itemIndex] || minOccurred[itemIndex] <= maxNotOccurred[itemIndex] && maxNotOccurred[itemIndex] <= maxOccurred[itemIndex])){
				isIrrelevant[itemIndex] = true;
				irrelevantCount++;
				if(irrelevantCount == inventoryItems.length){
					if(occurred)
						trigger = TriggerType.ever;
					else
						trigger = TriggerType.never;
				}
			}
			
		}
	}

	public void reset() {
		// Resettet das gelernte Wissen.
		occurredOnce = false;
		notOccurredOnce = false;
		minOccurred = new byte[YoloKnowledge.INDEX_MAX];
		maxOccurred = new byte[YoloKnowledge.INDEX_MAX];
		minNotOccurred = new byte[YoloKnowledge.INDEX_MAX];
		maxNotOccurred = new byte[YoloKnowledge.INDEX_MAX];
		
		for (int i = 0; i < minOccurred.length; i++) {
			minOccurred[i] = Byte.MAX_VALUE;
			minNotOccurred[i] = Byte.MAX_VALUE;
		}
		
		isIrrelevant = new boolean[YoloKnowledge.INDEX_MAX];

		trigger = TriggerType.conditional;
		irrelevantCount = 0;
		irrelevantEverProbybility = 0;
	}
	
	
	
}
