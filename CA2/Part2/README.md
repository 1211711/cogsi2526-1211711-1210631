# COGSI CA2

This document is part of the _COGSI_ (Configuração e Gestão de Sistemas) class from the Software Engineering master's
course on _ISEP_ (Instituto Superior de Engenharia do Porto).

This assignment was splited into two parts. This documents is only for Part Ii.

## Part II

This part focused on working migrating a _maven_ project to **_gradle_** and keeping the good practices learned and used in the previous
assignments.
Because of that, some steps will be simplified (like _commits_ and _pull requests_) but such commands can be seen
in [CA2 README](../../CA2/README.md).

The steps used to implement this assignment were:

1. Initial project
    1. Download the [tut-rest](https://github.com/spring-guides/tut-rest) project into the
       folder [CA2/Part2/tut-rest](./tut-rest)

2. Create initial tag
    1. Experiment the downloaded project
        1. Run the app by moving to the _links_ folder and running `../mvnw spring-boot:run`
        2. Test the app by opening the url `localhost:8080/employees` in a browser
            1. Validate that the output is a JSON with the employees

3. Init gradle project
    1. Open the terminal and use `gradle init` in order to initialize a new gradle project
        1. Choose the type of project to generate: `basic`
           1. _Application_ – Sets up a runnable project with a basic main class and application structure.
           2. _Library_ – Creates a project intended to be used as a library dependency in other projects. 
           3. _Gradle Plugin_ – Prepares a project for developing and packaging custom Gradle plugins. 
           4. _Basic_ – Generates only the minimal Gradle build structure, with no source code or predefined setup.
        2. Project name (default: gradle-migration): `tut-rest`
        3. Select the build script DSL: `groovy`
            1. Groovy – The original and most widely used DSL for Gradle (build.gradle). 
            2. Kotlin – A newer, statically-typed DSL (build.gradle.kts) with better IDE support and type safety.
        4. Generate build using new APIs and behavior (some features may change in the next minor release)? (default: no) [yes, no]: `no`
           1. Yes – Uses the latest Gradle APIs and features, which may be subject to change in future releases.
           2. No – Sticks to stable and well-established Gradle features for better compatibility
        5. `Basic` was chosen for a minimal setup without extra code. `Groovy` was picked for its simplicity and wide support. 
             `No` was selected to keep the build stable and avoid experimental features.
    2. Downgrade gradle version to 8.7
        1. Open the file [gradle/wrapper/gradle-wrapper.properties](./gradle_basic_demo-main/gradle/wrapper/gradle-wrapper.properties)
        2. Change the distributionUrl to:
           `https\://services.gradle.org/distributions/gradle-8.7-bin.zip`
        3. This was done because the latest version of gradle (9,1) at the time of writing this document has some
            compatibility issues with some dependencies used in the project
   
4. Migrate project to gradle
    1. Replace the src folder created by gradle with _links_ from the _tut-rest_ project
    2. Add project dependencies to [build.gradle](./gradle_basic_demo-main/build.gradle):
        1. Configure the tools that gradle will use and apply in the project
            ```gradle
            plugins {
                id 'java'
                id 'application'
                id 'org.springframework.boot' version '3.2.5'
                id 'io.spring.dependency-management' version "1.1.5"
            }
           
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(17)
                }
            }
           
            springBoot {
                mainClass = 'payroll.PayrollApplication'
            }
           ```
            1. The `java` plugin adds support for Java projects
            2. The `application` plugin facilitates building and running Java applications
            3. The `org.springframework.boot` plugin provides Spring Boot support, simplifying the creation of
               Spring-based applications. Version 3.2.5 was the one used on the _pom.xml_ file of the _tut-rest_ project
            4. The `io.spring.dependency-management` plugin allows managing dependencies in a way that is compatible
               with Spring Boot's dependency management. Version 1.1.5 was used due to it's the compatibility with spring boot 3.2.5
            5. The `java` block configures the Java toolchain to use Java 17 for compiling and running the project,
               ensuring consistency across different development environments
            6. The `springBoot` block specifies the main class of the Spring Boot application `payroll.PayrollApplication`, which is the entry point
               when running the application
        2. The dependencies were migrated from the _pom.xml_ file of the _tut-rest_ project to the
            [build.gradle](./gradle_basic_demo-main/build.gradle) file:
             ```gradle
             dependencies {
                 implementation 'org.springframework.boot:spring-boot-starter-web'
                 implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
                 implementation 'org.springframework.boot:spring-boot-starter-hateoas'
                 implementation 'org.springframework.boot:spring-boot-starter-webflux'
           
                 testImplementation 'org.springframework.boot:spring-boot-starter-test'
           
                 runtimeOnly 'com.h2database:h2'
             }
             ```
           1. There wasn't a need to specify the versions of the dependencies because the
              `io.spring.dependency-management` plugin takes care of that, ensuring compatibility with Spring Boot 3.2.5
        3. Add project properties
            ```gradle
              group = 'org.springframework.guides'
              version = '0.0.1-SNAPSHOT'
           ```

5. Create deployment task
   1. Create `cleanDeploymentDir` task in order to clean the deployment directory before copying new files
        ```gradle
           tasks.register('cleanDeploymentDir', Delete) {
               description = 'Cleans a specific version-controlled directory using -PdirPath'
            
               def deploymentDirPath = targetDirPath()
            
               println "Cleaning contents of: $deploymentDirPath"
               delete deploymentDirPath
            
               doFirst {
                   println "Starting to clean the deployment directory: $deploymentDirPath"
               }
           }
        ```
       1. The task is of type `Delete`, which is a built-in Gradle task used to delete files and directories
       2. The path of the directory to be cleaned is retrieved by calling the `targetDirPath` function
             ```
             def targetDirPath = {
                 def path = providers.gradleProperty("dirPath").orElse("build/deployment/dev")
            
                 return path.get()
             }
           ```
           This function checks for a project property named `dirPath` passed via the command line using `-PdirPath=test/path`.
           If the property is not provided, it defaults to `build/deployment/dev`. This was created to allow flexibility in
           specifying different target directories without changing the build script
       3. Use the `delete` method to remove all contents of the specified directory
   2. Create `copySource` task in order to copy the source files to a specific directory
        ```gradle
            tasks.register('copyArtifactToDir', Copy) {
                mustRunAfter 'cleanDeploymentDir'
                description = 'Generates a jar and copies it to a given directory'
                dependsOn 'bootJar'
            
                def deploymentDir = targetDirPath()
            
                from layout.buildDirectory.file("libs/${project.name}-${project.version}.jar")
            
                into deploymentDir
            
                doFirst {
                    logger.lifecycle("Copying application artifact to: $deploymentDir")
                }
            }
        ```
         1. The task is of type `Copy`, which is a built-in Gradle task used to copy files and directories
         2. `targetDirPath` function is used again to get the target directory path
         3. The `from` keyword specifies the source file to be copied, which is the JAR file generated by the `bootJar` task
            1. The `layout.buildDirectory.file("libs/${project.name}-${project.version}.jar")` expression constructs the path to the JAR file
               using the build directory layout and the project name and version
         4. The `into` keyword specifies the destination directory where the JAR file will be copied
         5. `mustRunAfter 'cleanDeploymentDir'` is used in order to ensure that this task runs after the `cleanDeploymentDir` task
         6. `dependsOn 'bootJar'` is used to ensure that the `bootJar` task runs before this task, guaranteeing that the JAR file is created before attempting to copy it
   3. Create `copyRuntimeExternalDependencies` task in order to copy the runtime dependencies to a specific directory
        ```gradle
            tasks.register('copyRuntimeExternalDependencies', Copy) {
                mustRunAfter 'copyArtifactToDir'
                description = 'Copies a subset of runtime dependencies to /libs in a given directory'
            
                def libDir = "${targetDirPath()}/lib"
            
                def subset = providers.gradleProperty("subset") ?: null
            
                if(subset.present) {
                    from (configurations.runtimeClasspath) {
                        include "**/*${subset.get()}"
                    }
                } else {
                    println "Subset is null, all of the lib files will be copied"
                    from configurations.runtimeClasspath
                }
            
                into libDir
            
                doFirst {
                    logger.lifecycle("Copying runtime dependencies to: $libDir")
                }
            }
      ```
      1. The task is of type `Copy`, which is a built-in Gradle task used to copy files and directories
      2. `targetDirPath` function is used again to get the target directory path and append `/lib` to it
      3. The `subset` variable retrieves a project property named `subset` passed via the command line using `-Psubset=testSubset`.
         If the property is not provided, it defaults to `null`. This allows filtering the dependencies to be copied based on the provided subset
         1. If `subset` is provided, the `from` block includes only the dependencies that match the subset pattern using the `include` method 
            with a wildcard pattern
         2. If `subset` is not provided, all runtime dependencies are copied
      4. The `from` keyword specifies the source files to be copied, which are the runtime dependencies of the project
         1. `configurations.runtimeClasspath` is used to get the runtime classpath configuration, which contains all the dependencies needed at runtime
      5. The `into` keyword specifies the destination directory where the dependencies will be copied
      6. `mustRunAfter 'copyArtifactToDir'` is used in order to ensure that this task runs after the `copyArtifactToDir` task
   4. Create `copyConfigurationFiles` task in order to copy and patch configuration files to a specific directory
        ```gradle
            tasks.register("copyConfigurationFiles", Copy) {
                mustRunAfter 'copyRuntimeExternalDependencies'
                description = "Copies the configuration files to a given directory and patches them"
            
                def configDir = "${targetDirPath()}/config"
            
                from(sourceSets.main.resources.sourceDirectories)
                {
                    include "*.properties"
                    filter(ReplaceTokens, tokens: [
                            "version":  project.version.toString(),
                            "buildTimestamp": Instant.now().toString()
                    ])
                }
            
                into configDir
            
                doFirst {
                    println "Copying configuration files to: $configDir"
                }
            }
      ```
        1. The task is of type `Copy`, which is a built-in Gradle task used to copy files and directories
        2. `targetDirPath` function is used again to get the target directory path and append `/config` to it
        3. The `from` keyword specifies the source files to be copied
           1. `sourceSets.main.resources.sourceDirectories` is used to get the directories containing the resource files for the main source set
           2. The `include "*.properties"` line filters the files to include only those with a `.properties` extension
                1. This only includes the property files on the given source directories, not including subdirectories
           3. The `filter(ReplaceTokens, tokens: [...])` line applies a filter to replace tokens in the properties files with actual values
              1. The `version` token is replaced with the project's version
              2. The `buildTimestamp` token is replaced with the current timestamp using `Instant.now().toString()`
        4. The `into` keyword specifies the destination directory where the configuration files will be copied
        5. `mustRunAfter 'copyRuntimeExternalDependencies'` is used in order to ensure that this task runs after the
           `copyRuntimeExternalDependencies` task
   5. Create `deployToDev` task in order to run all the previous tasks in order
        ```gradle
            tasks.register("deployToDev") {
                description = "Full deployment task that cleans a given directory, copies the artifact, runtime dependencies and configuration files"
            
                dependsOn 'cleanDeploymentDir'
                dependsOn 'copyArtifactToDir'
                dependsOn 'copyRuntimeExternalDependencies'
                dependsOn 'copyConfigurationFiles'
            
                doFirst {
                    println "Starting full deployment..."
                }
            
                doLast {
                    println "Full deployment completed."
                }
            }
      ```
        1. This task is a regular task that doesn't extend any specific type
        2. This task will clean, copy the artifact, runtime dependencies and configuration files to a specific directory given
           by the `-PdirPath=your/path` argument
        3. It has dependencies on all the previous tasks created
             1. The `mustRunAfter` clauses in the previous tasks ensure that they run in the correct order
   6. All the tasks contain a `doFirst` block that prints a message indicating the start of the task
      and some also contain a `doLast` block that prints a message indicating the completion of the task
   7. Test the tasks by running:
        1. `./gradlew deployToDev -PdirPath=build/deployment/dev -Psubset=.jar` - This will deploy the application to the
           `build/deployment/dev` directory, copying only the runtime dependencies that include `.jar` in their names
        2. `./gradlew deployToDev` - This will deploy the application to the default `build/deployment/dev` directory, copying all runtime dependencies

6. Create a task to run the application
    1. Add the plugin `aplication`
        1. This allows running the installed distribution of the application using the script generated by the `id application` plugin.
        2. Set the main class path which indicates which class to invoke with the java command when running the project
       ```gradle
        application {
            mainClass = 'payroll.PayrollApplication'
        }
       ```
    2. Create `runArtifactScript` task to run the distribution script
         ```gradle
         tasks.register('runArtifactScript', Exec) {
             dependsOn 'installDist'
 
             if (System.properties['os.name'].toLowerCase().contains('windows')) {
                 commandLine "build/install/${project.name}/bin/${project.name}.bat"
             } else {
                 commandLine "build/install/${project.name}/bin/${project.name}"
             }
         }
         ```
        1. It uses the `Exec` task type, which can execute operating system commands
        2. The `dependsOn 'installDist' ensures that the distribution package is created before execution
        3. The script file path is platform-aware, it runs `.bat` files on Windows and regular shell scripts on remaining systems
        4. To execute the application distribution, use `./gradlew runArtifactScript`
        5. This task is not runnable since the project runs on Spring Boot which cannot be loaded via distribution scripts. Because of that we added another task that runs via the fat jar file
    3. Create `runArtifactJar` task to run the generated JAR file
         ```gradle
         tasks.register('runArtifactJar', JavaExec) {
             dependsOn 'bootJar'
 
             def jarDir = "build/libs/${project.name}-${project.version}.jar"
 
             classpath = files(jarDir)
 
             doFirst {
                 logger.lifecycle("Running the application artifact: $jarDir")
             }
         }
         ```
        1. This task allows running the Spring Boot application directly from the generated JAR file
        2. It is of type `JavaExec`, which is a built-in Gradle task for running Java programs
        3. The `dependsOn 'bootJar'` ensures that the JAR is built before the task executes
        4. The `classpath` points to the generated JAR in the `build/libs` directory
        5. To run the application, execute `./gradlew runArtifactJar`

7. Create a task to compress Javadoc
    1. Create `compressJavadoc` task to package documentation
       ```gradle
       tasks.register('compressJavadoc', Zip) {
             dependsOn javadoc
 
             from 'build/docs/javadoc'
             archiveFileName = 'JavadocBackup.zip'
 
             destinationDirectory = file('build/zips')
       }
         ```
        1. This task compresses the generated Java documentation into a single `.zip` file
        2. It uses the `Zip` task type, a built-in Gradle task for creating ZIP archives
        3. It depends on the `javadoc` task, ensuring that documentation is generated before compression
        4. The compressed file `JavadocBackup.zip` is stored in the `build/zips` directory
        5. To generate and compress documentation, execute `./gradlew compressJavadoc`

8. Create a task to run integration tests
    1. Define custom source set, to properly separate integration tests from regular unit tests
         ```gradle
         sourceSets {
             intTest {
                 java {
                     srcDir file("src/intTest/java")
                     compileClasspath += sourceSets.main.output + configurations.testRuntimeClasspath
                     runtimeClasspath += output + compileClasspath
                 }
                 resources.srcDir file("src/intTest/resources")
             }
         }
        ```
        1. This defines a separate source set for integration tests with its own Java and resource directories, while including the main application and test dependencies for compilation and runtime
    2. Configure dependencies for integration tests
         ```gradle
         configurations {
             intTestImplementation {
                 canBeConsumed = false
                 canBeResolved = true
                 extendsFrom implementation, testImplementation
             }
             intTestRuntimeOnly {
                 canBeConsumed = false
                 canBeResolved = true
                 extendsFrom runtimeOnly, testRuntimeOnly
             }
         }
         ```
        1. These configurations provide compile-time and runtime dependencies for the integration tests, inheriting from the main and test configurations
    3. Add Integration tests in file [PayrollTest.java](gradle-migration/src/intTest/java/payroll/PayrollTest.java)
    4. Create `intTest` task for integration testing
         ```gradle
         tasks.register('intTest', Test) {
             description = 'Runs the integration tests.'
             group = 'verification'
 
             useJUnitPlatform()
 
             testClassesDirs = sourceSets.intTest.output.classesDirs
             classpath = sourceSets.intTest.runtimeClasspath
 
             shouldRunAfter test
         }
         ```
        1. This task runs the integration tests located in `src/intTest/java`.
        2. It uses the `Test` task type, configured to work with the intTest source set.
        3. The `useJUnitPlatform()` method enables JUnit 5 for running the tests.
        4. The `testClassesDirs and classpath` properties are configured to point to the integration test classes and runtime dependencies.
        5. The `shouldRunAfter test` ensures that integration tests **should** run after the regular unit tests.
        6. To execute the integration tests, execute `./gradlew intTest`

9. Update documentation
    1. The [README.md](./README.md) was update, using the same pull request strategy as previously

10. Tag assignment
     1. ```git tag -a ca2-part2``` was used to tag the assignment

-------
## Alternative Solutions
An easy and quick alternative to gradle would be maven, which is the build tool used in the original project.
However, this assignment was focused on learning, and since _maven_ was already how the project was built, it made more 
sense to use another alternative like _Ant_.

Ant is a more manual and less opinionated build tool compared to Gradle, which provides more flexibility but requires more 
configuration. Apache Ant is a XML-based build automation tool that, unlike Maven and Gradle, doesn’t enforce a project structure. 
In Ant everything must be explicitly defined in a build.xml file. It's _procedural_ rather than declarative, meaning you 
control every step, manually.

In order to init an Ant project, we need to create a `build.xml` file and define targets for compiling java, copy resources, 
package, run and deploy the application.

Ant doesn't have built-in dependency management like Maven or Gradle, so we would need to manually download and manage any 
external libraries or use Apache Ivy.

A quick setup with compiling and packaging of the project would look like this:
```xml
<project name="tut-rest" default="build" basedir=".">
    <property name="src.dir"     value="src/main/java"/>
    <property name="build.dir"   value="build/classes"/>
    <property name="dist.dir"    value="build/dist"/>
    <property name="main.class"  value="payroll.PayrollApplication"/>
    <property name="jar.name"    value="tut-rest-0.0.1-SNAPSHOT.jar"/>

    <target name="clean">
        <delete dir="${build.dir}" />
        <delete dir="${dist.dir}" />
    </target>

    <target name="compile">
        <mkdir dir="${build.dir}" />
        <javac srcdir="${src.dir}" destdir="${build.dir}" includeantruntime="false" source="17" target="17" />
    </target>

    <target name="jar" depends="compile">
        <mkdir dir="${dist.dir}" />
        <jar destfile="${dist.dir}/${jar.name}" basedir="${build.dir}">
            <manifest>
                <attribute name="Main-Class" value="${main.class}" />
            </manifest>
        </jar>
    </target>

    <target name="build" depends="clean,jar"/>
</project>
```

Using Apache Ivy for dependency management, we would need to create an `ivy.xml` file to define the dependencies:
```xml
<ivy-module version="2.0">
  <info organisation="org.springframework.guides" module="tut-rest"/>
  
  <dependencies>
    <dependency org="org.springframework.boot" name="spring-boot-starter-web" rev="3.2.5"/>
    <dependency org="org.springframework.boot" name="spring-boot-starter-data-jpa" rev="3.2.5"/>
    <dependency org="org.springframework.boot" name="spring-boot-starter-hateoas" rev="3.2.5"/>
    <dependency org="org.springframework.boot" name="spring-boot-starter-webflux" rev="3.2.5"/>
      
    <dependency org="org.springframework.boot" name="spring-boot-starter-test" rev="3.2.5" conf="test->default"/>
      
    <dependency org="com.h2database" name="h2" rev="2.2.224"/>
  </dependencies>
</ivy-module>
```

This would need to also be referenced in the ´build.xml´ file, with tasks definition:
```xml
<taskdef name="ivy" classname="org.apache.ivy.ant.IvyConfigure">
    <classpath>
        <fileset dir="lib">
            <include name="ivy-*.jar"/>
        </fileset>
    </classpath>
</taskdef>

<target name="ivy-init">
    <ivy/>
</target>

<target name="resolve-dependencies" depends="ivy-init">
    <taskdef name="ivyresolve" classname="org.apache.ivy.ant.IvyResolve"/>
    <ivyresolve/>
    
    <taskdef name="ivycachepath" classname="org.apache.ivy.ant.IvyCachePath"/>
    <ivycachepath pathid="project.classpath"/>
</target>
```

1. The `taskdef` element defines a new task named `ivy` that uses the `IvyConfigure` class from the Ivy library
2. The `resolve-dependencies` target depends on the `ivy-init` target to ensure that Ivy is initialized before resolving dependencies
3. The `ivyresolve` task resolves the dependencies defined in the `ivy.xml` file
4. The `ivycachepath` task creates a classpath reference named `project.classpath` that includes all resolved dependencies

In order to replicate the first set of tasks created in gradle, we would need to create more targets in the `build.xml` file.

```xml
<target name="clean-deployment-dir">
    <delete dir="build/deployment/dev" />
</target>

<target name="copy-artifact-to-dir" depends="jar">
    <mkdir dir="build/deployment/dev" />
    <copy file="${dist.dir}/${jar.name}" todir="build/deployment/dev" />
</target>

<target name="copy-runtime-external-dependencies">
    <mkdir dir="build/deployment/dev/lib" />
    <copy todir="build/deployment/dev/lib">
        <fileset dir="lib" includes="**/*.jar" />
    </copy>
</target>

<target name="copy-configuration-files">
    <mkdir dir="build/deployment/dev/config" />
    <copy todir="build/deployment/dev/config">
        <fileset dir="src/main/resources">
            <include name="*.properties" />
        </fileset>
        <filterset>
            <filter token="version" value="0.0.1-SNAPSHOT" />
            <filter token="buildTimestamp" value="${DSTAMP} ${TSTAMP}" />
        </filterset>
    </copy>
</target>

<target name="deployToDev" depends="clean-deployment,copy-jar,copy-libs,copy-config">
    <echo message="Deployment completed successfully."/>
</target>
```

A target is a set of tasks to be executed. Targets can depend on other targets, allowing us to create a sequence of operations, 
similar to the `dependsOn` and `mustRunAfter` clauses in gradle. Unlike gradle, in Ant the order of the target dependencies is the order of execution.

Regarding the java/javadoc/testing tasks, they would look like this on Ant:

```xml
<target name="run-artifact-jar" depends="resolve-dependencies,compile">
    <java fork="true" classname="${main.class}"/>
</target>
```

1. The target depends on the `resolve-dependencies` and `compile` targets to ensure that dependencies are resolved and the project is compiled before running
2. The `java` task is used to run a Java application, with the `fork="true"` attribute indicating that the application should be run in a separate process
3. The `classpath` element defines the classpath for the Java application, including both the resolved dependencies and the compiled classes

```xml
<target name="run-artifact-script" depends="install-dist">
    <condition property="is.windows" value="true" else="false">
        <os family="windows"/>
    </condition>
    
    <exec executable="${basedir}/build/install/${ant.project.name}/bin/${ant.project.name}${is.windows == 'true' ? '.bat' : ''}" failonerror="true"/>
</target>
```

1. The target depends on the `install-dist` target to ensure that the distribution package is created before execution
2. The `condition` element sets a property `is.windows` to `true` if the operating system is Windows, and `false` otherwise
3. The `exec` task is used to execute an external command, with the `executable` attribute specifying the path to the script (.bat if windows)

```xml
<target name="javadoc" depends="resolve-dependencies">
    <javadoc sourcepath="${src.dir}" destdir="build/docs/javadoc" />
</target>

<target name="compress-javadoc" depends="javadoc">
    <mkdir dir="build/zips" />
    <zip destfile="build/zips/JavadocBackup.zip" basedir="build/docs/javadoc" />
</target>
```

1. The `javadoc` target generates Java documentation using the `javadoc` task, specifying the source path and destination directory
2. The `compress-javadoc` target depends on the `javadoc` target to ensure that documentation is generated before compression
3. The `compress-javadoc` target uses the `zip` task to create a ZIP archive of the generated documentation

```xml
<property name="inttest.dir" value="src/intTest/java"/>

<target name="int-test" depends="compile">
    <junit printsummary="on" haltonfailure="false" fork="true">
        <classpath>
            <pathelement path="${build.dir}"/>
        </classpath>
        <batchtest>
            <fileset dir="${inttest.dir}">
                <include name="**/*Test.java"/>
            </fileset>
        </batchtest>
    </junit>
</target>
```

1. The `int-test` target runs integration tests using the `junit` task, which is part of Ant
2. The target depends on the `compile` target to ensure that the project is compiled before running the tests
    1. The `printsummary="on"` attribute enables a summary of the test results to be printed after execution
    2. The `haltonfailure="false"` attribute causes the build to continue even if any test fails
    3. The `fork="true"` attribute indicates that the tests should be run in a separate process
3. The `classpath` element defines the classpath for the JUnit tests
4. The `batchtest` element specifies a set of test classes to run, using a `fileset` to include all Java files in the 
`src/intTest/java` directory that match the pattern `**/*Test.java`

In order to execute any of the previous tasks/targets, we would need to run `ant <target-name>`.

--------

## Developers

| Name       | Number  | Evaluation |
|------------|:-------:|:----------:|
| João Sousa | 1210631 |    100%    |
| João Brito | 1211711 |    100%    |

[1]: https://docs.gradle.org/current/userguide/gradle_directories_intermediate.html "Gradle Build explanation"

