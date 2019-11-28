package com.sonalake.shotgun.usage;

import com.sonalake.shotgun.git.FileDiffNotifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.sonalake.shotgun.usage.Utils.identifyPath;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;


@Slf4j
public class ShotgunModel implements FileDiffNotifier {


  private final List<CommitShotgun> commitScores;
  private final ShotgunConfig config;

  public ShotgunModel(ShotgunConfig config) {
    commitScores = new ArrayList<>();
    this.config = config;
  }

  @Override
  public void noticeDiff(RevCommit commit, List<DiffEntry> entries) {
    if (entries.isEmpty()) {
      return;
    }
    CommitShotgun shotgun = CommitShotgun.builder()
      .commit(commit.getName())
      .committer(commit.getCommitterIdent().getEmailAddress())
      .commitDate(
        LocalDate.ofInstant(
          commit.getCommitterIdent().getWhen().toInstant(),
          commit.getCommitterIdent().getTimeZone().toZoneId()
        )
      )
      .score(score(commit, entries))
      .message(commit.getFullMessage())
      .entries(entries.stream().map(e ->
        CommitEntry.builder()
          .path(identifyPath(e, Collections.emptyList()))
          .changeType(e.getChangeType())
          .build()
      ).collect(Collectors.toList()))
      .size(entries.size())
      .build();

    commitScores.add(shotgun);
  }

  private DirectedAcyclicGraph<String, DefaultWeightedEdge> buildGraph(List<DiffEntry> entries) {
    // the subgraphs will eventually be trees
    DirectedAcyclicGraph<String, DefaultWeightedEdge> graph = new DirectedAcyclicGraph<>(DefaultWeightedEdge.class);
    entries.forEach(entry -> {
      String path = identifyPath(entry, config.getSourceSets());

      String[] pathElements = StringUtils.split(path, File.separator);

      String lastElement = null;
      for (int i = 0; i != pathElements.length; i++) {
        String element = StringUtils.join(
          Arrays.copyOfRange(pathElements, 0, i + 1),
          File.separator
        );
        if (!graph.containsVertex(element)) {
          graph.addVertex(element);
        }
        if (null != lastElement && !Objects.equals(element, lastElement)) {
          if (!graph.containsEdge(lastElement, element)) {
            graph.addEdge(lastElement, element);
          }
        }

        lastElement = element;
      }
    });
    return graph;
  }

  private Double score(RevCommit commit, List<DiffEntry> allEntries) {
    // we ignore deleted entries when we score
    List<DiffEntry> entries = allEntries.stream()
      .filter(e -> !DELETE.equals(e.getChangeType()))
      .collect(Collectors.toList());
    if (entries.size() == 1) {
      return 1.0;
    }

    //
    Set<String> files = entries.stream().map(e -> identifyPath(e, config.getSourceSets())).collect(Collectors.toSet());
    MutableDouble score = new MutableDouble(0.0);

    DirectedAcyclicGraph<String, DefaultWeightedEdge> graph = buildGraph(entries);

    // each subgraph is a distinct tree
    new ConnectivityInspector<>(graph)
      .connectedSets()
      .forEach(subset -> {

        AsSubgraph<String, DefaultWeightedEdge> subgraph = new AsSubgraph<>(graph, subset);
        String root = subgraph.vertexSet().stream()
          .filter(e -> 0 == graph.inDegreeOf(e))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("No root found"));

        // prune down to only the common changed elements
        while (graph.outDegreeOf(root) == 1 && !files.contains(root)) {
          DefaultWeightedEdge edge = subgraph.outgoingEdgesOf(root).iterator().next();
          String newRoot = subgraph.getEdgeTarget(edge);
          subgraph.removeVertex(root);
          root = newRoot;
        }

        // if there are no edges then we have 1 file and nothing else
        int edges = subgraph.edgeSet().size();
        score.add(edges == 0 ? 1 : edges);
      });

    return score.doubleValue();
  }


  public List<CommitShotgun> getScores() {
    return this.commitScores;
  }
}
