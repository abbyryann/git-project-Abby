import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.nio.file.Files;
import java.nio.file.Path;

public class directions {
    public static void main(String[] args) {
        try {
            // TODO (FH-1): create the JavaFileSystem directory
            File root = new File("git/");
            root.mkdirs();

            // make objects/ inside git/
            File backup = new File("git/objects/");
            backup.mkdirs();

            // Create a file named index inside git/ if it does not already exist, with no
            // extension
            FileWriter indexWriter = new FileWriter("git/index");
            indexWriter.close();

            // Create a file named HEAD inside git/ if it does not already exist, with no
            // extension
            FileWriter headWriter = new FileWriter("git/HEAD");
            headWriter.close();

            // print message
            System.out.println("Git Repository Created");

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }
}
