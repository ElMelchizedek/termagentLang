import java.io.*;
import java.util.ArrayList;


public class FileIO extends JSON {

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
