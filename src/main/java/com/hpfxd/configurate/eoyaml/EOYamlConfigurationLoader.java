package com.hpfxd.configurate.eoyaml;

import com.amihaiemil.eoyaml.Comment;
import com.amihaiemil.eoyaml.Node;
import com.amihaiemil.eoyaml.Scalar;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A loader for YAML-formatted configurations, using the
 * <a href="https://github.com/decorators-squad/eo-yaml">eo-yaml</a> library
 * for parsing and generation.
 *
 * @author hpfxd
 */
public class EOYamlConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {
    /**
     * All values are internally represented as a {@link String}.
     */
    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(String.class);
    
    /**
     * Creates a new {@link EOYamlConfigurationLoader} builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, EOYamlConfigurationLoader> {
        private boolean guessIndentation = false;
        
        /**
         * Set whether we should try to guess indentation of misplaced lines.
         * <p>
         * If disabled, an exception is thrown if indentation is not correct.<br>
         * The default value is {@code false}.
         * </p>
         * <p>
         * See Also: <a href="https://github.com/decorators-squad/eo-yaml/wiki/Validation-of-Indentation">
         * eo-yaml wiki: Validation of Indentation</a>
         *
         * @param guessIndentation Whether we should try to guess indentation of misplaced lines.
         * @return this builder
         */
        public Builder guessIndentation(final boolean guessIndentation) {
            this.guessIndentation = guessIndentation;
            return this;
        }
        
        @Override
        public EOYamlConfigurationLoader build() {
            defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            
            return new EOYamlConfigurationLoader(this);
        }
    }
    
    private final boolean guessIndentation;
    
    private EOYamlConfigurationLoader(Builder build) {
        super(build, new CommentHandler[]{ new YamlCommentHandler() });
        
        this.guessIndentation = build.guessIndentation;
    }
    
    @Override
    protected void checkCanWrite(ConfigurationNode node) throws ConfigurateException {
        if (!node.isMap() && !node.virtual() && node.raw() != null) {
            throw new ConfigurateException(node, "Can only write nodes that are in map format!");
        }
    }
    
    @Override
    protected void loadInternal(CommentedConfigurationNode node, BufferedReader reader) throws ParsingException {
        // eo-yaml doesn't support reading from a Reader, so first we convert the content into a string.
        // it would also be possible to wrap an InputStream around a Reader, but this is good enough for now :D
        String content = reader.lines()
                .collect(Collectors.joining(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR));
    
        YamlMapping mapping;
        
        try {
            // attempt to parse mapping from content
            mapping = Yaml.createYamlInput(content, this.guessIndentation).readYamlMapping();
        } catch (IOException e) {
            throw ParsingException.wrap(node, e);
        }
        
        // mapping was parsed successfully, read into root node
        readNode(mapping, node);
    }
    
    private void readNode(@NonNull YamlNode yamlNode, @NonNull CommentedConfigurationNode node) throws ParsingException {
        Comment comment = yamlNode.comment();
        
        // check that comment exists
        if (comment != null && comment.value() != null && !comment.value().isEmpty()) {
            // set comment on configurate node
            node.comment(comment.value()
                    // normalize line separators
                    .replace("\r\n", AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR));
        }
        
        switch (yamlNode.type()) {
            case MAPPING:
                YamlMapping mapping = yamlNode.asMapping();
                
                if (mapping.isEmpty()) {
                    node.raw(Collections.emptyMap());
                } else {
                    for (YamlNode key : mapping.keys()) {
                        YamlNode value = mapping.value(key);
                        
                        if (key.type() != Node.SCALAR) {
                            // disallow complex keys (for now at least)
                            throw new ParsingException(node, -1, -1, null,
                                    "Complex keys not allowed!", null);
                        }
    
                        // yaml node -> string
                        String k = ((Scalar) key).value();
                        
                        // recursively read nodes
                        this.readNode(value, node.node(k));
                    }
                }
                break;
                
            case SEQUENCE:
                YamlSequence sequence = yamlNode.asSequence();
                
                if (sequence.isEmpty()) {
                    node.raw(Collections.emptyList());
                } else {
                    int i = 0;
                    for (YamlNode value : sequence.values()) {
                        // recursively read children
                        this.readNode(value, node.node(i));
                        
                        i++;
                    }
                }
                break;
                
            case SCALAR:
                // write scalar value to node
                node.raw(yamlNode.asScalar().value());
                break;
        }
    }
    
    @Override
    protected void saveInternal(@NonNull ConfigurationNode node, @NonNull Writer writer) throws ConfigurateException {
        try {
            if (!node.isMap() && (node.virtual() || node.raw() == null)) {
                writer.write(SYSTEM_LINE_SEPARATOR);
                return;
            }
            
            // write node to a yamlNode
            YamlNode yamlNode = this.writeNode(node);
    
            // write the yaml to the writer
            // note: print() will close the writer, but is fine because this is the last time it's used
            Yaml.createYamlPrinter(writer).print(yamlNode);
        } catch (IOException e) {
            throw ConfigurateException.wrap(node, e);
        }
    }
    
    private YamlNode writeNode(@NonNull ConfigurationNode node) {
        // the node's comment, or an empty string if not set
        String comment = "";
        
        if (node instanceof CommentedConfigurationNodeIntermediary<?>) {
            // holy shit this class name is annoying me
            CommentedConfigurationNodeIntermediary<?> commentedNode = (CommentedConfigurationNodeIntermediary<?>) node;
            
            String c = commentedNode.comment();
            if (c != null && !c.isEmpty()) {
                // interesting; eo-yaml seems to only support multi-line
                // comments if they're separated using the system line separator
                comment = c.replaceAll("\\r?\\n", AbstractConfigurationLoader.SYSTEM_LINE_SEPARATOR);
            }
        }
        
        if (node.isMap()) {
            YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
    
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                String key = String.valueOf(entry.getKey());
                ConfigurationNode childNode = entry.getValue();
                
                // recursively call method
                builder = builder.add(key, this.writeNode(childNode));
            }
            
            return builder.build(comment);
        } else if (node.isList()) {
            YamlSequenceBuilder builder = Yaml.createYamlSequenceBuilder();
    
            for (ConfigurationNode child : node.childrenList()) {
                // recursively call method
                builder = builder.add(this.writeNode(child));
            }
            
            return builder.build(comment);
        } else {
            return Yaml.createYamlScalarBuilder()
                    .addLine(String.valueOf(node.rawScalar()))
        
                    // inline comments not currently supported, so set as empty
                    .buildPlainScalar(comment, "");
        }
    }
    
    @Override
    public CommentedConfigurationNode createNode(ConfigurationOptions options) {
        return CommentedConfigurationNode.root(options.nativeTypes(NATIVE_TYPES));
    }
}
