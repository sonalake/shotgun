package com.sonalake.shotgun.report;

import com.sonalake.shotgun.usage.CommitEntry;
import com.sonalake.shotgun.usage.CommitShotgun;
import com.sonalake.shotgun.usage.ShotgunConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDate.of;
import static java.util.Arrays.asList;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportBuilderTest {

  @Test
  public void testAggregation(@TempDir Path out) {

    ShotgunConfig config = ShotgunConfig.builder()
      .outputFile(out.resolve("out.html"))
      .inputDirectory(out.resolve("git-be-here"))
      .minimumCommitInterest(3)
      .topCommitValueForFiles(5)
      .topCommitValueForFileSets(3)
      .build();


    List<CommitShotgun> commitScores = asList(
      // median is 3
      commit(of(1976, 8, 26), 1.0, "a"),
      commit(of(1976, 8, 26), 1.0, "a"),
      commit(of(1976, 8, 26), 3.0, "a"),
      commit(of(1976, 8, 26), 3.0, "a"),
      commit(of(1976, 8, 26), 4.0, "a"),

      // median is 2.5
      commit(of(1976, 8, 27), 2.0, "b", "c"),
      commit(of(1976, 8, 27), 3.0, "a", "b", "c", "d"),

      //
      commit(of(1976, 8, 28), 4.0, "b", "c"),
      //
      commit(of(1976, 8, 29), 5.0, "b", "c")
    );

    ReportBuilder builder = new ReportBuilder(commitScores, config);

    // assert the heatmaps
    assertEquals(3, builder.exportCalendarHeatMap().get(epochSecond(1976, 8, 26)));
    assertEquals(2.5, builder.exportCalendarHeatMap().get(epochSecond(1976, 8, 27)));
    assertEquals(4, builder.exportCalendarHeatMap().get(epochSecond(1976, 8, 28)));
    assertEquals(5, builder.exportCalendarHeatMap().get(epochSecond(1976, 8, 29)));

    // assert busy files
    List<BusyFile> files = builder.getBusyFiles(
      config.getTopCommitValueForFiles(), config.getMinimumCommitInterest()
    );
    assertEquals(asList(
      BusyFile.builder().count(6).file("a").build(),
      BusyFile.builder().count(4).file("b").build(),
      BusyFile.builder().count(4).file("c").build()),
      files
    );


    // assert busy sets
    List<BusySet> sets = builder.getBusySets(
      config.getTopCommitValueForFileSets(), config.getMinimumCommitInterest()
    );
    assertEquals(asList(BusySet.builder().count(3).files(asList("b", "c")).build()),
      sets
    );


    // sanity check that we don't NPE
    builder.export(config.getOutputFile());
    assertTrue(Files.isReadable(config.getOutputFile()), "File wasn't created");
  }

  private long epochSecond(int year, int month, int day) {
    return of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
  }

  private CommitShotgun commit(LocalDate day, Double score, String... paths) {
    return CommitShotgun.builder()
      .commitDate(day)
      .score(score)
      .entries(Stream.of(paths).map(this::add).collect(Collectors.toList())
      ).build();
  }


  private CommitEntry add(String path) {
    return CommitEntry.builder().changeType(ADD).path(path).build();
  }
}
