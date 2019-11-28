package com.sonalake.shotgun.usage;

import lombok.*;

import java.nio.file.Path;
import java.util.List;

@Builder
@RequiredArgsConstructor
@Data
@ToString
public class ShotgunConfig {

  private final Path inputDirectory;
  private final Path outputFile;
  @Singular
  private final List<String> sourceSets;

  private final Integer minimumCommitInterest;

  private final Integer topCommitValueForFileSets;

  private final Integer topCommitValueForFiles;

  @Singular
  private final List<Integer> legendLevels;
}
