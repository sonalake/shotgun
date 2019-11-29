package com.sonalake.shotgun.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
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
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
  public void testGitError(@TempDir Path workingDir) throws IOException {

    GitDiffer differ = GitDiffer.builder()
      .workingDirectory(workingDir)
      .build();

    // given we have some git repo
    Git git = mock(Git.class);
    Repository repo = mock(Repository.class);
    when(git.getRepository()).thenReturn(repo);
    ObjectReader reader = mock(ObjectReader.class);
    when(git.getRepository().newObjectReader()).thenReturn(reader);

    // but that opening a reader has some error
    doThrow(new IOException("Oh noes")).when(reader).open(any(AnyObjectId.class), eq(OBJ_TREE));

    // when we try to diff these commits
    RevCommit commit = commit();
    RevCommit parent = commit();
    GitException observed = assertThrows(GitException.class,
      () -> differ.manageDiffs(git, commit, parent, new FileDiffNotifier[0])
    );

    // the error is as expected
    assertEquals(
      "Failed to get diff between " + parent + " and " + commit
      , observed.getMessage()
    );
    assertEquals(
      "Oh noes"
      , observed.getCause().getMessage()
    );
  }

  private RevCommit commit() {
    RevCommit commit = mock(RevCommit.class);
    when(commit.getTree()).thenReturn(mock(RevTree.class));
    when(commit.getTree().getId()).thenReturn(new ObjectId(1, 2, 3, 4, 5));
    return commit;
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
