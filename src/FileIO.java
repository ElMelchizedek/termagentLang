import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class FileIO {

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

    public static <T> T JsonToObject(String json, Class<T> the_class) {
        Gson gson = new Gson();
        T object = null;
        try {
            object = gson.fromJson(json, the_class);
        } catch (JsonSyntaxException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return object;
    }

}
