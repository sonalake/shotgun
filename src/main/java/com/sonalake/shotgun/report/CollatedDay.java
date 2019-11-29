package com.sonalake.shotgun.report;

import com.sonalake.shotgun.usage.CommitShotgun;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class CollatedDay {
  private String date;
  private Long epochSecond;
  private String score;
  private List<CommitShotgun> commits;
}
