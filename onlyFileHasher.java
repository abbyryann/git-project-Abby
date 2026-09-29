import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class onlyFileHasher {
    public String hashFile(String filePath) throws IOException {
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

    public void main() {
        try {
            System.out.println(hashFile("hello.txt"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
