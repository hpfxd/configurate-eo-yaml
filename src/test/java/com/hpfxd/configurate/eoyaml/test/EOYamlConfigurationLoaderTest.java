package com.hpfxd.configurate.eoyaml.test;

import com.hpfxd.configurate.eoyaml.EOYamlConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EOYamlConfigurationLoaderTest {
    @Test
    public void simpleLoadingTest() throws IOException {
        ConfigurationNode node = this.loader("example.yml", null).load();
        assertEquals("yes", node.node("test", "does-it-work").raw());
    }
    
    @Test
    public void commentLoadingTest() throws IOException {
        CommentedConfigurationNode node = this.loader("comments.yml", null).load();
        
        assertEquals("comment 1", node.node("test-mapping").comment(), "Mapping comment");
        assertEquals("comment 2", node.node("test-mapping", "test-scalar").comment());
        
        assertEquals("comment 3", node.node("test-sequence").comment());
        assertEquals("comment 4", node.node("test-sequence", 0).comment());
        
        assertEquals("comment line 1\ncomment line 2", node.node("multiple-lines").comment());
        
        assertNull(node.node("no-comment").comment());
    }
    
    @Test
    public void headerTest(@TempDir Path tempDir) throws IOException {
        Path outFile = tempDir.resolve("header-test.yml");
        
        EOYamlConfigurationLoader loader = this.loader(null, outFile);
        CommentedConfigurationNode node = loader.createNode(ConfigurationOptions.defaults()
                .header("this is a header\nheader line 2"));
        
        node.node("hello").comment("this is not a header\nnot header line 2").set("hi");
        node.node("nested", "hello").comment("this is not a header\nnot header line 2").set("hi");
        
        loader.save(node);
        
        List<String> expectedLines = this.getResourceLines("header.yml");
        List<String> testLines = Files.readAllLines(outFile, StandardCharsets.UTF_8);
        
        assertEquals(expectedLines, testLines);
    }
    
    private EOYamlConfigurationLoader loader(@Nullable String resource, @Nullable Path outputFile) {
        EOYamlConfigurationLoader.Builder builder = EOYamlConfigurationLoader.builder();
        
        if (resource != null) {
            builder.url(this.requireResource(resource));
        }
        
        if (outputFile != null) {
            builder.path(outputFile);
        }
        
        return builder.build();
    }
    
    private @NonNull List<@NonNull String> getResourceLines(String path) throws IOException {
        try (InputStream in = this.requireResource(path).openStream()) {
            return new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.toList());
        }
    }
    
    private @NonNull URL requireResource(String path) {
        final @Nullable URL resource = this.getClass().getResource('/' + path);
        assertNotNull(resource, () -> "Resource " + path + " was not present when expected to be!");
        return resource;
    }
}
