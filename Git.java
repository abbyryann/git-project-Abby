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

public class Git{ //rename Only Filhasher to Git
    public static void main(String[] args) throws IOException {
        init(args);
        for (int i = 0; i < args.length; i++) {
            createBlob(args[i]);
        }
    }

    public static void createBlob(String filePath) throws IOException {
        String sha1hash = hashFile(filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        File backup = new File("git/objects/ " + sha1hash);
        FileWriter object = new FileWriter(("git/objects/" + sha1hash));
        object.write(content);
        object.close();
        System.out.println(backup);
        updateIndexFile(filePath);
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

    public static void updateIndexFile(String filePath) throws IOException {
        String hash = hashFile(filePath);//changed sha1hash to hash
        File backup = new File("git/index/" + hash + ".txt");
        FileWriter updatingIndex = new FileWriter(("git/index"));
        // String content = new String(Files.readAllBytes(Paths.get(filePath)));
        updatingIndex.write(hash + " " + filePath);
        updatingIndex.close();
        System.out.println(backup);
        System.out.println(updatingIndex);
    }
    public static void init(String[] args) {
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
