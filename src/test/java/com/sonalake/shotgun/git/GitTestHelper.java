package com.sonalake.shotgun.git;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RequiredArgsConstructor
public class GitTestHelper {
  private final Path workingDir;
  private Git git;


  public GitTestHelper file(String directory, String file) throws IOException {
    Path fullPath = workingDir.resolve(directory).resolve(file);
    Files.createDirectories(fullPath.getParent());
    if (!Files.exists(fullPath)) {
      Files.createFile(fullPath);
    } else {
      Files.writeString(fullPath, "update\n", StandardOpenOption.APPEND);
    }
    return this;
  }

  public GitTestHelper delete(String directory, String file) throws IOException {
    Path fullPath = workingDir.resolve(directory).resolve(file);
    Files.deleteIfExists(fullPath);
    return this;
  }

  public GitTestHelper init() throws GitAPIException, IOException, InterruptedException {
    this.git = Git.init()
      .setDirectory(workingDir.toFile())
      .setGitDir(workingDir.resolve(".git").toFile())
      .call();
    Files.writeString(workingDir.resolve(".init"), "something to start with", StandardOpenOption.CREATE_NEW);
    commit();
    return this;
  }

  private void assertGit() {
    assertNotNull(this.git, "not initialised");
  }

  public GitTestHelper checkout(String name) throws GitAPIException {
    assertGit();
    boolean exists = git.branchList().call().stream().map(Ref::getName).anyMatch(e -> e.endsWith(name));
    if (!exists) {
      git.branchCreate().setName(name).call();
    }
    git.checkout().setName(name).call();

    return this;
  }


  public GitTestHelper commit() throws GitAPIException {
    assertGit();
    git.add().addFilepattern(".").call();
    DirCache delta = git.add().setUpdate(true).addFilepattern(".").call();
    List<String> message = new ArrayList<>();
    for (int i = 0; i != delta.getEntryCount(); i++) {
      message.add(delta.getEntry(i).getFileMode() + ": " + delta.getEntry(i).getPathString());
    }
    git.commit().setMessage("Commit: " + join(message, "\n")).call();
    return this;
  }


  public GitTestHelper merge(String from) throws GitAPIException, IOException {
    assertGit();
    ObjectId otherBranch = git.getRepository().resolve(from);
    git.merge()
      .include(otherBranch)
      .setCommit(true)
      .setFastForward(MergeCommand.FastForwardMode.FF)
      .setMessage("Merging " + from).call();

    return this;
  }


}
