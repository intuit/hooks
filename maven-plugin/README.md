# Maven Kotlin Plugin Extension

At the moment, the Maven extension is not complete and only helps to register the Kotlin compiler plugin and partially configure the generated source directory. You will still be required to add the appropriate dependencies and add the generated source directory to your source sets.

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
        <!-- Arrow annotations used in code gen -->
        <dependency>
            <groupId>io.arrow-kt</groupId>
            <artifactId>arrow-annotations</artifactId>
            <version>0.11.0</version>
            <scope>provided</scope>
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
                    <!-- Configure main source and generated source directories -->
                    <sourceDirs>
                        <source>${project.basedir}/src/main/kotlin</source>
                        <source>${build.directory}/generated/source/kapt/main</source>
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