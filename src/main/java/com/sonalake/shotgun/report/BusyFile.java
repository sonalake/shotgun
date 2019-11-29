package com.sonalake.shotgun.report;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class BusyFile {
  private int count;
  private String file;
}
