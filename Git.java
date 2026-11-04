import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Git { // rename Only Filhasher to Git
    public static void main(String[] args) throws IOException {
        System.err.println(init(args));
        for (int i = 0; i < args.length; i++) {
            createBlob(args[i]);
        }
    }

    public static void createBlob(String filePath) throws IOException {
        String sha1hash = hashFile(filePath);
        // String content = new String(Files.readAllBytes(Paths.get(filePath)));
        File backup = new File("git/objects/ " + sha1hash);

        // FileWriter object = new FileWriter(("git/objects/" + sha1hash));
        // object.write(content);
        // object.close();
        File blobFile = new File("git/objects", sha1hash); // now it read by byte under the while loop

        if (!blobFile.exists()) {
            FileReader reader = new FileReader(filePath);
            FileWriter writer = new FileWriter(blobFile);

            int currentByte = reader.read();

            while (currentByte != -1) {
                writer.write(currentByte);
                currentByte = reader.read();
            }

            reader.close();
            writer.close();
        }

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
            throw new IOException("SHA-1 is not avaliable", e);// SHA-256 instead of SHA-1
        }

        byte[] hash = digest.digest(fileBytes);

        return HexFormat.of().formatHex(hash);
    }

    public static void updateIndexFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        String fullPath = path.toAbsolutePath().toString();
        String rootPath = Path.of("").toAbsolutePath().toString();
        fullPath = path.toAbsolutePath().normalize().toString();
        if (fullPath.startsWith(rootPath + File.separator)) {
            fullPath = fullPath.substring(rootPath.length() + 1);
        }
        fullPath = fullPath.replace("\\", "/");
        String hash = hashFile(filePath);// changed sha1hash to hash
        String output = hash + " " + fullPath;
        Path indexPath = Path.of("git/index");
        if (Files.exists(indexPath)) {
            ArrayList<String> lines = new ArrayList<>(Files.readAllLines(indexPath));
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.equals(output)) {
                    return;
                }
                String linePath = line.substring(line.indexOf(" ") + 1);
                if (linePath.equals(fullPath)) {
                    lines.set(i, output);
                    changed = true;
                }
            }
            if (changed) {
                FileWriter rewriteIndex = new FileWriter("git/index");
                rewriteIndex.write(lines.get(0));
                for (int i = 1; i < lines.size(); i++) {
                    rewriteIndex.write("\n" + lines.get(i));
                }
                rewriteIndex.close();
                return;
            }

        }
        FileWriter updatingIndex = new FileWriter(("git/index"), true);
        // String content = new String(Files.readAllBytes(Paths.get(filePath)));
        if (!(Files.size(indexPath) == 0)) {
            updatingIndex.write("\n");
        }
        updatingIndex.write(hash + " " + fullPath);
        updatingIndex.close();
    }

    public static String createTree(ArrayList<String> workingList, String dirPath) throws IOException {
        StringBuilder treeBuilder = new StringBuilder();
        for (String entry : workingList) {
            String[] components = entry.split(" ", 3);
            if (components.length != 3) {
                throw new IllegalArgumentException("Working List made incorrectly.");
            }
            String type = components[0];
            String hash = components[1];
            String path = components[2];
            String fileParent;
            String fileName;
            if (path.contains("/")) {
                fileParent = path.substring(0, path.lastIndexOf("/"));
                fileName = path.substring(path.lastIndexOf("/") + 1);
            } else {
                fileParent = "";
                fileName = path;
            }
            if (fileParent.equals(dirPath)) {
                if (treeBuilder.length() > 0) {
                    treeBuilder.append("\n");
                }
                treeBuilder.append(type).append(" ").append(hash).append(" ").append(fileName);
            }
        }

        Path treePath = Path.of("git", "objects", "tempName");
        Files.writeString(treePath, treeBuilder.toString());
        String treeHash = hashFile(treePath.toString());
        Files.move(treePath, Path.of("git", "objects", treeHash), StandardCopyOption.REPLACE_EXISTING);
        return treeHash;
    }

    public static String createTreeFromIndex() throws IOException {
        ArrayList<String> workingList = new ArrayList<>();
        ArrayList<String> indexLines = new ArrayList<>(Files.readAllLines(Path.of("git/index")));
        for (int i = 0; i < indexLines.size(); i++) {
            if (!indexLines.get(i).isBlank()) {
                workingList.add("blob " + indexLines.get(i));
            }
        }

        for (int i = 1; i < workingList.size(); i++) {
            String current = workingList.get(i);
            String currentPath = current.split(" ", 3)[2];
            int j = i - 1;
            while (j >= 0 && workingList.get(j).split(" ", 3)[2].compareToIgnoreCase(currentPath) > 0) {
                workingList.set(j + 1, workingList.get(j));
                j--;
            }
            workingList.set(j + 1, current);
        }

        while (true) {
            String deepestDir = "";
            int deepestCount = 0;
            for (int i = 0; i < workingList.size(); i++) {
                String entryPath = workingList.get(i).split(" ", 3)[2];
                int slashCount = 0;
                for (int k = 0; k < entryPath.length(); k++) {
                    if (entryPath.charAt(k) == '/') {
                        slashCount++;
                    }
                }
                if (slashCount > deepestCount) {
                    deepestCount = slashCount;
                    deepestDir = entryPath.substring(0, entryPath.lastIndexOf("/"));
                }
            }
            if (deepestCount == 0) {
                break;
            }

            String treeHash = createTree(workingList, deepestDir);

            ArrayList<String> newList = new ArrayList<>();
            boolean added = false;
            for (int i = 0; i < workingList.size(); i++) {
                String entry = workingList.get(i);
                String entryPath = entry.split(" ", 3)[2];
                String entryDir = "";
                if (entryPath.contains("/")) {
                    entryDir = entryPath.substring(0, entryPath.lastIndexOf("/"));
                }
                if (entryDir.equals(deepestDir)) {
                    if (!added) {
                        newList.add("tree " + treeHash + " " + deepestDir);
                        added = true;
                    }
                } else {
                    newList.add(entry);
                }
            }
            workingList = newList;
        }
        if (workingList.isEmpty()) {
            throw new IOException("Nothing left in working list.");
        }
        return createTree(workingList, "");
    }

    public static String commit(String author, String message) throws IOException {
        if (author == null || message == null || author.isEmpty() || message.isEmpty()) {
            throw new IllegalArgumentException("Author and message are needed");
        }
        String treeHash = createTreeFromIndex();
        Path headPath = Path.of("git", "HEAD");
        String parent = Files.readString(headPath);
        String commitDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder commit = new StringBuilder();
        commit.append("tree: ").append(treeHash).append("\n");
        if (!parent.isEmpty()) {
            commit.append("parent: ").append(parent).append("\n");
        }
        commit.append("author: ").append(author).append("\n");
        commit.append("date: ").append(commitDate).append("\n");
        commit.append("message: ").append(message);
        Path tempPath = Path.of("git", "objects", "tempCommit");
        Files.writeString(tempPath, commit.toString());
        String commitHash = hashFile(tempPath.toString());
        Files.move(tempPath, Path.of("git", "objects", commitHash), StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(headPath, commitHash);
        return commitHash;
    }

    public static String init(String[] args) { // change output to a string that you return
        try {
            // TODO (FH-1): create the JavaFileSystem directory
            boolean folderMade = false;
            File root = new File("git/");
            if (!root.exists()) { // adding check statement
                root.mkdirs();
                folderMade = true;
            }

            // make objects/ inside git/
            File backup = new File("git/objects/");
            if (!backup.exists()) {
                backup.mkdirs();
                folderMade = true;
            }
            File indexFile = new File("git/index");
            if (!indexFile.exists()) {
                indexFile.createNewFile();
                folderMade = true;
            }
            File HeadFile = new File("git/HEAD");
            if (!HeadFile.exists()) {
                HeadFile.createNewFile();
                folderMade = true;
            }

            if (folderMade) {
                return "Git Repository Created";
            } else {
                return "Git Repository Already Exist";
            }

            // Create a file named index inside git/ if it does not already exist, with no
            // extension

            // Create a file named HEAD inside git/ if it does not already exist, with no
            // extension

            // print message

        } catch (IOException e) {
            return ("File error: " + e.getMessage());
        }
    }
}