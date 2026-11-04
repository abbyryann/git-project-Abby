import java.io.IOException;

public class GitTester {
    public static void main(String[] args) throws IOException {
        GitWrapper.init();
        GitWrapper.add("myProgram/Hello.txt");
        GitWrapper.add("myProgram/docs/World.txt");
        String hash = GitWrapper.commit("John Doe", "Initial commit");
        System.out.println("Commit: " + hash);
    }
}