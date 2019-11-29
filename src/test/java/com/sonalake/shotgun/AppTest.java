package com.sonalake.shotgun;

import com.beust.jcommander.ParameterException;
import com.sonalake.shotgun.git.GitDiffer;
import com.sonalake.shotgun.git.GitException;
import com.sonalake.shotgun.usage.ShotgunConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppTest {

  @Spy
  private App app;

  @Mock
  private GitDiffer differ;

  @Test
  public void testNoArgsDoesNotBreak() {
    // given we pass no args
    app.execute();

    // we should get no exceptions, and the differ should not be called
    verifyNoInteractions(differ);
  }

  @Test
  public void testHelpWorks() {
    // given we pass no args
    app.execute("-h");

    // we should get no exceptions, and the differ should not be called
    verifyNoInteractions(differ);
  }


  @Test
  public void testInputRequired() {
    // given we pass no args
    ParameterException observed = assertThrows(ParameterException.class, () -> app.execute("-s", "somesource"));
    assertEquals("The following option is required: [-i | --input-dir]", observed.getMessage());
  }

  @Test
  public void testExecution(@TempDir Path sample) {
    // give we have our mock
    doReturn(differ).when(app).buildGitDiffer(any());

    // and pass in the mandatory stuff
    app.execute("-i", sample.toAbsolutePath().toString());

    // then we execute
    verify(differ).scanRepo(any());

  }

  @Test
  public void testGitDifferFactory(@TempDir Path sample) {
    // give we have our mock
    GitDiffer localDiffer = app.buildGitDiffer(ShotgunConfig.builder().inputDirectory(sample).build());

    // when we try to process we should fail because this directory is not real
    GitException observed = assertThrows(GitException.class, localDiffer::scanRepo);
    assertEquals("No git repo in: " + sample, observed.getMessage());
  }
}
