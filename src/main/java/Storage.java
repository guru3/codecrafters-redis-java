import java.util.HashMap;
import java.util.Map;


class Storage {

	private final Map<String, String> internalStorage;

	Storage() {
		this.internalStorage = new HashMap<>();
	}

	void set(String key, String value) {
		this.internalStorage.put(key, value);
	}

	String get(String key) {
		return this.internalStorage.get(key);
	}

}