# COGSI CA2

This document is part of the _COGSI_ (Configuração e Gestão de Sistemas) class from the Software Engineering master's
course on _ISEP_ (Instituto Superior de Engenharia do Porto).

This assignment was splited into two parts. This documents is only for Part Ii.

## Part I

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
            ```
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
            ```
              group = 'org.springframework.guides'
              version = '0.0.1-SNAPSHOT'
           ```

5. Create deployment task
   1. Create `cleanDeploymentDir` task in order to clean the deployment directory before copying new files
        ```
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
       2. Use the `delete` method to remove all contents of the specified directory
   2. Create `copySource` task in order to copy the source files to a specific directory
        ```
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
        ```
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
        ```
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
        ```
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



8. Update documentation
    1. The [README.md](./README.md) was update, using the same pull request strategy as previously

9. Tag assignment
    1. ```git tag -a ca2-part1``` was used to tag the assignment

-------
**Q1:** Explain how the _**Gradle Wrapper**_ and the _**JDK Toolchain**_ ensure the correct versions of _Gradle_ and the
_Java Development Kit_ are used without requiring manual installation.

**R:**
The Gradle Wrapper automatically downloads and runs the exact Gradle version specified by the project, eliminating the
need for developers to manually install or configure Gradle. The JDK Toolchain, configured in build.gradle, specifies
the required Java version for compilation and execution, and Gradle automatically selects a compatible JDK on the system
or downloads one if necessary.
--------
**Q2:** In the root directory of the application, run `./gradlew –q javaToolchain` and explain the output

**R:**
Running the above command gradle fails because the option is placed badly. But if you fix the command to
`./gradlew javaToolchain -q` it will output the information about the Java toolchains that Gradle will use for this
project. More specifically prints the vendor, version and installation path of the JDK used by gradle.
![javaToolChain Command](./img/javaToolChain.png)

--------

## Developers

| Name       | Number  | Evaluation |
|------------|:-------:|:----------:|
| João Sousa | 1210631 |    100%    |
| João Brito | 1211711 |    100%    |

[1]: https://docs.gradle.org/current/userguide/gradle_directories_intermediate.html "Gradle Build explanation"

