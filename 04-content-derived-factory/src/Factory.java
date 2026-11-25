// 04-content-derived-factory/src/Factory.java
// A content-derived factory. Fill in create() and derive(). Plain Java, no framework.
// Compile and run: javac src/Factory.java && java -cp src Factory
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Factory {
    // An item: the content it was built from, and an id derived from that content.
    record Item(Map<String, String> content, String id) {}

    // Return an Item holding the content and an id derived from its content (use derive).
    static Item create(Map<String, String> content) {
        return new Item(new LinkedHashMap<>(content), derive(content));
    }

    // Recompute the id from the content alone, with no registry and no shared state,
    // so the demo passes.
    static String derive(Map<String, String> content) {
        try {
            List<String> sortedKeys = new ArrayList<>(content.keySet());
            Collections.sort(sortedKeys);
            StringBuilder sb = new StringBuilder();
            for (String key : sortedKeys) {
                sb.append(key).append(":").append(content.get(key)).append("\n");
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws Exception {
        Map<String, String> content = new LinkedHashMap<>();
        content.put("title", "Quarterly report");
        content.put("body", "Revenue held steady.");
        content.put("author", "avery");

        Item item = create(content);

        // Second party holds only the content, in a different field order, and derives the id itself.
        Map<String, String> sameContent = new LinkedHashMap<>();
        sameContent.put("author", "avery");
        sameContent.put("body", "Revenue held steady.");
        sameContent.put("title", "Quarterly report");
        if (!derive(sameContent).equals(item.id())) {
            throw new AssertionError("same content must derive the same id");
        }

        // Tamper with one field. The derived id must no longer match the original.
        Map<String, String> tampered = new LinkedHashMap<>(content);
        tampered.put("body", "Revenue tripled.");
        if (derive(tampered).equals(item.id())) {
            throw new AssertionError("changed content must change the id");
        }

        System.out.println("ok: " + item.id());
    }
}
