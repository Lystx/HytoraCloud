import org.kohsuke.github.*;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class Test {

    public static void main(String[] args) throws IOException {
        GitHub github = GitHubBuilder.fromEnvironment().build();

        GHRepository repo = github.getRepository("Lystx/HytoraCloud");

        for (GHCommit commit : repo.listCommits()) {
            System.out.println(commit.getCommitter().getName() +  " => " + commit.getCommitShortInfo().getMessage() + " => " + new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(commit.getCommitDate()));
        }
    }
}
