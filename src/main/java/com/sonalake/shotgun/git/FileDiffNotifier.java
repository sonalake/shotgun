package com.sonalake.shotgun.git;


import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

/**
 * Passed to the {@link GitDiffer} and into which are passed any non-empty git diffs
 */
@FunctionalInterface
public interface FileDiffNotifier {
  /**
   * Notice a new git diff
   *
   * @param commit The commit details
   * @param entries The list of entry details
   */
  void noticeDiff(RevCommit commit, List<DiffEntry> entries);
}
