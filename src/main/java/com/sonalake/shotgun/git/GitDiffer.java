package com.sonalake.shotgun.git;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Builder
@RequiredArgsConstructor
@Slf4j
public class GitDiffer {
  /**
   * The location of the git project (it is assumed that the .git directory is in this)
   */
  private final Path workingDirectory;


  /**
   * Scan the workingDirectory for changes with at least one parent, and use the diffs from the first of those parents
   *
   * @param notifiers The notifiers into which the changes will be fired
   * @throws GitAPIException
   * @throws IOException
   */
  public void scanRepo(FileDiffNotifier... notifiers) throws GitAPIException, IOException {
    final Git git = openRepo();
    log.debug("Scanning repo {} for commits into {}", git.getRepository().getIdentifier(), git.getRepository().getBranch());
    ObjectId branchId = git.getRepository()
      .exactRef("refs/heads/" + git.getRepository().getBranch())
      .getObjectId();

    LogCommand getLog = git.log().add(branchId);
    getLog.call().forEach(commit -> {
      log.debug("{} {} {}", commit.getAuthorIdent(), commit, commit.getFullMessage());
      try {
        if (commit.getParents().length > 0) {
          RevCommit parent = commit.getParent(0);
          manageDiffs(git, commit, parent, notifiers);
        }
      } catch (IOException | GitAPIException e) {
        throw new IllegalStateException("Oh noes!", e);
      }
    });
  }

  private Git openRepo() throws IOException {
    if (!workingDirectory.toFile().canRead() || workingDirectory.toFile().list().length == 0) {
      throw new IllegalArgumentException("Can't read repo in: " + workingDirectory);
    } else {
      log.debug("Using existing in {}", workingDirectory);
      return Git.open(workingDirectory.toFile());
    }
  }


  /**
   * Handles the actual diff calculation
   * @param git the git instance
   * @param commit the current commit
   * @param parent the commit *FROM* which the diff is being applied
   * @param notifiers the notifiers at which the diffs are being fired
   * @throws IOException
   * @throws GitAPIException
   */
  private void manageDiffs(Git git, RevCommit commit, RevCommit parent, FileDiffNotifier[] notifiers) throws IOException, GitAPIException {
    ObjectReader reader = git.getRepository().newObjectReader();

    // the old tree is based on the parent
    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
    oldTreeIter.reset(reader, parent.getTree().getId());

    // the new tree is based on the current commit
    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
    newTreeIter.reset(reader, commit.getTree().getId());

    //  get the list of changed files and fire them on to the notifiers
    List<DiffEntry> diffs = git.diff()
      .setNewTree(newTreeIter)
      .setOldTree(oldTreeIter)
      .call();
    for (FileDiffNotifier notifier : notifiers) {
      notifier.noticeDiff(commit, diffs);
    }
  }
}
