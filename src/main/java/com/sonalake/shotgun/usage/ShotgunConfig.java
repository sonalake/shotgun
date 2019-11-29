package com.sonalake.shotgun.usage;

import lombok.*;

import java.nio.file.Path;
import java.util.List;

@Builder
@RequiredArgsConstructor
@Data
@ToString
public class ShotgunConfig {

  /**
   * Where is the git project (.git is in this)
   */
  private final Path inputDirectory;

  /**
   * Where should the output report go
   */
  private final Path outputFile;

  /**
   * What source sets should be used to split up file types
   */
  @Singular
  private final List<String> sourceSets;

  /**
   * In the hotspots - ignore any files / sets with fewer than this many commits
   */
  private final Integer minimumCommitInterest;

  /**
   * In the hotpsots - take the top N file set counts as a filter - this may result in more than N results
   */
  private final Integer topCommitValueForFileSets;

  /**
   * In the hotpsots - take the top N file counts as a filter - this may result in more than N results
   */
  private final Integer topCommitValueForFiles;

  /**
   * What legend levels to use
   */
  @Singular
  private final List<Integer> legendLevels;
}
