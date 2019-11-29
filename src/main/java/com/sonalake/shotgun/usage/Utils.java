package com.sonalake.shotgun.usage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {


  /**
   * Calculate the median
   *
   * @param values a collection of doubles - any nulls are ignored
   * @return null if there are no values, or else the median
   */
  public static Double median(Collection<Double> values) {
    List<Double> scores = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
    if (scores.isEmpty()) {
      return null;
    }

    double[] unboxedScores = new double[scores.size()];
    for (int i = 0; i != scores.size(); i++) {
      unboxedScores[i] = scores.get(i);
    }

    return new Median().evaluate(unboxedScores);

  }


  /**
   * Identify the commit entry for the path
   *
   * @param entry               the input entry
   * @param identifyingPrefixes the available source sets
   * @return the defined commit entry
   */
  public static CommitEntry identifyPath(DiffEntry entry, List<String> identifyingPrefixes) {

    String path = DELETE.equals(entry.getChangeType())
      ? entry.getOldPath()
      : entry.getNewPath();

    for (String candidate : identifyingPrefixes) {
      if (path.startsWith(candidate)) {
        return CommitEntry.builder()
          .changeType(entry.getChangeType())
          .sourceSet(candidate)
          .path(StringUtils.substringAfter(path, candidate))
          .build();
      }
    }

    return CommitEntry.builder()
      .changeType(entry.getChangeType())
      .sourceSet("")
      .path(path)
      .build();
  }

}
