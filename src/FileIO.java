import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileIO {

    public static ArrayList<String> lineListOfFile(String name) {
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
            System.exit(0);
        }

        return null;
    }

}
