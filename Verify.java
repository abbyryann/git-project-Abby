import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Verify {
    public static void main(String[] args) throws IOException {
        Git.init(args);
        new FileWriter("git/index").close();

        Git.createBlob("hello.txt");
        Git.createBlob("myProgram/badem.txt");
        Git.createBlob("myProgram/Hello.txt");
        Git.createBlob("myProgram/docs/World.txt");
        Git.createBlob("myProgram/scripts/miao.txt");
        System.out.println(Files.readString(Path.of("git/index")));

        ArrayList<String> list = new ArrayList<>();
        list.add("blob 7c211433f02071597741e6ff5a8ea34789abbf43 myProgram/docs/World.txt");
        list.add("blob 47bd6bb31751ca4ce5045708ff897b827f36bf6b myProgram/scripts/miao.txt");
        System.out.println(Git.createTree(list, "myProgram/docs"));
        System.out.println(Git.createTree(list, "myProgram/scripts"));
        System.out.println(Git.createTree(list, "myProgram"));
        System.out.println(Git.createTreeFromIndex());

        Git.commit("Zack", "first");
        Git.commit("Zack", "second");
    }
}
