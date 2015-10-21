package controllers.Return42.util;

import java.util.Map;

public class MapUtil {

	public static <K, V, D extends V> V getOrCreate(Map<K, V> map, K key, D defaultEntry) {
		V entry = map.get(key);
		if (entry == null) {
			entry = defaultEntry;
			map.put(key, entry);
		}
		return entry;
	}

}
