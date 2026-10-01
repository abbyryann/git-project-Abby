import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class onlyFileHasher {
    public static void main(String[] args) throws IOException {
        try {
            String sha1hash = hashFile("hello.txt");
            File backup = new File("git/objects/ " + sha1hash + ".txt");
            FileWriter object = new FileWriter(("git/objects/" + sha1hash + ".txt"));
            String content = new String(Files.readAllBytes(Paths.get("hello.txt")));
            object.write(content);
            object.close();
            System.out.println(backup);
            System.out.println(content);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static String hashFile(String filePath) throws IOException {
        // TODO (FH-4): read the whole file, digest it, convert the bytes to hex
        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path))
            throw new IOException("No such files: " + filePath);

        byte[] fileBytes = Files.readAllBytes(path);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 is not avaliable", e);
        }

        byte[] hash = digest.digest(fileBytes);

        return HexFormat.of().formatHex(hash);
    }
}
