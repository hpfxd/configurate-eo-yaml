package com.hpfxd.configurate.eoyaml;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.stream.Stream;

public class YamlCommentHandler implements CommentHandler {
    private static final String COMMENT_PREFIX = "#";
    
    @Override
    public @Nullable String extractHeader(final BufferedReader reader) throws IOException {
        if (!beginsWithPrefix(reader)) {
            return null;
        }
        boolean firstLine = true;
        
        final StringBuilder build = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (firstLine) {
                if (line.length() > 0 && line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
                build.append(line);
                firstLine = false;
            } else if (line.trim().startsWith(COMMENT_PREFIX)) {
                line = line.substring(line.indexOf(COMMENT_PREFIX) + 1);
                if (line.length() > 0 && line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
                if (build.length() > 0) {
                    build.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
                }
                build.append(line);
            } else if (Strings.isBlank(line) || line.equals("---")) {
                break;
            } else {
                return null;
            }
        }
        // We've reached the end of the document?
        return build.length() > 0 ? build.toString() : null;
    }
    
    @Override
    public Stream<String> toComment(final Stream<String> lines) {
        return Stream.concat(lines
                .map(s -> {
                    if (s.length() > 0 && s.charAt(0) == ' ') {
                        return COMMENT_PREFIX + s;
                    } else {
                        return COMMENT_PREFIX + " " + s;
                    }
                }), Stream.of("---"));
    }
    
    private static boolean beginsWithPrefix(final BufferedReader reader) throws IOException {
        final CharBuffer buf = CharBuffer.allocate(COMMENT_PREFIX.length());
        if (reader.read(buf) != buf.limit()) {
            return false;
        }
        buf.flip();
        return COMMENT_PREFIX.contentEquals(buf);
    }
}
