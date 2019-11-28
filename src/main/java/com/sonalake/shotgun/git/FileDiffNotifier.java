package com.sonalake.shotgun.git;


import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

@FunctionalInterface
public interface FileDiffNotifier {
  void noticeDiff(RevCommit commit, List<DiffEntry> entries);
}
