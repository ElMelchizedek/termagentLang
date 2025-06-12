import org.junit.Test;
import org.junit.jupiter.api.Nested;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

@Nested
public class TestJSON {
    // Helper methods.
    public static <T> T readFile(String path, Class<T> target_class) {
        try {
            T instance = target_class.getDeclaredConstructor().newInstance();
            instance = JSON.jsonRead(path, target_class);
            return instance;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
    public static ArrayList<Character> stringToCharacterList(String string) {
        ArrayList<Character> characters = new ArrayList<>();
        for (char c : string.toCharArray()) {
            characters.add(c);
        }
        return characters;
    }

    // Classes used in tests.
    static class PrimitiveString {
        public String primitive;
    }

    static class PrimitiveBoolean {
        public boolean exists = true;
    }

    static class ObjectSimple {
        public Parent parent;

        public static class Parent {
            public String child;
        }
    }

    static class ObjectNested {
        public Parent parent;

        public static class Parent {
            public Child child;

            public static class Child {
                public String primitive;
            }
        }
    }

    static class ArraySimple {
        public ArrayList<String> array;
    }

    static class ArrayNested {
        public ArrayList<ArrayList<String>> array;
    }

    static class ObjectInArray {
        public ArrayList<Item> array;

        public static class Item {
            public String key;
        }
    }

    // Unit tests.
    @Test
    public void testJsonPrimitiveString() {
        PrimitiveString verified_instance = new PrimitiveString();
        verified_instance.primitive = "value";

        PrimitiveString test_instance = readFile("data/test_json/primitivestring.json", PrimitiveString.class);

        assertNotNull(test_instance);
        assertNotNull(test_instance.primitive);
        assertEquals("value", test_instance.primitive);
        assertEquals(verified_instance.primitive, test_instance.primitive);
    }
    @Test
    public void testJsonPrimitiveBoolean() {
        PrimitiveBoolean verified_instance = new PrimitiveBoolean();
        verified_instance.exists = true;

        PrimitiveBoolean test_instance = readFile("data/test_json/primitiveboolean.json", PrimitiveBoolean.class);

        assertNotNull(test_instance);
        assertNotNull(test_instance.exists);
        assertEquals(true, test_instance.exists);
        assertEquals(verified_instance.exists, test_instance.exists);
    }

    @Test
    public void testJsonObjectSimple() {
        ObjectSimple verified_instance = new ObjectSimple();
        verified_instance.parent = new ObjectSimple.Parent();
        verified_instance.parent.child = "value";

        ObjectSimple test_instance = readFile("data/test_json/objectsimple.json", ObjectSimple.class);

        assertNotNull(test_instance);
        assertNotNull(test_instance.parent);
        assertNotNull(test_instance.parent.child);
        assertEquals("value", test_instance.parent.child);
        assertEquals(verified_instance.parent.child, test_instance.parent.child);
        assertEquals(verified_instance.parent, test_instance.parent);
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

//        JsonObject test_root = readFile("data/test_json/objectnested.json");
//        assertEquals(verified_root.toString(0), test_root.toString(0));
    }
    @Test
    public void testJsonArraySimple() {
        JsonObject verified_root = new JsonObject();
        JsonArray array = new JsonArray();

        array.getElements().add(new JsonPrimitive(stringToCharacterList("\"alpha\"")));
        array.getElements().add(new JsonPrimitive(stringToCharacterList("\"bravo\"")));
        verified_root.getProperties().put("\"array\"", array);

//        JsonObject test_root = readFile("data/test_json/arraysimple.json");
//        assertEquals(verified_root.toString(0), test_root.toString(0));
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

//        JsonObject test_root = readFile("data/test_json/arraynested.json");
//        assertEquals(verified_root.toString(0), test_root.toString(0));
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

//        JsonObject test_root = readFile("data/test_json/objectinarray.json");
//        assertEquals(verified_root.toString(0), test_root.toString(0));
    }

}
