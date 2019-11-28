package com.sonalake.shotgun.cli;

import com.beust.jcommander.JCommander;
import com.sonalake.shotgun.usage.ShotgunConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParamsTest {

    @Test
    public void testParser() {
        String[] args = {
                "-i", "here",
                "-o", "there",
                "-s", "set1",
                "-s", "set2",
                "-m", "99",
                "-cf", "100",
                "-cs", "101",
                "-ll", "100",
                "-ll", "50",
        };
        Params params = new Params();
        JCommander parser = JCommander.newBuilder()
                .addObject(params)
                .build();
        parser.parse(args);

        ShotgunConfig config = params.toConfig();
        assertEquals(Path.of("here"), config.getInputDirectory());
        assertEquals(Path.of("there"), config.getOutputFile());
        assertEquals(asList("set1", "set2"), config.getSourceSets());
        assertEquals(99, config.getMinimumCommitInterest());
        assertEquals(100, config.getTopCommitValueForFiles());
        assertEquals(101, config.getTopCommitValueForFileSets());
        assertEquals(asList(50, 100), config.getLegendLevels());

    }
}
