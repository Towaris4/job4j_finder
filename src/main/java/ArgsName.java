import java.util.HashMap;
import java.util.Map;

public class ArgsName {

    private final Map<String, String> values = new HashMap<>();

    public String get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException("This key: '" + key + "' is missing");
        }
        return values.get(key);
    }

    private void parse(String[] args) {
        for (String line : args) {
            int index = line.indexOf("=");
            validate(line, index);
            String key = line.substring(1, index);
            String value = line.substring(index + 1);
            values.put(key, value);
        }
    }

    private static void validate(String line, int index) {
        if (index == -1) {
            throw new IllegalArgumentException("Error: This argument '" + line + "' does not contain an equal sign");
        }
        if (!line.startsWith("-")) {
            throw new IllegalArgumentException("Error: This argument '" + line + "' does not start with a '-' character");
        }
        if (index - 1 <= 0) {
            throw new IllegalArgumentException("Error: This argument '" + line + "' does not contain a key");
        }
        if (index + 1 == line.length()) {
            throw new IllegalArgumentException("Error: This argument '" + line + "' does not contain a value");
        }
    }

    public static ArgsName of(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments not passed to program");
        }
        ArgsName names = new ArgsName();
        names.parse(args);
        return names;
    }
}