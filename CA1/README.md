# COGSI CA1

This document is part of the _COGSI_ (Configuração e Gestão de Sistemas) class from the Software Engineering master's
course on _ISEP_ (Instituto Superior de Engenharia do Porto).

This assignment was splited into two parts.

## Part I

This part focused on working with git and **no** branches.

The commands used to implement this assignment were:

1. Create folder for CA1
    1. ```mkdir CA1```
2. Copy project to new folder and create README.md
    1. ```nano README.md```
3. Initial commit and tag
    1. ```git commit -am "Init CA1 assignment"```
    2. ```git push```
    3. ```git tag -a 1.1.0```
    4. ```git push origin 1.1.0```
4. Update Vet model
    1. Update the Vet.java file
    2. ```git commit -am "Add professionalLicenseNumber support to Vet model"```
    3. ```git push```
5. Update Vet repository
    1. Update the JdbcVetRepositoryImpl.java file
    2. Update the h2 database data and schemas
    3. ```git commit -am "Update Vet repository and db schemas"```
    4. ```git push```
6. Update Vet UI
    1. Update the vetList.jsp file
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
    1. TODO

**Q1:** Show which is the repository’s default branch and when was its latest commit made

**R:** ```git symbolic-ref HEAD``` or ```git symbolic-ref refs/remotes/origin/HEAD``` to retrieve based
on remote

**Q2:** Show how many distinct contributors made commits in the
repository

**R:** ```git shortlog -sn```
or
```git shortlog -n``` to validate which commits were made by each contributor

## Part II

This part focused on working with git and **with** branches.

## Developers

| Name       | Number  | Evaluation |
|------------|:-------:|:----------:|
| João Sousa | 1210631 |    100%    |
| João Brito | 1211711 |    100%    |
