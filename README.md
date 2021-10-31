# configurate-eo-yaml

YAML configuration loader for [Configurate](https://github.com/SpongePowered/Configurate/)
using [eo-yaml](https://github.com/decorators-squad/eo-yaml/).

## Why?

Currently in Configurate, the YAML loader which is implemented using SnakeYAML does not support writing comments.

The only loaders that support round-tripping comments are Hocon and XML. I personally find XML to be a bit annoying to
use for configurations (looking at you Maven...), and the Hocon loader uses a Map implementation internally that does
not preserve the ordering of entries, which means when saving, the options are all over the place.

eo-yaml both properly preserves entry ordering, and supports round-tripping comments. :)

## Usage

This project is published to [repo.hpfxd.com](https://repo.hpfxd.com/releases/). If you're using Maven or Gradle, just
add the following to your build script.

**Gradle**
```kotlin
repositories {
    maven(url = "https://repo.hpfxd.com/releases/")
}

dependencies {
    implementation("com.hpfxd.configurate:configurate-eo-yaml:1.0.0")
}
```

**Maven**
```xml
<repositories>
    <repository>
        <id>hpfxd-repo</id>
        <url>https://repo.hpfxd.com/releases/</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.hpfxd.configurate</groupId>
    <artifactId>configurate-eo-yaml</artifactId>
    <version>1.0.0</version>
</dependency>
</dependencies>
```

Once you have the library imported into your project, you can just use it like any other Configurate loader:
```java
EOYamlConfigurationLoader loader = EOYamlConfigurationLoader.builder()
        .file(new File("./config.yml"))
        .build();
```

The loader only has one unique option: **guessIndentation**. 
See [the eo-yaml wiki](https://github.com/decorators-squad/eo-yaml/wiki/Validation-of-Indentation) for more information.
You can set this option by calling `guessIndentation(boolean)` on the builder.
By default, this option is set to `false`.
