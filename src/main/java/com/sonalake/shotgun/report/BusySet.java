package com.sonalake.shotgun.report;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class BusySet {
  private int count;
  private List<String> files;
}
