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
    String parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack, String current_key);
}
class JsonArray implements Vertex {
    private ArrayList<Token> elements = new ArrayList<>();

    public JsonArray() {}

    public void setElements(ArrayList<Token> elements) { this.elements = elements; }
    public ArrayList<Token> getElements() { return elements; }

    public String parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack, String current_key) {
        return current_key;
    }
}
class JsonObject implements Vertex {
    private Map<String, Vertex> properties = new HashMap<>();

    public JsonObject() {};

    public void setProperties(Map<String, Vertex> properties) { this.properties = properties; }
    public Map<String, Vertex> getProperties() { return properties; }

    public String parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack, String current_key) {
        if (token.getForm() == TokenForm.ObjectBoundaryBegin) {
            JsonObject new_property = new JsonObject();
            ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);
            vertex_stack.push(new_property);
            state_stack.push(ParserState.Object);
        } else if (token.getForm() == TokenForm.ObjectBoundaryEnd) {
            vertex_stack.pop();
            state_stack.pop();
            return null;
        } else if (token.getForm() == TokenForm.String) {
            JsonPrimitive new_property = new JsonPrimitive(token.getData());
            ((JsonObject) vertex_stack.peek()).getProperties().put(current_key, new_property);
        } else if (token.getForm() == TokenForm.Deliminator) {
            return null;
        }

        return current_key;
    }

}
class JsonPrimitive implements Vertex {
    private ArrayList<Character> data;

    public JsonPrimitive(ArrayList<Character> data) { this.data = data; }
    public JsonPrimitive() {}

    public void setData(ArrayList<Character> data) { this.data = data; }
    public ArrayList<Character> getData() { return data; }

    public String parse(Token token, Deque<Vertex> vertex_stack, Deque<ParserState> state_stack, String current_key) {
        return current_key;
    }
}

enum ParserState { Object, Key, Value, }

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

        if (tokens == null) throw new NullPointerException("Parsed tokens ArrayList returned null.");
        else return tokens;

    }

    static JsonObject generateTree(ArrayList<Token> tokens) throws Exception {
        Deque<Vertex> vertex_stack = new ArrayDeque<>();
        Deque<ParserState> state_stack = new ArrayDeque<>();
        // Create root vertex and top of state stack.
        vertex_stack.push(new JsonObject());
        state_stack.push(ParserState.Object);
        // State variables.
        String current_key = null;
        boolean parse_value = false;

        for (Token token: tokens) {
            if (parse_value == true) {
                vertex_stack.peek().parse(token, vertex_stack, state_stack, current_key);
                parse_value = false;
                current_key = null;
            }
            else if (state_stack.peek() == ParserState.Object) {
                if (token.getForm() == TokenForm.String && current_key == null) {
                    StringBuilder builder = new StringBuilder(token.getData().size());
                    for (Character character : token.getData()) builder.append(character);
                    current_key = builder.toString();
                }
                if (current_key != null) {
                    if (token.getForm() == TokenForm.Definer) {
                        parse_value = true;
                    }
                }
            }
        }

        // Returns the root vertex.
        return (JsonObject) vertex_stack.getLast();
    }

    public static Vertex jsonRead(String path) throws Exception {
        FileInputStream stream = null;
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

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

}
