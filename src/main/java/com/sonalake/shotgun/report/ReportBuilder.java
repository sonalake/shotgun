package com.sonalake.shotgun.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sonalake.shotgun.usage.CommitEntry;
import com.sonalake.shotgun.usage.CommitShotgun;
import com.sonalake.shotgun.usage.ShotgunConfig;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.sonalake.shotgun.usage.Utils.median;
import static java.util.Collections.emptyList;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;

/**
 * Build the HTML report.
 */
public class ReportBuilder {

  static final String REPO = "repo";
  static final String LEGEND = "legend";
  static final String HEAT_MAP = "heatMap";
  static final String BUSY_SETS = "busySets";
  static final String BUSY_FILES = "busyFiles";
  static final String COMMIT_DATA = "commitData";
  private final List<CommitShotgun> commitScores;
  private final ShotgunConfig config;

  public ReportBuilder(List<CommitShotgun> commitScores, ShotgunConfig config) {
    this.commitScores = commitScores;
    this.config = config;
  }


  public void export(String templateName, Path target) {
    Map<String, String> reportMap = buildReportMap();

    writeReport(templateName, target, reportMap);
  }

  private void writeReport(String templateName, Path target, Map<String, String> reportData) {
    try {
      // don't try to "fix" this nested try, we need to guarantee the output directory _ before_ we
      // try to create the file
      Files.createDirectories(target.getParent());
      try (Writer out = Files.newBufferedWriter(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates/"));
        Template template = cfg.getTemplate(templateName);

        template.process(reportData, out);
      }
    } catch (IOException | TemplateException e) {
      throw new AnalysisException("Failed to create report in " + target, e);
    }
  }

  /**
   * Builds the overall map
   *
   * @return
   */
  private Map<String, String> buildReportMap() {
    try {
      Map<Long, Double> heatMap = exportCalendarHeatMap();
      List<CollatedDay> commitData = getCommitData();
      List<BusySet> busySets = getBusySets(config.getTopCommitValueForFileSets(), config.getMinimumCommitInterest());
      List<BusyFile> busyFiles = getBusyFiles(config.getTopCommitValueForFiles(), config.getMinimumCommitInterest());

      ObjectMapper mapper = new ObjectMapper();
      mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
      return Map.of(
        REPO, config.getInputDirectory().getFileName().toString(),
        LEGEND, StringUtils.join(config.getLegendLevels(), ","),
        HEAT_MAP, mapper.writeValueAsString(heatMap),
        BUSY_SETS, mapper.writeValueAsString(busySets),
        BUSY_FILES, mapper.writeValueAsString(busyFiles),
        COMMIT_DATA, mapper.writeValueAsString(commitData)
      );
    } catch (IOException e) {
      throw new AnalysisException("Failed to build report model", e);
    }
  }


  /**
   * Builds a map from epochDay -> score for that day
   *
   * @return
   */
  Map<Long, Double> exportCalendarHeatMap() {
    // group the commits by date
    Map<Long, Double> data = new TreeMap<>();
    commitScores.stream()
      .collect(groupingBy(CommitShotgun::getCommitDate))
      .forEach((date, commits) -> data.put(
        date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
        median(commits.stream().map(CommitShotgun::getScore).collect(Collectors.toList()))
      ));

    return data;
  }


  /**
   * Builds a history  as a list of
   *
   * <pre>
   *   {@code
   *   {
   *         "committer": "daniel.bray@sonalake.com",
   *         "commit": "c42b2234364f388c5c3c9d3d3f1428b2bc47979f",
   *         "message": "update jenkins process\n",
   *         "score": 10.0,
   *         "size": 9,
   *         "entries": [
   *           {
   *             "sourceSet": "",
   *             "path": ".java-version",
   *             "changeType": "ADD"
   *           }
   *         ]
   *       }
   *   }
   * </pre>
   *
   * @return
   */
  List<CollatedDay> getCommitData() {
    return commitScores.stream()
      .collect(groupingBy(CommitShotgun::getCommitDate))
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByKey())
      .map(e -> collateDay(e.getKey(), e.getValue()))
      .collect(Collectors.toList());
  }

  /**
   * Builds a list of busy files in the format
   * <pre>
   *   {@code
   *    {
   *       "file": "src/app/i18n/locale-data/en.json",
   *       "count": 554
   *     }
   *   }
   * </pre>
   *
   * @param top
   * @param lowerLimit
   * @return
   */
  List<BusyFile> getBusyFiles(int top, int lowerLimit) {
    Map<String, Integer> fileCounts = new HashMap<>();

    commitScores.forEach(commit -> commit.getEntries().stream()
      .filter(e -> !DELETE.equals(e.getChangeType()))
      .forEach(entry -> {
        Integer count = fileCounts.getOrDefault(entry.getFullPath(), 0);
        fileCounts.put(entry.getFullPath(), ++count);
      }));

    List<Integer> topUniqueValues = new TreeSet<>(fileCounts.values())
      .stream()
      .filter(i -> i >= lowerLimit)
      .sorted(reverseOrder())
      .limit(top)
      .collect(Collectors.toList());

    if (topUniqueValues.isEmpty()) {
      return emptyList();
    }

    Integer lowWaterMark = topUniqueValues.get(topUniqueValues.size() - 1);
    return fileCounts.entrySet().stream()
      .filter(e -> e.getValue() >= lowWaterMark)
      .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
      .map(e -> BusyFile.builder().count(e.getValue()).file(e.getKey()).build())
      .collect(Collectors.toList());
  }

  /**
   * Build a list of busy sets in the format
   * <pre>
   *   {@code
   *    {
   *       "files": [
   *         "package-lock.json",
   *         "package.json"
   *       ],
   *       "count": 90
   *     }
   *   }
   * </pre>
   *
   * @param top
   * @param lowerLimit
   * @return
   */
  List<BusySet> getBusySets(int top, int lowerLimit) {
    Map<List<String>, Integer> setCounts = new HashMap<>();
    commitScores.stream().filter(e -> e.getEntries().size() > 1).forEach(commit -> {
      List<String> key = commit.getEntries().stream().map(CommitEntry::getFullPath).collect(Collectors.toList());
      Integer current = setCounts.getOrDefault(key, 0);
      setCounts.put(key, current + 1);
    });


    List<Integer> topUniqueValues = setCounts.values()
      .stream()
      .sorted(reverseOrder())
      .collect(Collectors.toList());

    int lowWaterMark;
    if (topUniqueValues.size() >= top) {
      lowWaterMark = topUniqueValues.get(top);
    } else if (!topUniqueValues.isEmpty()) {
      lowWaterMark = topUniqueValues.get(topUniqueValues.size() - 1);
    } else {
      lowWaterMark = 0;
    }

    return setCounts.entrySet().stream()
      .filter(e -> e.getValue() >= lowWaterMark)
      .filter(e -> e.getValue() >= lowerLimit)
      .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
      .map(e -> BusySet.builder().count(e.getValue()).files(e.getKey()).build())
      .collect(Collectors.toList());
  }

  /**
   * Collate the list of commits for a given day, into the commit score model for that day, i.e.
   *
   * <pre>
   *   {@code
   *   {
   *    {
   *   "date": "2019-07-22",
   *   "score": 2.0,
   *   "commits": [
   *     {
   *       "committer": "hubert.dolny@sonalake.com",
   *       "commit": "f7e459797ae9daf1ed389df3e023902c2f0ae4ff",
   *       "message": "Initial Jhipster application\n",
   *       "score": 2166.0,
   *       "size": 276,
   *       "entries": [
   *         {
   *           "sourceSet": "",
   *           "path": ".gitattributes",
   *           "changeType": "ADD"
   *     }
   *   ],
   *   "epochSecond": 1563750000
   * }
   *   }
   * </pre>
   *
   * @param date
   * @param commits
   * @return
   */
  private CollatedDay collateDay(LocalDate date, List<CommitShotgun> commits) {
    Double score = median(commits.stream().map(CommitShotgun::getScore).collect(Collectors.toList()));
    return CollatedDay.builder()
      .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
      .epochSecond(date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
      .score(score == null ? "n/a" : score.toString())
      .commits(commits.stream().sorted(Comparator.comparing(CommitShotgun::getScore).reversed()).collect(Collectors.toList()))
      .build();

  }

}
