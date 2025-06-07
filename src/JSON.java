import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


enum TokenForm {Definer, Deliminator, ArrayBoundaryBegin, ObjectBoundaryBegin, ArrayBoundaryEnd,
    ObjectBoundaryEnd, String, Integer, Boolean}

class Token {
    private TokenForm form;
    // Can't be fucked to implement composition with a Token interface to delineate integers, booleans, and others.
    private ArrayList<Character> data;
    private int value;
    private boolean status;

    public Token(TokenForm form, ArrayList<Character> data) {
        this.form = form;
        this.data = data;
    }
    public Token(TokenForm form, int value) {
        this.form = form;
        this.value = value;
    }
    public Token(TokenForm form, boolean status) {
        this.form = form;
        this.status = status;
    }
    public Token(TokenForm form) {
        this.form = form;
    }

    public TokenForm getForm() { return form; }
    public ArrayList<Character> getData() { return data; }
    public int getValue() { return value; }
    public boolean getStatus() { return status; }

    public void setForm(TokenForm form) { this.form = form; }
    public void addToData(Character datum)  { data.add(datum); }
}

interface Vertex {
    void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack);
    boolean validate();
    String toString(int indent);
}
class JsonArray implements Vertex {
    private ArrayList<Vertex> elements = new ArrayList<>();

    public JsonArray() {}

    public void setElements(ArrayList<Vertex> elements) { this.elements = elements; }
    public ArrayList<Vertex> getElements() { return elements; }

    @Override
    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {
        if (state_stack.peek() == ParserState.Array) {
            if (token.getForm() == TokenForm.String)  {
                JsonPrimitive new_element = new JsonPrimitive(token.getData());
                assert vertex_stack.peek() != null;
                ((JsonArray) vertex_stack.peek()).getElements().add(new_element);
            }
            else if (token.getForm() == TokenForm.ArrayBoundaryBegin) {
                JsonArray new_element = new JsonArray();
                assert vertex_stack.peek() != null;
                ((JsonArray) vertex_stack.peek()).getElements().add(new_element);
                vertex_stack.push(new_element);
                state_stack.push(ParserState.Array);
            }
            else if (token.getForm() == TokenForm.ArrayBoundaryEnd) {
                vertex_stack.pop();
                state_stack.pop();
            }
            else if (token.getForm() == TokenForm.ObjectBoundaryBegin) {
                JsonObject new_element = new JsonObject();
                assert vertex_stack.peek() != null;
                ((JsonArray) vertex_stack.peek()).getElements().add(new_element);
                vertex_stack.push(new_element);
                state_stack.push(ParserState.ObjectExpectingKey);
            }
        }
    }

    @Override
    public boolean validate() { return true; }

    @Override
    public String toString(int indent) {
        StringBuilder builder = new StringBuilder("[\n");
        String spaces = "  ".repeat(indent + 1);

        for (Vertex element : elements) {
            builder.append(spaces)
                    .append(element.toString(indent + 1))
                    .append(",\n");
        }

        if (!elements.isEmpty()) {
            builder.setLength(builder.length() - 2);
        }

        return builder.append("\n")
                .append(". ".repeat(indent))
                .append("]")
                .toString();
    }
}
class JsonObject implements Vertex {
    private Map<String, Vertex> properties = new HashMap<>();
    private String current_key;

    public JsonObject() {}

    public void setProperties(Map<String, Vertex> properties) { this.properties = properties; }
    public Map<String, Vertex> getProperties() { return properties; }

    @Override
    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {
        if (state_stack.peek() == ParserState.ObjectExpectingKey) {
            if (token.getForm() == TokenForm.String) {
                StringBuilder builder = new StringBuilder(token.getData().size());
                for (Character character : token.getData()) builder.append(character);
                current_key = builder.toString();
                state_stack.pop();
                state_stack.push(ParserState.ObjectExpectingValue);

            }

        } else if (state_stack.peek() == ParserState.ObjectExpectingValue) {
            if (token.getForm() == TokenForm.ObjectBoundaryBegin) {
                JsonObject new_property = new JsonObject();
                assert vertex_stack.peek() != null;
                ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);
                vertex_stack.push(new_property);
                state_stack.push(ParserState.ObjectExpectingKey);

            } else if (token.getForm() == TokenForm.ObjectBoundaryEnd) {
                vertex_stack.pop();
                state_stack.pop();

            } else if (token.getForm() == TokenForm.String) {
                JsonPrimitive new_property = new JsonPrimitive(token.getData());
                assert vertex_stack.peek() != null;
                ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);

            } else if (token.getForm() == TokenForm.Deliminator) {
                state_stack.pop();
                state_stack.push(ParserState.ObjectExpectingKey);

            } else if (token.getForm() == TokenForm.ArrayBoundaryBegin) {
                JsonArray new_property = new JsonArray();
                assert vertex_stack.peek() != null;
                ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);
                vertex_stack.push(new_property);
                state_stack.push(ParserState.Array);

            } else if (token.getForm() == TokenForm.Integer) {
                JsonInteger new_property = new JsonInteger(token.getValue());
                assert vertex_stack.peek() != null;
                ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);

            } else if (token.getForm() == TokenForm.Boolean) {
                JsonBoolean new_property = new JsonBoolean(token.getStatus());
                assert vertex_stack.peek() != null;
                ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);
            }
        }
    }

    @Override
    public boolean validate() {
        for (Map.Entry<String, Vertex> entry : properties.entrySet()) {
            if (entry.getValue() instanceof JsonPrimitive) {
                JsonPrimitive vertex = (JsonPrimitive) entry.getValue();

                StringBuilder builder = new StringBuilder(vertex.getData().size());
                for (Character character : vertex.getData()) {
                    builder.append(character);
                }
                String data = builder.toString();

//                if (data.equals("true")) {
//                    properties.replace(entry.getKey(), new JsonBoolean(true));
//                }
//                else if (data.equals("false")) {
//                    properties.replace(entry.getKey(), new JsonBoolean(false));
//                }
//                try {
//                    int value = Integer.parseInt(data);
//                    properties.replace(entry.getKey(), new JsonInteger(value));
//                } catch  (Exception e) {}
            }
            else if (entry.getValue() instanceof JsonObject) {
                return entry.getValue().validate();
            }
        }

        return true;
    }

    @Override
    public String toString(int indent) {
        StringBuilder builder = new StringBuilder("{\n");
        String spaces = "  ".repeat(indent + 1);

        for (Map.Entry<String, Vertex> entry : properties.entrySet()) {
            builder.append(spaces)
                    .append('"')
                    .append(entry.getKey())
                    .append("\": ")
                    .append(entry.getValue().toString(indent + 1))
                    .append(",\n");
        }

        if (!properties.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove trailing ",\n".
        }

        return builder.append("\n")
                .append("  ".repeat(indent))
                .append("}")
                .toString();
    }

    // For testing
}
class JsonPrimitive implements Vertex {
    private ArrayList<Character> data;

    public JsonPrimitive(ArrayList<Character> data) { this.data = data; }
    public JsonPrimitive() {}

    public void setData(ArrayList<Character> data) { this.data = data; }
    public ArrayList<Character> getData() { return data; }

    @Override
    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {}

    @Override
    public boolean validate() { return true; }

    @Override
    public String toString(int indent) {
        char[] array = new char[data.size()];
        for (int i = 0; i < data.size(); i++) {
            array[i] = data.get(i);
        }
        return new String(array);
    }
}
class JsonInteger implements Vertex {
    private int value;

    public JsonInteger(int value) { this.value = value; }
    public JsonInteger() {}

    public void setValue(int data) { this.value = value; }
    public int getValue() { return value; }

    @Override
    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {}

    @Override
    public boolean validate() { return true; }

    @Override
    public String toString(int indent) {
        return Integer.toString(value);
    }
}
class JsonBoolean implements Vertex {
    private boolean value;

    public JsonBoolean(boolean value) { this.value = value; }
    public JsonBoolean() {}

    @Override
    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {}

    @Override
    public boolean validate() { return true; }

    @Override
    public String toString(int indent) {
        return Boolean.toString(value);
    }
}

enum ParserState { ObjectExpectingKey, ObjectExpectingValue, Array }

public class JSON {

    static ArrayList<Token> extractTokens(FileInputStream stream, int file_length) throws Exception {
        // First we simply turn the file into an ArrayList of Strings, using regular expressions.
        byte[] byte_data = new byte[stream.available()];
        stream.read(byte_data);
        String string_data = new String(byte_data);

        ArrayList<String> raw_tokens = new ArrayList<>();
        // Regex pattern to match JSON tokens:
        // - Punctuation: { } [ ] : ,
        // - Strings: "..."
        // - Numbers: 123, -45.67, 1.2e3
        // - Booleans: true, false
        // - Null: null
        Pattern pattern = Pattern.compile(
                        "\"(?:\\\\.|[^\"\\\\])*\"|" +  // Strings (with escaped quotes)
                        "[-+]?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?|" +  // Numbers
                        "true|false|null|" +  // Booleans and null
                        "[{}\\[\\],:]"  // Punctuation
        );
        Matcher matcher = pattern.matcher(string_data);
        while (matcher.find()) {
            raw_tokens.add(matcher.group());
        }

        // Now we actually extract the equivalent Token instances from the String list.
        ArrayList<Token> tokens = new ArrayList<>();
        for (String string : raw_tokens) {
            if (string.equals(":")) tokens.add(new Token(TokenForm.Definer));
            else if (string.equals(",")) tokens.add(new Token(TokenForm.Deliminator));
            else if (string.equals("[")) tokens.add(new Token(TokenForm.ArrayBoundaryBegin));
            else if (string.equals("]")) tokens.add(new Token(TokenForm.ArrayBoundaryEnd));
            else if (string.equals("{")) tokens.add(new Token(TokenForm.ObjectBoundaryBegin));
            else if (string.equals("}")) tokens.add(new Token(TokenForm.ObjectBoundaryEnd));
            else if (string.startsWith("\"") && string.endsWith("\"")) {
                ArrayList<Character> characters = new ArrayList<>();
                for (char c : string.toCharArray()) {
                    characters.add(c);
                }
                tokens.add(new Token(TokenForm.String, characters));
            }
            else if (string.chars().allMatch(Character::isDigit)) {
                int value = Integer.parseInt(string);
                tokens.add(new Token(TokenForm.Integer, value));
            }
            else if (string.equals("true") || string.equals("false")) {
                boolean status = Boolean.parseBoolean(string);
                tokens.add(new Token(TokenForm.Boolean, status));
            }
        }

        return tokens;

    }

    static JsonObject generateTree(ArrayList<Token> tokens) {
        Deque<Vertex> vertex_stack = new ArrayDeque<>();
        Deque<ParserState> state_stack = new ArrayDeque<>();
        // Create root vertex and top of state stack.
        vertex_stack.push(new JsonObject());
        state_stack.push(ParserState.ObjectExpectingKey);

        for (Token token: tokens) {
            assert vertex_stack.peek() != null;
            vertex_stack.peek().parse(token, vertex_stack, state_stack);
        }

        // Returns the root vertex.
        return (JsonObject) vertex_stack.getLast();
    }

    static boolean validateTree(JsonObject root) {
        return true;
    }

    public static Vertex jsonRead(String path) {
        FileInputStream stream;
        try {
            // Load the file into memory.
            stream = new FileInputStream(path);
            int file_length = stream.available();

            // Organise raw Data into a flat array of lexical elements, with form of Data specified.
            ArrayList<Token> tokens = extractTokens(stream, file_length);
            // We chop off the first and last tokens since they'll always be "{" or "}" and are unnecessary in our case.
            if (    tokens.getFirst().getForm() != TokenForm.ObjectBoundaryBegin
                    || tokens.getLast().getForm() != TokenForm.ObjectBoundaryEnd ) {
                throw new Exception("JSON Data is not begun or ended by ObjectBoundary token.");
            }
            tokens.removeFirst();
            tokens.removeLast();

            // Build tree out of the Data from the tokens list.
            // We only require the Root vertex to have the whole tree.
            JsonObject root = generateTree(tokens);
            return root;

            // Now we validate the tree to make sure there's no errors in the JSON, and to get the booleans and integers.


        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }
}
