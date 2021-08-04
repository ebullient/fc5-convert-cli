# fc5-convert-cli

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## To just run: 

1. Install JBang: https://www.jbang.dev/documentation/guide/latest/installation.html
2. Install the snapshot jar: 
```
jbang app install --name fc5-convert --force --fresh https://jitpack.io/dev/ebullient/fc5-convert-cli/1.0.0-SNAPSHOT/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar
```
3. Run the command: 
```
fc5-convert --help
```

## To build: 

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. `java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar --help`

This hacky project goes alongside https://github.com/kinkofer/FightClub5eXML

I like using both Fight Club 5 and Game Master 5 (more the former than the latter these days, because Roll20..), and for the game I run, I like being able to control/rebuild what is visible and in the compendium for my players (e.g. to constrain sources).

I also use [Obsidian](https://obsidian.md) to keep track of my notes. The goal is to use the same filtered resources as a reference both in my local notes (Obsidian) and in Fight Club 5 (Players).

1. Clone https://github.com/kinkofer/FightClub5eXML

2. ./mvnw clean install

## Next steps:

1. Look in the Collections directory of the Fight Club repository to see how collections work. Choose one of those or make your own.

2. Validate your Collection using the xsd: 
    ```
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar validate --help
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar validate -s FightClub5eXML/Utilities/collection.xsd FightClub5eXML/Collections/CoreRulebooks.xml
    ```

3. Merge and transform the collected XML documents using XSLT 2.0 (a default xslt file is in src/main/resources):
    ```
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar transform --help
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar transform -o target -x  '-merged' FightClub5eXML/Collections/CoreRulebooks.xml
    ```
    This will create `target/CoreRulebooks-merged.xml`

4. Convert the merged XML document to markdown (🚧 in progress 🚧). For testing/tooling around purposes, you can tool around with test files:
    ```
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar convert --help
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar convert -o target/reference target/reference src/test/resources/backgroundAcolyte.xml
    java -jar target/convert-cli-1.0.0-SNAPSHOT-runner.jar convert -o target/reference target/CoreRulebooks-merged.xml
    ```
