import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

enum JSONTokenForm {Definer, Deliminator, ArrayBoundary, ObjectBoundary, String}
enum JSONVertexForm {Array, Object, Primitive, Root}

class JSONToken {
    private JSONTokenForm form;
    private ArrayList<Character> data;

    public JSONToken(JSONTokenForm form, ArrayList<Character> data) {
        this.form = form;
        this.data = data;
    }
    public JSONToken(JSONTokenForm form) {
        this.form = form;
    }

    public JSONTokenForm getForm() { return form; }
    public ArrayList<Character> getData() { return data; }

    public void setForm(JSONTokenForm form) { this.form = form; }
    public void addToData(Character datum)  { data.add(datum); }
}

class Vertex {
    private Vertex Parent;
    private ArrayList<Vertex> Children = new ArrayList<>();
    private ArrayList<Character> Data = new ArrayList<>();
    private String Label;
    private JSONVertexForm Form;

    public Vertex(String label, Vertex parent, JSONVertexForm form, ArrayList<Character> data) {
        this.Label = label;
        this.Parent = parent;
        this.Form = form;
        this.Data = data;
    }
    public Vertex(String label, Vertex parent, JSONVertexForm form) {
        this.Label = label;
        this.Parent = parent;
        this.Form = form;
    }
    public Vertex(String label, Vertex parent) {
        this.Label = label;
        this.Parent = parent;
    }

    public Vertex getParent() { return Parent; }
    public ArrayList<Vertex> getChildren() { return Children; }
    public ArrayList<Character> getData() { return Data; }
    public String getLabel() { return Label; }
    public JSONVertexForm getForm() { return Form; }

    public void setForm(JSONVertexForm form) { this.Form = form; }
    public void setData(ArrayList<Character> data) { this.Data = data; }
    public void addChild(Vertex child) throws Exception {
        if (child.getParent() != this) throw new Exception("Child to be added to Vertex does not have it as declared Parent.");
        Children.add(child);
    }

}

class JSON {

    static ArrayList<JSONToken> extractTokens(FileInputStream stream, int file_length) throws Exception {
        ArrayList<JSONToken> tokens = new ArrayList<>();
        JSONToken buffer_token = null;

        for (int i = 0; i < file_length; i++) {
            char next_character = (char)stream.read();

            if ( next_character == ':' ) tokens.add(new JSONToken(JSONTokenForm.Definer));
            else if ( next_character == ',' ) tokens.add(new JSONToken(JSONTokenForm.Deliminator));
            else if ( next_character == '[' || next_character == ']' ) tokens.add(new JSONToken(JSONTokenForm.ArrayBoundary));
            else if ( next_character == '{' || next_character == '}' ) tokens.add(new JSONToken(JSONTokenForm.ObjectBoundary));
            else if ( next_character == '"' ) {
                if ( buffer_token == null ) buffer_token = new JSONToken(JSONTokenForm.String, new ArrayList<>());
                else {
                    tokens.add(buffer_token);
                    buffer_token = null;
                }
            }

            else {
                if (buffer_token == null) throw new NullPointerException("Attempted to read into null buffer token.");
                else buffer_token.addToData(next_character);
            }
        }

        if (tokens == null) throw new NullPointerException("Parsed tokens ArrayList returned null.");
        else return tokens;

    }

    static Vertex generateTree(ArrayList<JSONToken> tokens) throws Exception {
        Vertex root = new Vertex("root", null, JSONVertexForm.Root);
        Vertex current_vertex = root;

        for (int i = 0; i < tokens.size(); i++) {
            JSONToken previous_token = (i != 0) ? tokens.get(i - 1) : null;
            JSONToken current_token = tokens.get(i);
            JSONToken next_token = (i != tokens.size() - 1) ? tokens.get(i + 1) : null;

            // LABELED STUFF
            // Discover new sub-node.
            if ( current_token.getForm() == JSONTokenForm.String && next_token.getForm() == JSONTokenForm.Definer ) {
                // Have to go through a long-winded process of constructing a string due to the nature of an
                // ArrayList<Character>.
                StringBuilder builder = new StringBuilder(current_token.getData().size());
                for (Character character : current_token.getData()) builder.append(character);
                String name = builder.toString();

                Vertex new_vertex = new Vertex(name, current_vertex);
                current_vertex.addChild(new_vertex);
                current_vertex = new_vertex;

                // Iterate again to jump over the Definer token.
                i++;
            }
            // Determine new sub-node contains an array.
            else if ( current_vertex.getForm() == null && current_token.getForm() == JSONTokenForm.ArrayBoundary ) {
                current_vertex.setForm(JSONVertexForm.Array);
            }
            // Determine new sub-node contains an object.
            else if ( current_vertex.getForm() == null && current_token.getForm() == JSONTokenForm.ObjectBoundary ) {
                current_vertex.setForm(JSONVertexForm.Object);
            }
            // Determine new sub-node contains primitive data.
            else if ( current_vertex.getForm() == null && current_token.getForm() == JSONTokenForm.String) {
                current_vertex.setForm(JSONVertexForm.Primitive);
                current_vertex.setData(current_token.getData());
                current_vertex = current_vertex.getParent();
            }

            // UNLABELED STUFF
            // Check if token represents new primitive element for array.
            else if ( current_vertex.getForm() == JSONVertexForm.Array && current_token.getForm() == JSONTokenForm.String ) {
                String iterative_label = String.valueOf(current_vertex.getChildren().size());
                Vertex new_element = new Vertex(
                        iterative_label, current_vertex, JSONVertexForm.Primitive, current_token.getData());
                try {
                    current_vertex.addChild(new_element);
                } catch ( Exception e) { throw e; }
            }
            // Check if token represents new array element for current array vertex.
            else if ( current_vertex.getForm() == JSONVertexForm.Array && current_token.getForm() == JSONTokenForm.ArrayBoundary ) {
                String iterative_label = String.valueOf(current_vertex.getChildren().size());
                Vertex new_element = new Vertex(iterative_label, current_vertex, JSONVertexForm.Array);
                try {
                    current_vertex.addChild(new_element);
                } catch ( Exception e ) { throw e; }
                current_vertex = new_element;
            }
            // Check if token represents new object element for current array vertex.
            else if ( current_vertex.getForm() == JSONVertexForm.Array && current_token.getForm() == JSONTokenForm.ObjectBoundary ) {
                String iterative_label = String.valueOf(current_vertex.getChildren().size());
                Vertex new_element = new Vertex(iterative_label, current_vertex, JSONVertexForm.Object);
                try {
                    current_vertex.addChild(new_element);
                } catch ( Exception e ) { throw e; }
                current_vertex = new_element;
            }

            // ENDING BOUNDARIES
            else if ( current_token.getForm() == JSONTokenForm.Deliminator && previous_token.getForm() != JSONTokenForm.Definer ) {
                current_vertex = current_vertex.getParent();
            }
        }

        if (root.getChildren().size() == 0) throw new Exception("Tree generated has no meaningful vertices.");
        else return root;
    }

    public static void readJson(String path) throws Exception {
        FileInputStream stream = null;
        try {
            // Load the file into memory.
            stream = new FileInputStream(path);
            int file_length = stream.available();

            // Organise raw Data into a flat array of lexical elements, with form of Data specified.
            ArrayList<JSONToken> tokens = extractTokens(stream, file_length);
            // We chop off the first and last tokens since they'll always be "{" or "}" and are unnecessary in our case.
            if (    tokens.getFirst().getForm() != JSONTokenForm.ObjectBoundary
                    || tokens.getLast().getForm() != JSONTokenForm.ObjectBoundary ) {
                throw new Exception("JSON Data is not begun or ended by ObjectBoundary token.");
            }
            tokens.removeFirst();
            tokens.removeLast();

            // Build tree out of the Data from the tokens list.
            // We only require the Root vertex to have the whole tree.
            Vertex root = generateTree(tokens);


        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }


    }

}

public class FileIO extends JSON{

    public static String readFileIntoString(String path) {
        try (FileInputStream stream = new FileInputStream(path)) {
            byte[] bytes = stream.readAllBytes();
            return new String(bytes);
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public static ArrayList<String> readFileIntoList(String name) {
        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            String line;
            ArrayList<String> list = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                list.add(line);
            }

            return list;
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }


}
