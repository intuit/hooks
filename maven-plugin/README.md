# Maven Kotlin Plugin Extension

> **Warning**
>
> The Maven Kotlin plugin automatically bundles a specific version of the KSP plugin, which is tied to a specific version of Kotlin (can be found [here](./settings.gradle.kts#19)). This means the Gradle plugin is only compatible with projects that use that specific Kotlin version. At some point, this module will be upgraded to publish in accordance to the KSP/Kotlin version it bundles.

At the moment, the Maven extension is not complete and only helps to register the KSP plugin and partially configure the generated source directory. You will still be required to add the appropriate dependencies and add the generated source directory to your source sets.

### Installation

```xml
<project>
    <properties>
        <hooks.version>latest version</hooks.version>
    </properties>

    <dependencies>
        <!-- Hooks runtime -->
        <dependency>
            <groupId>com.intuit.hooks</groupId>
            <artifactId>hooks</artifactId>
            <version>${hooks.version}</version>
        </dependency>
    
        <!-- Other dependencies... -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <compilerPlugins>
                        <!-- Apply hooks plugin -->
                        <plugin>hooks</plugin>
                    </compilerPlugins>
                    <!-- Configure generated source directories -->
                    <sourceDirs>
                        <source>${project.basedir}/src/main/kotlin</source>
                    </sourceDirs>
                </configuration>
                <!-- Add hooks maven plugin to compiler classpath -->
                <dependencies>
                    <dependency>
                        <groupId>com.intuit.hooks</groupId>
                        <artifactId>maven-plugin</artifactId>
                        <version>${hooks.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```