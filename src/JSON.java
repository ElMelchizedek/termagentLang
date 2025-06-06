import java.io.FileInputStream;
import java.util.*;


enum TokenForm {Definer, Deliminator, ArrayBoundaryBegin, ObjectBoundaryBegin, ArrayBoundaryEnd,
    ObjectBoundaryEnd, String}
class Token {
    private TokenForm form;
    private ArrayList<Character> data;

    public Token(TokenForm form, ArrayList<Character> data) {
        this.form = form;
        this.data = data;
    }
    public Token(TokenForm form) {
        this.form = form;
    }

    public TokenForm getForm() { return form; }
    public ArrayList<Character> getData() { return data; }

    public void setForm(TokenForm form) { this.form = form; }
    public void addToData(Character datum)  { data.add(datum); }
}

interface Vertex {
    void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack);
    String toString(int indent);
}
class JsonArray implements Vertex {
    private ArrayList<Vertex> elements = new ArrayList<>();

    public JsonArray() {}

    public void setElements(ArrayList<Vertex> elements) { this.elements = elements; }
    public ArrayList<Vertex> getElements() { return elements; }

    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {
        if (state_stack.peek() == ParserState.Array) {
            if (token.getForm() == TokenForm.String)  {
                JsonPrimitive new_element = new JsonPrimitive(token.getData());
                assert vertex_stack.peek() != null;
                ((JsonArray) vertex_stack.peek()).getElements().add(new_element);
            }
            else if (token.getForm() == TokenForm.ArrayBoundaryEnd) {
                vertex_stack.pop();
                state_stack.pop();
            }
        }
    }

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

            }
        }
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

    public void parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack) {
    }

    @Override
    public String toString(int indent) {
        char[] array = new char[data.size()];
        for (int i = 0; i < data.size(); i++) {
            array[i] = data.get(i);
        }
        return new String(array);
    }
}

enum ParserState { ObjectExpectingKey, ObjectExpectingValue, Array }

public class JSON {

    static ArrayList<Token> extractTokens(FileInputStream stream, int file_length) throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        Token buffer_token = null;

        for (int i = 0; i < file_length; i++) {
            char next_character = (char)stream.read();

            if ( next_character == ':' ) tokens.add(new Token(TokenForm.Definer));
            else if ( next_character == ',' ) tokens.add(new Token(TokenForm.Deliminator));
            else if ( next_character == '[' ) tokens.add(new Token(TokenForm.ArrayBoundaryBegin));
            else if ( next_character == '{' ) tokens.add(new Token(TokenForm.ObjectBoundaryBegin));
            else if ( next_character == ']' ) tokens.add(new Token(TokenForm.ArrayBoundaryEnd));
            else if ( next_character == '}' ) tokens.add(new Token(TokenForm.ObjectBoundaryEnd));
            else if ( next_character == '"' ) {
                if ( buffer_token == null ) buffer_token = new Token(TokenForm.String, new ArrayList<>());
                else {
                    tokens.add(buffer_token);
                    buffer_token = null;
                }
            }
            else {
                if (buffer_token != null) buffer_token.addToData(next_character);
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
            return generateTree(tokens);

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }
}
