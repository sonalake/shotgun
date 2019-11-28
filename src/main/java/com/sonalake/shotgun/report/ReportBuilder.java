package com.sonalake.shotgun.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonalake.shotgun.usage.CommitEntry;
import com.sonalake.shotgun.usage.CommitShotgun;
import com.sonalake.shotgun.usage.ShotgunConfig;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
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
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;

public class ReportBuilder {

  private final List<CommitShotgun> commitScores;
  private final ShotgunConfig config;

  public ReportBuilder(List<CommitShotgun> commitScores, ShotgunConfig config) {
    this.commitScores = commitScores;
    this.config = config;
  }


  public void export(Path target) throws IOException, TemplateException {
    // make sure the output directory exists
    Files.createDirectories(target.getParent());

    Map<Long, Double> heatMap = exportCalendarHeatMap();
    List<? extends Map<String, ?>> commitData = getCommitData();
    List<Map<String, ?>> busySets = getBusySets(config.getTopCommitValueForFileSets(), config.getMinimumCommitInterest());
    List<Map<String, ?>> busyFiles = getBusyFiles(config.getTopCommitValueForFiles(), config.getMinimumCommitInterest());

    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> in = Map.of(
      "repo", config.getInputDirectory().getFileName().toString(),
      "legend", StringUtils.join(config.getLegendLevels(), ","),
      "heatMap", mapper.writeValueAsString(heatMap),
      "busySets", mapper.writeValueAsString(busySets),
      "busyFiles", mapper.writeValueAsString(busyFiles),
      "commitData", mapper.writeValueAsString(commitData)
    );

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
    cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates/"));
    Files.createDirectories(target.getParent());
    Template template = cfg.getTemplate("report.template.ftl");

    try (Writer out = Files.newBufferedWriter(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      template.process(in, out);
    }
  }

  public Map<Long, Double> exportCalendarHeatMap() throws IOException {
    // group the commits by date
    Map<Long, Double> data = new TreeMap<>();
    commitScores.stream()
      .collect(groupingBy(CommitShotgun::getCommitDate))
      .forEach((date, commits) -> {
        data.put(
          date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
          median(commits.stream().map(CommitShotgun::getScore).collect(Collectors.toList()))
        );
      });

    return data;
  }


  private List<? extends Map<String, ?>> getCommitData() {
    return commitScores.stream()
      .collect(groupingBy(CommitShotgun::getCommitDate))
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByKey())
      .map(e -> collateDay(e.getKey(), e.getValue()))
      .collect(Collectors.toList());
  }

  private List<Map<String, ?>> getBusyFiles(int top, int lowerLimit) {
    Map<String, Integer> fileCounts = new HashMap<>();

    commitScores.stream().flatMap(e -> e.getEntries().stream()).forEach(entry -> {
      int count = fileCounts.getOrDefault(entry.getPath(), 0);
      fileCounts.put(entry.getPath(), count + 1);
    });

    List<Integer> topUniqueValues = new TreeSet<>(fileCounts.values())
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

    return fileCounts.entrySet().stream()
      .filter(e -> e.getValue() >= lowWaterMark)
      .filter(e -> e.getValue() >= lowerLimit)
      .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
      .map(e -> Map.of(
        "count", e.getValue(),
        "file", e.getKey()
      ))
      .collect(Collectors.toList());
  }

  private List<Map<String, ?>> getBusySets(int top, int lowerLimit) {
    Map<List<String>, Integer> setCounts = new HashMap<>();
    commitScores.stream().filter(e -> e.getEntries().size() > 1).forEach(commit -> {
      List<String> key = commit.getEntries().stream().map(CommitEntry::getPath).collect(Collectors.toList());
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
      .map(e -> Map.of(
        "count", e.getValue(),
        "files", e.getKey()
      ))
      .collect(Collectors.toList());
  }

  private Map<String, ?> collateDay(LocalDate date, List<CommitShotgun> commits) {
    Double score = median(commits.stream().map(CommitShotgun::getScore).collect(Collectors.toList()));
    return Map.of(
      "date", date.format(DateTimeFormatter.ISO_LOCAL_DATE),
      "epochSecond", date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
      "score", score == null ? "n/a" : score,
      "commits", commits.stream().sorted(Comparator.comparing(CommitShotgun::getScore).reversed()).collect(Collectors.toList())
    );
  }

}
