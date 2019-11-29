package com.sonalake.shotgun.usage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.diff.DiffEntry;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Data
@Builder
@RequiredArgsConstructor
public class CommitEntry {
  private final String sourceSet;
  private final String path;
  private final DiffEntry.ChangeType changeType;

  @JsonIgnore
  public String getFullPath() {
    return trimToEmpty(getSourceSet()) + getPath();
  }
}
