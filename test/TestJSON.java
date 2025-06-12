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
    }

    @Test
    public void testJsonObjectNested() {
        ObjectNested verified_instance = new ObjectNested();
        verified_instance.parent = new ObjectNested.Parent();
        verified_instance.parent.child = new ObjectNested.Parent.Child();
        verified_instance.parent.child.primitive = "value";

        ObjectNested test_instance = readFile("data/test_json/objectnested.json", ObjectNested.class);

        assertNotNull(test_instance);
        assertNotNull(test_instance.parent);
        assertNotNull(test_instance.parent.child);
        assertNotNull(test_instance.parent.child.primitive);
        assertEquals("value", test_instance.parent.child.primitive);
        assertEquals(verified_instance.parent.child.primitive, test_instance.parent.child.primitive);
    }
    @Test
    public void testJsonArraySimple() {
        ArraySimple verified_instance = new ArraySimple();
        verified_instance.array = new ArrayList<>();
        verified_instance.array.add("alpha");
        verified_instance.array.add("bravo");

        ArraySimple test_instance = readFile("data/test_json/arraysimple.json", ArraySimple.class);

        assertNotNull(test_instance);
        assertNotNull(test_instance.array);
        assertEquals("alpha", test_instance.array.get(0));
        assertEquals("bravo", test_instance.array.get(1));
        for (int i = 0; i < verified_instance.array.size(); i++) {
            assertEquals(verified_instance.array.get(i), test_instance.array.get(i));
        }
    }
    @Test
    public void testJsonArrayNested() {
        ArrayNested verified_instance = new ArrayNested();
        verified_instance.array = new ArrayList<>();
        ArrayList<String> first_array = new ArrayList<>();
        first_array.add("alpha");
        first_array.add("bravo");
        ArrayList<String> second_array = new ArrayList<>();
        second_array.add("yi");
        second_array.add("er");
        verified_instance.array.add(first_array);
        verified_instance.array.add(second_array);

        ArrayNested test_instance = readFile("data/test_json/arraynested.json", ArrayNested.class);

        assertNotNull(test_instance);
        assertNotNull(test_instance.array);
        assertEquals("alpha", test_instance.array.get(0).get(0));
        assertEquals("bravo", test_instance.array.get(0).get(1));
        assertEquals("yi", test_instance.array.get(1).get(0));
        assertEquals("er", test_instance.array.get(1).get(1));
        for (int i = 0; i < verified_instance.array.size(); i++) {
            for (int j = 0; j < verified_instance.array.get(i).size(); j++) {
                assertEquals(verified_instance.array.get(i).get(j), test_instance.array.get(i).get(j));
            }
        }
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
