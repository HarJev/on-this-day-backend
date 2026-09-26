package com.onthisday.quiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Balances the fixed quiz type/difficulty matrix using a small min-cost flow. */
public class QuizCandidateSelector {

  public List<QuizQuestionCandidate> select(
      List<QuizQuestionCandidate> orderedCandidates, int questionCount) {
    return selectAdditional(orderedCandidates, List.of(), questionCount);
  }

  public List<QuizQuestionCandidate> selectAdditional(
      List<QuizQuestionCandidate> orderedCandidates,
      List<QuizQuestionCandidate> establishedPrefix,
      int targetQuestionCount) {
    QuizRules.requireSupportedQuestionCount(targetQuestionCount);
    var candidates = validatedCopy(orderedCandidates, "orderedCandidates");
    var prefix = validatedCopy(establishedPrefix, "establishedPrefix");
    requireDisjoint(candidates, prefix);

    var additionalCount = targetQuestionCount - prefix.size();
    if (additionalCount < 0) {
      throw new InvalidQuizDefinitionException(
          "establishedPrefix cannot exceed targetQuestionCount");
    }
    if (candidates.size() < additionalCount) {
      throw new InsufficientQuizQuestionsException(
          targetQuestionCount, prefix.size() + candidates.size());
    }
    if (additionalCount == 0) {
      return List.of();
    }

    var typeTargets = remainingTypeTargets(targetQuestionCount, prefix);
    var difficultyTargets = remainingDifficultyTargets(targetQuestionCount, prefix);
    return allocate(candidates, additionalCount, targetQuestionCount, typeTargets, difficultyTargets);
  }

  private static List<QuizQuestionCandidate> allocate(
      List<QuizQuestionCandidate> candidates,
      int additionalCount,
      int targetQuestionCount,
      Map<QuestionType, Integer> typeTargets,
      Map<QuizDifficulty, Integer> difficultyTargets) {
    var typeCount = QuestionType.values().length;
    var difficultyCount = QuizDifficulty.values().length;
    var source = 0;
    var firstType = 1;
    var firstDifficulty = firstType + typeCount;
    var sink = firstDifficulty + difficultyCount;
    var flow = new MinCostFlow(sink + 1);

    // These bounded integer weights encode the documented lexicographic fallback:
    // combined deviation, then type deviation, then supplied candidate rank.
    long rankBound = (long) additionalCount * Math.max(1, candidates.size()) + 1;
    long typeTieCost = rankBound;
    long primaryDeviationCost = (long) (targetQuestionCount + 2) * typeTieCost + rankBound;

    for (var type : QuestionType.values()) {
      var target = typeTargets.getOrDefault(type, 0);
      flow.addEdge(source, firstType + type.ordinal(), target, 0);
      flow.addEdge(
          source,
          firstType + type.ordinal(),
          additionalCount,
          primaryDeviationCost + typeTieCost);
    }
    for (var difficulty : QuizDifficulty.values()) {
      var target = difficultyTargets.getOrDefault(difficulty, 0);
      flow.addEdge(firstDifficulty + difficulty.ordinal(), sink, target, 0);
      flow.addEdge(
          firstDifficulty + difficulty.ordinal(),
          sink,
          additionalCount,
          primaryDeviationCost);
    }

    var candidateEdges = new ArrayList<MinCostFlow.Edge>(candidates.size());
    for (var rank = 0; rank < candidates.size(); rank++) {
      var candidate = candidates.get(rank);
      candidateEdges.add(
          flow.addEdge(
              firstType + candidate.type().ordinal(),
              firstDifficulty + candidate.difficulty().ordinal(),
              1,
              rank));
    }

    flow.send(source, sink, additionalCount);
    var selected = new ArrayList<QuizQuestionCandidate>(additionalCount);
    for (var rank = 0; rank < candidateEdges.size(); rank++) {
      if (candidateEdges.get(rank).capacity == 0) {
        selected.add(candidates.get(rank));
      }
    }
    if (selected.size() != additionalCount) {
      throw new QuizUnavailableException("Could not allocate the requested quiz questions.");
    }
    return List.copyOf(selected);
  }

  private static Map<QuestionType, Integer> remainingTypeTargets(
      int targetQuestionCount, List<QuizQuestionCandidate> prefix) {
    var remaining = new java.util.EnumMap<QuestionType, Integer>(QuestionType.class);
    remaining.putAll(QuizRules.questionTypeTargets(targetQuestionCount));
    for (var candidate : prefix) {
      remaining.computeIfPresent(
          candidate.type(), (ignored, count) -> Math.max(0, count - 1));
    }
    return Map.copyOf(remaining);
  }

  private static Map<QuizDifficulty, Integer> remainingDifficultyTargets(
      int targetQuestionCount, List<QuizQuestionCandidate> prefix) {
    var remaining = new java.util.EnumMap<QuizDifficulty, Integer>(QuizDifficulty.class);
    remaining.putAll(QuizRules.difficultyTargets(targetQuestionCount));
    for (var candidate : prefix) {
      remaining.computeIfPresent(
          candidate.difficulty(), (ignored, count) -> Math.max(0, count - 1));
    }
    return Map.copyOf(remaining);
  }

  private static List<QuizQuestionCandidate> validatedCopy(
      List<QuizQuestionCandidate> candidates, String fieldName) {
    if (candidates == null) {
      throw new InvalidQuizDefinitionException(fieldName + " must not be null");
    }
    var copy = List.copyOf(candidates);
    Set<String> ids = new HashSet<>();
    for (var candidate : copy) {
      if (candidate == null) {
        throw new InvalidQuizDefinitionException(fieldName + " must not contain null entries");
      }
      if (!ids.add(candidate.questionId())) {
        throw new InvalidQuizDefinitionException(fieldName + " must not contain duplicate IDs");
      }
    }
    return copy;
  }

  private static void requireDisjoint(
      List<QuizQuestionCandidate> candidates, List<QuizQuestionCandidate> prefix) {
    var ids = new HashSet<String>();
    prefix.forEach(candidate -> ids.add(candidate.questionId()));
    var overlap =
        candidates.stream().map(QuizQuestionCandidate::questionId).filter(ids::contains).toList();
    if (!overlap.isEmpty()) {
      throw new InvalidQuizDefinitionException(
          "orderedCandidates and establishedPrefix must not overlap: "
              + String.join(", ", overlap));
    }
  }

  private static final class MinCostFlow {

    private final List<List<Edge>> graph;

    private MinCostFlow(int nodeCount) {
      graph = new ArrayList<>(nodeCount);
      for (var node = 0; node < nodeCount; node++) {
        graph.add(new ArrayList<>());
      }
    }

    private Edge addEdge(int from, int to, int capacity, long cost) {
      var forward = new Edge(to, graph.get(to).size(), capacity, cost);
      var reverse = new Edge(from, graph.get(from).size(), 0, -cost);
      graph.get(from).add(forward);
      graph.get(to).add(reverse);
      return forward;
    }

    private void send(int source, int sink, int amount) {
      for (var sent = 0; sent < amount; sent++) {
        var distance = new long[graph.size()];
        Arrays.fill(distance, Long.MAX_VALUE);
        distance[source] = 0;
        var previousNode = new int[graph.size()];
        var previousEdge = new int[graph.size()];

        for (var pass = 1; pass < graph.size(); pass++) {
          var changed = false;
          for (var node = 0; node < graph.size(); node++) {
            if (distance[node] == Long.MAX_VALUE) {
              continue;
            }
            for (var edgeIndex = 0; edgeIndex < graph.get(node).size(); edgeIndex++) {
              var edge = graph.get(node).get(edgeIndex);
              var nextDistance = distance[node] + edge.cost;
              if (edge.capacity > 0 && nextDistance < distance[edge.to]) {
                distance[edge.to] = nextDistance;
                previousNode[edge.to] = node;
                previousEdge[edge.to] = edgeIndex;
                changed = true;
              }
            }
          }
          if (!changed) {
            break;
          }
        }
        if (distance[sink] == Long.MAX_VALUE) {
          throw new QuizUnavailableException("Could not allocate the requested quiz questions.");
        }

        for (var node = sink; node != source; node = previousNode[node]) {
          var edge = graph.get(previousNode[node]).get(previousEdge[node]);
          edge.capacity--;
          graph.get(node).get(edge.reverseIndex).capacity++;
        }
      }
    }

    private static final class Edge {
      private final int to;
      private final int reverseIndex;
      private int capacity;
      private final long cost;

      private Edge(int to, int reverseIndex, int capacity, long cost) {
        this.to = to;
        this.reverseIndex = reverseIndex;
        this.capacity = capacity;
        this.cost = cost;
      }
    }
  }
}
