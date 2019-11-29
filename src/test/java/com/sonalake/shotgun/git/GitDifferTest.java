package com.sonalake.shotgun.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.jgit.diff.DiffEntry.DEV_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GitDifferTest {

  @Test
  public void testFileError(@TempDir Path workingDir) {
    GitException observed = assertThrows(GitException.class,
      () ->
        GitDiffer.builder()
          .workingDirectory(workingDir)
          .build()
          .scanRepo((commit, entries) -> {
          })
    );
    assertEquals("No git repo in: " + workingDir, observed.getMessage());
  }

  @Test
  public void testSimpleLog(@TempDir Path workingDir) throws GitAPIException, IOException, InterruptedException {
    new GitTestHelper(workingDir)
      .init()
      .checkout("work")
      .file("a/b/c", "file1")
      .commit()
      .checkout("master").merge("work");

    List<List<DiffEntry>> commits = new ArrayList<>();

    GitDiffer.builder()
      .workingDirectory(workingDir)
      .build()
      .scanRepo((commit, entries) -> commits.add(entries));

    List<List<String>> simpleHistory = commits.stream()
      .map(e -> e.stream().map(v -> v.getChangeType() + ": " + pathOf(v)).collect(Collectors.toList()))
      .collect(Collectors.toList());

    assertEquals(singletonList(singletonList("ADD: a/b/c/file1")), simpleHistory);

  }


  @Test
  public void testUpdatesAndDeletes(@TempDir Path workingDir) throws GitAPIException, IOException, InterruptedException {
    GitTestHelper git = new GitTestHelper(workingDir).init();

// add a file on a branch and merge it to master
    git.checkout("work")
      .file("a/b/c", "file1")
      .commit()
      .checkout("master").merge("work");

// edit a file and add a file on some branch and then merge it to master

    git.checkout("work2")
      .file("a/b/c", "file1")
      .file("a/b/c", "file2")
      .commit()
      .checkout("master").merge("work2");

// edit a file on some branch, and delte a file, and then merge it to master

    git.checkout("work3")
      .file("a/b/c", "file1")
      .delete("a/b/c", "file2")
      .commit()
      .checkout("master").merge("work3");

    List<List<DiffEntry>> commits = new ArrayList<>();

    GitDiffer.builder()
      .workingDirectory(workingDir)
      .build()
      .scanRepo((commit, entries) -> commits.add(entries));


    List<List<String>> simpleHistory = commits.stream()
      .map(e -> e.stream().map(v -> v.getChangeType() + ": " + pathOf(v)).collect(Collectors.toList()))
      .collect(Collectors.toList());

    assertEquals(asList(
      asList("MODIFY: a/b/c/file1", "DELETE: a/b/c/file2"),
      asList("MODIFY: a/b/c/file1", "ADD: a/b/c/file2"),
      singletonList("ADD: a/b/c/file1")
    ), simpleHistory);

  }


  private String pathOf(DiffEntry e) {
    if (!e.getNewPath().equals(DEV_NULL)) {
      return e.getNewPath();
    }
    return e.getOldPath();
  }
}
