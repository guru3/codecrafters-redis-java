import java.time.Duration;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.Map;


class ValueWithExpiry {
	String value;
	LocalDateTime expiryTime;

	ValueWithExpiry(String value, LocalDateTime expiryTime) {
		this.value = value;
		this.expiryTime = expiryTime;
	}

	boolean isExpired() {
		return this.expiryTime.isBefore(LocalDateTime.now());
	}
}


class Value {
	String value;
	boolean isValid;

	Value(String value, boolean isValid) {
		this.value = value;
		this.isValid = isValid;
	}
}


class Storage {

	private final Map<String, ValueWithExpiry> internalStorage;
	
	private static final Value INVALID_VALUE = new Value("", false);

	Storage() {
		this.internalStorage = new HashMap<>();
	}

	void set(String key, String value) {
		this.internalStorage.put(key, new ValueWithExpiry(value, LocalDateTime.MAX));
	}

	Value get(String key) {
		ValueWithExpiry valueWithExpiry = this.internalStorage.get(key);
		if( valueWithExpiry == null ) {
			return INVALID_VALUE;
		}

		if( valueWithExpiry.isExpired() ) {
			this.internalStorage.remove(key);
			return INVALID_VALUE;
		}

		return new Value(valueWithExpiry.value, true);
	}

	void setWithExpiry(String key, String value, Duration time) {
		LocalDateTime expiryTime = LocalDateTime.now();
		ValueWithExpiry valueWithExpiry = new ValueWithExpiry(value, expiryTime.plus(time));
		this.internalStorage.put(key, valueWithExpiry);
	}

}