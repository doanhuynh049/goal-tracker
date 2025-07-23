package util;

import java.io.*;
import java.util.List;

public class FileUtil {
    // Save a list of objects to a file using serialization
    public static <T extends Serializable> void saveToFile(List<T> list, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(list);
        }
    }

    // Load a list of objects from a file using deserialization
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<T>) ois.readObject();
        }
    }

    // Save a list of strings to a file (one per line)
    public static void saveLinesToFile(List<String> lines, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // Load a list of strings from a file (one per line)
    public static List<String> loadLinesFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return reader.lines().toList();
        }
    }
}
