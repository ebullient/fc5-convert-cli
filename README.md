# fc5-convert-cli

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

This hacky project goes alongside https://github.com/kinkofer/FightClub5eXML

I like using both Fight Club 5 and Game Master 5 (more the former than the latter these days, because Roll20..), and for the game I run, I like being able to control/rebuild what is visible and in the compendium for my players (e.g. to constrain sources).

I also use [Obsidian](https://obsidian.md) to keep track of my notes. The goal is to use the same filtered resources as a reference both in my local notes (Obsidian) and in Fight Club 5 (Players).

## To build (optional)

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. `java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar --help`

## To run without building yourself

1. Install JBang: https://www.jbang.dev/documentation/guide/latest/installation.html
2. Install the snapshot jar: 
```
jbang app install --name fc5-convert --force --fresh https://jitpack.io/dev/ebullient/fc5-convert-cli/1.0.0-SNAPSHOT/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar
```
3. Run the command: 
```
fc5-convert --help
```

## Next steps:

1. Grab a copy of the FC5 XML repo: Clone https://github.com/kinkofer/FightClub5eXML

2. Look in the Collections directory of the Fight Club repository to see how collections work. Choose one of those or make your own.

3. Validate the Collection (against a built-in XML schema)
    - Using the built jar: 
      ```shell
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar validate --help
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar validate FightClub5eXML/Collections/CoreRulebooks.xml
      ```
    - Using the jbang installed alias: 
      ```shell
      fc5-convert validate --help
      fc5-convert validate FightClub5eXML/Collections/CoreRulebooks.xml
      ```    

4. Merge and transform the collected XML documents using XSLT 2.0 (a default xslt file is in src/main/resources):
    - Using the built jar: 
      ```shell
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar transform --help
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar transform -o target -x '-merged' FightClub5eXML/Collections/CoreRulebooks.xml
      ```
    - Using the jbang installed alias (notice `-o target` for the output directory): 
      ```shell
      fc5-convert transform --help
      fc5-convert transform -o target -x '-merged' FightClub5eXML/Collections/CoreRulebooks.xml
      ```
      
    This will create `target/CoreRulebooks-merged.xml`

5. Convert the merged XML document to Obsidian markdown (basically CommonMark with YAML front-matter). For testing/tooling around purposes, you can tool around with test files (notice `-o target/reference` for the output directory). 
    - Using the built jar: 
      ```shell
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar obsidian --help
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar obsidian \
        -o target/reference \
        src/test/resources/backgroundAcolyte.xml
      java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar obsidian \
        -o target/reference \
        target/CoreRulebooks-merged.xml
      ```
    - Using the jbang installed alias: 
      ```shell
      fc5-convert obsidian --help
      fc5-convert obsidian \
        -o target/reference \
        target/CoreRulebooks-merged.xml
      ```    

6. Export items to CSV (notice `-o target` for the output directory)
   - Using the built jar:
     ```shell
     java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar \
         transform -o target -x .csv \
         -t src/main/resources/itemExport.xslt \
         target/CoreRulebooks-merged.xml
     ```
   - Using the jbang installed alias :
     ```shell
     fc5-convert transform -o target -x .csv \
       -t src/main/resources/itemExport.xslt \
       target/CoreRulebooks-merged.xml
     ```    

## Qute Markdown templates

This applicaiton uses the [Qute Templating Engine](https://quarkus.io/guides/qute). Simple customizations to markdown output can be achieved by copying a template from src/main/resources/templates, making the desired modifications, and then specifying that template on the command line.
    
```shell
java -jar target/fc5-convert-cli-1.0.0-SNAPSHOT-runner.jar obsidian \
  --background src/main/resources/templates/background2md.txt \
  -o target/reference target/CoreRulebooks-merged.xml
```
OR
```shell
fc5-convert obsidian \
  --background src/main/resources/templates/background2md.txt \
  -o target/reference target/CoreRulebooks-merged.xml
```    
    
