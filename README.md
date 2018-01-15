# sf-utils
Salesforce utils (exporting metadata, objects, etc.)

## Dependecies
- Must install java to build and run this application

## Quick start
- [Install and setup Git](https://help.github.com/articles/set-up-git) on your machine
- Clone the repository: `git clone git@github.com:amind/sf-utils.git` OR download it to your desktop
- [Install and setup latest Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) on your machine
- [Install and setup Java IDE](https://netbeans.org/ , https://www.jetbrains.com/idea/) on your machine
- Build jar app using Netbeans, Intellij or other java IDEs (project is maven based thus portable between IDEs). 
- Check java dependencies to be set, like JAVA_HOME, jar execution program (must be JRE8 or higher)
- Run jar by double click, or if there's no correct configuration for JAVA_HOME and jar execution program, then use console:
  java -jar sf-utils-1.0.jar
- For connecting program to your SF instance you should provide SF credentials: username, password, security token
- SF Security token can be generated from SF ui: Setup -> Personal Setup -> My Personal Information -> Reset My Security Token
- Happy exporting!

###### Project Structure
1. Project structure is maven based. See pom.xml
2. GUI is based on javafx platform. You can find editor for fxml files here: http://www.oracle.com/technetwork/java/javase/downloads/javafxscenebuilder-info-2157684.html
3. Java libraries for SF can be found in pom.xml dependecies section