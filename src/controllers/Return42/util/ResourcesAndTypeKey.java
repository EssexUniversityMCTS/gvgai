package controllers.Return42.util;

import java.util.Map;

public class ResourcesAndTypeKey extends BiKey<Integer, Integer> {
	public ResourcesAndTypeKey(Map<Integer, Integer> resources, int type) {
		super(resources.hashCode(), type);
	}
}
