package com.sonalake.shotgun.cli;

import com.beust.jcommander.Parameter;
import com.sonalake.shotgun.usage.ShotgunConfig;
import lombok.Getter;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;

@Getter
public class Params {

  @Parameter(names = {"-h", "--help"}, help = true, description = "Print this help message")
  private boolean help;

  @Parameter(required = true, names = {"-i", "--input-dir"}, description = "The working directory, must be a git root directory")
  private String inputDirectory;

  @Parameter(names = {"-o", "--output-file"}, description = "The output file location")
  private String outputFile = ".shotgun/report.html";

  @Parameter(names = {"-s", "--source-set"}, description = "A source set to split by")
  private List<String> sourceSets = emptyList();

  @Parameter(names = {"-m", "--commit-minimum"}, description = "Files and sets with a commit size less than this are ignored")
  private Integer minimumCommitInterest = 3;

  @Parameter(names = {"-cs", "--commit-size-set"},
    description = "We want the sets in the top N counts (this may result in more than N results)")
  private Integer topCommitValueForFileSets = 10;

  @Parameter(names = {"-cf", "--commit-size-file"},
    description = "We want the files in the top N counts (this may result in more than N results)")
  private Integer topCommitValueForFiles = 40;

  @Parameter(names = {"-ll", "--legend-levels"}, description = "The legend levels")
  private List<Integer> legendLevels = asList(10, 20, 30, 50, 80, 120);


  public ShotgunConfig toConfig() {
    return ShotgunConfig.builder()
      .inputDirectory(Paths.get(inputDirectory))
      .outputFile(Paths.get(outputFile))
      .sourceSets(sourceSets)
      .minimumCommitInterest(minimumCommitInterest)
      .topCommitValueForFileSets(topCommitValueForFileSets)
      .topCommitValueForFiles(topCommitValueForFiles)
      .legendLevels(legendLevels.stream().sorted().collect(Collectors.toList()))
      .build();
  }
}
