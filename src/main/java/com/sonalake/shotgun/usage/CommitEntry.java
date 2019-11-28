package com.sonalake.shotgun.usage;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.diff.DiffEntry;

@Data
@Builder
@RequiredArgsConstructor
public class CommitEntry {
  private final String path;
  private final DiffEntry.ChangeType changeType;
}
