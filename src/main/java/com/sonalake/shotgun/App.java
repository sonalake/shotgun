/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.sonalake.shotgun;

import com.beust.jcommander.JCommander;
import com.sonalake.shotgun.cli.Params;
import com.sonalake.shotgun.git.GitDiffer;
import com.sonalake.shotgun.report.ReportBuilder;
import com.sonalake.shotgun.usage.ShotgunConfig;
import com.sonalake.shotgun.usage.ShotgunModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  public static void main(String[] args) {
    new App().execute(args);
  }

  void execute(String... args) {
    Params params = new Params();
    JCommander parser = JCommander.newBuilder()
      .addObject(params)
      .build();
    ;

    if (args.length == 0) {
      parser.usage();
    } else {
      parser.parse(args);
      if (params.isHelp()) {
        parser.usage();
      } else {
        ShotgunConfig config = params.toConfig();
        ShotgunModel data = analyseGit(config);
        writeReport(config, data);
      }
    }
  }

  ShotgunModel analyseGit(ShotgunConfig config) {
    ShotgunModel shotgunModel = new ShotgunModel(config);
    buildGitDiffer(config).scanRepo(shotgunModel);
    return shotgunModel;

  }

  protected GitDiffer buildGitDiffer(ShotgunConfig config) {
    return GitDiffer.builder()
      .workingDirectory(config.getInputDirectory())
      .build();
  }

  void writeReport(ShotgunConfig config, ShotgunModel data) {
    new ReportBuilder(data.getScores(), config)
      .export("report.template.ftl", config.getOutputFile());

    log.info("Report written to {}", config.getOutputFile());
  }
}
