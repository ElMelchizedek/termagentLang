import org.junit.Test;
import org.junit.jupiter.api.Nested;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

@Nested
public class TestJSON {
    // Helper methods.
    public static JsonObject readFile(String path) {
        JsonObject root = null;
        try {
            root = (JsonObject) JSON.jsonRead(path);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return root;
    }
    public static ArrayList<Character> stringToCharacterList(String string) {
        ArrayList<Character> characters = new ArrayList<>();
        for (char c : string.toCharArray()) {
            characters.add(c);
        }
        return characters;
    }

    // Unit tests.
    @Test
    public void testJsonPrimitiveString() {
        JsonObject verified_root = new JsonObject();

        verified_root.getProperties().put("\"primitive\"",
                new JsonPrimitive(stringToCharacterList("\"value\"")));

        JsonObject test_root = readFile("data/test_json/primitivestring.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonPrimitiveBoolean() {
        JsonObject verified_root = new JsonObject();

        verified_root.getProperties().put("\"exists\"",
                new JsonBoolean(true));

        JsonObject test_root = readFile("data/test_json/primitiveboolean.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonObjectSimple() {
        JsonObject verified_root = new JsonObject();
        JsonObject parent = new JsonObject();

        parent.getProperties().put("\"child\"",
                new JsonPrimitive(stringToCharacterList("\"value\"")));
        verified_root.getProperties().put("\"parent\"", parent);

        JsonObject test_root = readFile("data/test_json/objectsimple.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonObjectNested() {
        JsonObject verified_root = new JsonObject();
        JsonObject parent = new JsonObject();
        JsonObject child = new JsonObject();

        child.getProperties().put("\"primitive\"",
                new JsonPrimitive(stringToCharacterList("\"value\"")));
        parent.getProperties().put("\"child\"", child);
        verified_root.getProperties().put("\"parent\"", parent);

        JsonObject test_root = readFile("data/test_json/objectnested.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonArraySimple() {
        JsonObject verified_root = new JsonObject();
        JsonArray array = new JsonArray();

        array.getElements().add(new JsonPrimitive(stringToCharacterList("\"alpha\"")));
        array.getElements().add(new JsonPrimitive(stringToCharacterList("\"bravo\"")));
        verified_root.getProperties().put("\"array\"", array);

        JsonObject test_root = readFile("data/test_json/arraysimple.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonArrayNested() {
        JsonObject verified_root = new JsonObject();
        JsonArray array = new JsonArray();
        JsonArray child_1 = new JsonArray();
        JsonArray child_2 = new JsonArray();

        child_1.getElements().add(new JsonPrimitive(stringToCharacterList("\"alpha\"")));
        child_1.getElements().add(new JsonPrimitive(stringToCharacterList("\"bravo\"")));
        child_2.getElements().add(new JsonPrimitive(stringToCharacterList("\"yi\"")));
        child_2.getElements().add(new JsonPrimitive(stringToCharacterList("\"er\"")));
        array.getElements().add(child_1);
        array.getElements().add(child_2);
        verified_root.getProperties().put("\"array\"", array);

        JsonObject test_root = readFile("data/test_json/arraynested.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonObjectInArray() {
        JsonObject verified_root = new JsonObject();
        JsonArray array = new JsonArray();
        JsonObject object = new JsonObject();

        object.getProperties().put("\"key\"",
                new JsonPrimitive(stringToCharacterList("\"value\"")));
        array.getElements().add(object);
        verified_root.getProperties().put("\"array\"", array);

        JsonObject test_root = readFile("data/test_json/objectinarray.json");
        assertEquals(verified_root.toString(0), test_root.toString(0));
    }

}
