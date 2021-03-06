package com.sonalake.shotgun.usage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CommitShotgun {
  @JsonIgnore
  private LocalDate commitDate;
  private String committer;
  private String commit;
  private String message;
  private Double score;
  private Integer size;
  @Singular
  private List<CommitEntry> entries;
}
