# COGSI CA1

This document is part of the _COGSI_ (Configuração e Gestão de Sistemas) class from the Software Engineering master's
course on _ISEP_ (Instituto Superior de Engenharia do Porto).

This assignment was splited into two parts.

## Part I

This part focused on working with git and **no** branches.

Also, in order to make it **easier to understand** and have a **safer history**, we will make small incremental commits.
This also makes it easier to debug, review, maintain and keeps a logical flow. This approach reduces errors and enables
straightforward corrections without the need of great effort.
During the assignments it's possible to see commits based on different components of the project (Eg. models,
repositories, ui, documentation...).
Although this is not mandatory, it's a good practice.

The commands used to implement this assignment were:

1. Create folder for CA1
    1. ```mkdir CA1``` is used to create a folder with the name _CA1_
2. Copy project to new folder and create README.md
    1. ```nano README.md``` is used to create a file with the name _README_ and with the extension _.md_
3. Initial commit and tag
    1. ```git commit -a -m "Init CA1 assignment"``` is used to save the initial changes to the repository, with the
       options _**-a**_ and _**-m**_ we add the changes to the staging area and define the message of the commit "_Init
       CA1 assignment_". An alternative to this command would be:
        1. ```git add .``` in this case we first add the changes to the staging area
        2. ```git commit -m "Init CA1 assignment"``` this way we don't need the option _**-a**_ since we already added
           the changes to the staging area and only want to commit them
    2. ```git push``` is used to send the committed changes from the local repository to the remote repository
    3. ```git tag -a 1.1.0``` is used to create an annotated tag named 1.1.0. The tag is used to define this first
       milestone which is the base of CA1 assignment
    4. ```git push origin 1.1.0``` is used to push the created tag to the remote repository, since ```git push``` only
       pushes commits
4. Update Vet model
    1. Update
       the [Vet.java](./spring-framework-petclinic/src/main/java/org/springframework/samples/petclinic/model/Vet.java)
       file
    2. ```git commit -am "Add professionalLicenseNumber support to Vet model"``` here we simplify the options used
       before into a single one "_**-am**_" with the exact same behaviour
    3. ```git push```
5. Update Vet repository
    1. Update
       the [JdbcVetRepositoryImpl.java](./spring-framework-petclinic/src/main/java/org/springframework/samples/petclinic/repository/jdbc/JdbcVetRepositoryImpl.java)
       file
    2. Update the h2 database data and schemas
    3. ```git commit -am "Update Vet repository and db schemas"``` we want functional small and
    4. ```git push```
6. Update Vet UI
    1. Update
       the [vetList.jsp](/Users/ctw02859/Desktop/ISEP/COGSI/cogsi2526-1211711-1210631/CA1/spring-framework-petclinic/src/main/webapp/WEB-INF/jsp/vets/vetList.jsp)
       file
    2. ```git commit -am "Update Vet list UI"```
    3. ```git push```
7. Update previous data base schemas and data
    1. Update database data and schemas regarding owner (only the missing ones)
    2. ```git commit -am "Add NIF to Owners on all DBs"```
    3. ```git push```
8. Sanitize other Vet databases
    1. Update missing vet database data and schemas
    2. ```git commit -am "Add professionalLicenseNumber to DB's"```
    3. ```git push```
9. Tag new feature
    1. ```git tag -a 1.2.0```
    2. ```git push origin 1.2.0```
10. Revert commit
    1. ```git log --oneline``` in order to see all of the commits as well as their hashes
    2. ```git revert d9a8f77``` in order to revert the commit itself, we also could use ```git revert HEAD``` since in
       this case the HEAD was the wanted commit to be reverted
11. Update Readme
    1. Add all commands and context to the CA1 [README.md](README.md)
    2. ```git commit -am "Add explanation for context and commands. Closes #6"``` in this commit the message contains
       the keyword used to close the task with id [#6](https://github.com/1211711/cogsi2526-1211711-1210631/issues/6)
12. Create final tag
    1. ```git tag -a ca1-part1``` this tag marks the end of this part of the assignment

**Q1:** Show which is the repository’s default branch and when was its latest commit made

**R:**
```git symbolic-ref HEAD``` or ```git symbolic-ref refs/remotes/origin/HEAD``` to retrieve based
on remote.

**Q2:** Show how many distinct contributors made commits in the
repository

**R:**
```git shortlog -sn``` or ```git shortlog -n``` to validate which commits were made by each contributor.

## Part II

This part focused on working with git and **with** branches.

## Developers

| Name       | Number  | Evaluation |
|------------|:-------:|:----------:|
| João Sousa | 1210631 |    100%    |
| João Brito | 1211711 |    100%    |
