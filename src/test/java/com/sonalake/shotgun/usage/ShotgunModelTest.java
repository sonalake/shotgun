package com.sonalake.shotgun.usage;

import com.sonalake.shotgun.git.GitDiffer;
import com.sonalake.shotgun.git.GitTestHelper;
import freemarker.template.TemplateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Map.of;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShotgunModelTest {

  public static final String JAVA_SRC_MAIN = "java/src/main";
  public static final String JAVA_SRC_RESOURCES = "java/src/resources";

  @Test
  public void oneFileScore(@TempDir Path working) throws InterruptedException, IOException, GitAPIException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .commit()
      .checkout("master").merge("work");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the score is 1
    List<CommitShotgun> scores = model.getScores();
    assertScore(scores.get(0), 1.0, of(ADD, singletonList("java/src/main/com/sonalake/billybob/file1")));
  }

  @Test
  public void twoFilesInOneDirectory(@TempDir Path working) throws InterruptedException, IOException, GitAPIException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file2")
      .commit()
      .checkout("master").merge("work");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the score is 2
    List<CommitShotgun> scores = model.getScores();
    assertScore(scores.get(0), 2.0,
      of(ADD, asList(
        "java/src/main/com/sonalake/billybob/file1",
        "java/src/main/com/sonalake/billybob/file2"
      )));

  }

  @Test
  public void twoFilesInOneHierarchy(@TempDir Path working) throws InterruptedException, IOException, GitAPIException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .file(JAVA_SRC_MAIN + "/com/sonalake/", "file2")
      .commit()
      .checkout("master").merge("work");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the score is 3
    List<CommitShotgun> scores = model.getScores();
    assertScore(scores.get(0), 3.0,
      of(ADD, asList(
        "java/src/main/com/sonalake/billybob/file1",
        "java/src/main/com/sonalake/file2"
      )));

  }


  @Test
  public void twoFilesInMultipleHierarchies(@TempDir Path working) throws InterruptedException, IOException, GitAPIException, TemplateException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .file(JAVA_SRC_MAIN + "/com/sonalake/cabbage/", "file1")
      .file(JAVA_SRC_MAIN + "/com/sonalake/", "file2")
      .file(JAVA_SRC_MAIN + "/com/sonalake/cabbage/and/kings", "file2")

      .commit()
      .checkout("master").merge("work");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the score is 3
    List<CommitShotgun> scores = model.getScores();
    assertScore(scores.get(0), 8.0,
      of(ADD, asList(
        "java/src/main/com/sonalake/billybob/file1",
        "java/src/main/com/sonalake/cabbage/and/kings/file2",
        "java/src/main/com/sonalake/cabbage/file1",
        "java/src/main/com/sonalake/file2"
      )));

  }


  @Test
  public void ignoresDeletes(@TempDir Path working) throws InterruptedException, IOException, GitAPIException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .commit()
      .checkout("master").merge("work")
      // now delete it
      .checkout("work2")
      .delete(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .commit()
      .checkout("master").merge("work2");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the scores are 1 and 0
    List<CommitShotgun> scores = model.getScores();
    // last commit first
    assertScore(scores.get(0), 0.0,
      of(DELETE, asList(
        "java/src/main/com/sonalake/billybob/file1"
      )));

    assertScore(scores.get(1), 1.0,
      of(ADD, asList(
        "java/src/main/com/sonalake/billybob/file1"
      )));

  }


  @Test
  public void twoFilesInAdjacentDirectories(@TempDir Path working) throws InterruptedException, IOException, GitAPIException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .file(JAVA_SRC_MAIN + "/com/sonalake/cabbage", "file2")
      .commit()
      .checkout("master").merge("work");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the score is 4
    List<CommitShotgun> scores = model.getScores();
    assertScore(scores.get(0), 4.0,
      of(ADD, asList(
        "java/src/main/com/sonalake/billybob/file1",
        "java/src/main/com/sonalake/cabbage/file2"
      )));

  }


  @Test
  public void differentSourceTreesAreDistinct(@TempDir Path working) throws InterruptedException, IOException, GitAPIException {

    // setup
    Path gitDir = working.resolve("git");
    Path outputDir = working.resolve("out");

    // given a git history of one commit with one file in it
    GitTestHelper git = new GitTestHelper(gitDir)
      .init()
      .checkout("work")
      .file(JAVA_SRC_MAIN + "/com/sonalake/billybob/", "file1")
      .file(JAVA_SRC_RESOURCES + "/com/sonalake/", "file2")
      .commit()
      .checkout("master").merge("work");


    // when we process the commits
    ShotgunModel model = givenModel(working, outputDir);
    new GitDiffer(gitDir).scanRepo(model);

    // then the score is treats the two sources as distinct
    List<CommitShotgun> scores = model.getScores();
    assertScore(scores.get(0), 2.0,
      of(ADD, asList(
        "java/src/main/com/sonalake/billybob/file1",
        "java/src/resources/com/sonalake/file2"
      )));

  }


  private ShotgunModel givenModel(@TempDir Path working, Path outputDir) {
    return new ShotgunModel(
      ShotgunConfig.builder()
        .outputFile(working.resolve(outputDir))
        .sourceSet(JAVA_SRC_MAIN)
        .sourceSet(JAVA_SRC_RESOURCES)
        .build()
    );
  }

  private void assertScore(CommitShotgun observed, double expectedScore, Map<DiffEntry.ChangeType, List<String>> expectedFiles) {
    Map<DiffEntry.ChangeType, List<String>> observedFiles = new TreeMap<>();
    observed.getEntries().forEach(e -> {
      List<String> filesByType = observedFiles.getOrDefault(e.getChangeType(), new ArrayList<>());
      filesByType.add(e.getSourceSet() + e.getPath());
      Collections.sort(filesByType);
      observedFiles.put(e.getChangeType(), filesByType);

    });
    assertEquals(expectedFiles, observedFiles, "Wrong files");
    assertEquals(expectedScore, observed.getScore(), "Wrong score");
  }

}
