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
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

@Slf4j
public class App {
  public static void main(String[] args) throws GitAPIException, IOException, TemplateException {
    Params params = new Params();
    JCommander parser = JCommander.newBuilder()
      .addObject(params)
      .build();

    if (args.length == 0) {
      parser.usage();
    } else {
      parser.parse(args);
      if (params.isHelp()) {
        parser.usage();
      } else {
        new App().process(params.toConfig());
      }
    }
  }

  public void process(ShotgunConfig config) throws GitAPIException, IOException, TemplateException {
    ShotgunModel shotgunModel = new ShotgunModel(config);

    GitDiffer.builder()
      .workingDirectory(config.getInputDirectory())
      .build()
      .scanRepo(shotgunModel);


    new ReportBuilder(shotgunModel.getScores(), config)
      .export(config.getOutputFile());

    log.info("Report written to {}", config.getOutputFile());

  }
}
