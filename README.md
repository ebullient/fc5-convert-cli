# fc5-convert-cli

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

I like using both Fight Club 5 and Game Master 5 (more the former than the latter these days, because Roll20..), and for the game I run, I like being able to control/rebuild what is visible and in the compendium for my players (e.g. to constrain sources).

I also use [Obsidian](https://obsidian.md) to keep track of my notes. The goal is to use the same filtered resources as a reference both in my local notes (Obsidian) and in Fight Club 5 (Players).

## To run without building yourself

1. Install JBang: https://www.jbang.dev/documentation/guide/latest/installation.html
2. Install the snapshot jar: 
  ```
  jbang app install --name fc5-convert --force --fresh https://jitpack.io/dev/ebullient/fc5-convert-cli/1.1.0-SNAPSHOT/fc5-convert-cli-1.1.0-SNAPSHOT-runner.jar
  ```
3. Run the command: 
  ```
  fc5-convert --help
  ```

## To build (optional)

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. `java -jar target/fc5-convert-cli-1.1.0-SNAPSHOT-runner.jar --help`


To run commands listed below, either: 

- Replace `fc5-convert` with `java -jar target/fc5-convert-cli-1.1.0-SNAPSHOT-runner.jar`
- Use JBang to create an alias that points to the built jar: 

    ```shell
  jbang app install --name fc5-convert --force --fresh ~/.m2/repository/dev/ebullient/fc5-convert-cli/1.1.0-SNAPSHOT/fc5-convert-cli-1.1.0-SNAPSHOT-runner.jar
    ```

    > Feel free to use an alternate alias by replacing the value specified as the name: `--name fc5-convert`, and adjust the commands shown below accordingly.

## Starting with 5eTools JSON data

1. Download a release of the 5e tools mirror, or create a shallow clone of the repo (which can/should be deleted afterwards):

    ```shell
    git clone --depth 1 https://github.com/5etools-mirror-1/5etools-mirror-1.github.io.git
    ```

2. Invoke the CLI. In this first example, let's generate indexes and use only SRD content:

    ```
    fc5-convert 5etools \
    --xml \
    --md \
    --index \
    -o dm \
    ~/git/dnd/5etools-mirror-1.github.io/data
    ```

    - `--xml` Create FightClub 5 Compendium XML files (compendium.xml and files per type)
    - `--md` Create Obsidian Markdown from [Templates](#templates)
    - `--index` Create `all-index.json` containing all of the touched artifact ids, and `src-index.json` that shows the filtered/allowed artifact ids. These files are useful when tweaking exclude rules (as shown below).
    - `-o dm` The target output directory. Files will be created in this directory.

    The rest of the command-line specifies input files: 

    - `~/git/dnd/5etools-mirror-1.github.io/data` Path to the data directory containing 5etools files (a clone or release of the mirror repo)

3. Invoke the command again, this time including sources and custom items:

    ```shell
    fc5-convert 5etools \
    --xml \
    --md \
    --index \
    -o dm \
    -s PHB,DMG,SCAG \
    5etools-mirror-1.github.io/data \
    my-items.json dm-sources.json
    ```
    
    - `-s PHB,DMG,SCAG` Will include content from the Player's Handbook, the Dungeon Master's Guide, and the Sword Coast Adventurer's Guide, all of which I own. Source abbreviations are found in the [source code](https://github.com/ebullient/fc5-convert-cli/blob/74e5a8e4f1d1ec5f3ba996d8ac9476a3a1dbe892/src/main/java/dev/ebullient/fc5/json5e/CompendiumSources.java#L113).
    - `my-items.json` Custom items that I've created for my campaign that follow 5etools JSON format.
    - `dm-sources.json` Additional parameters (shown in detail below)

### Additional parameters

I use a json file to provide detailed configuration for sources, as doing so with command line arguments becomes tedious and error-prone. I use something like this:

```json
{
  "from": [
    "AI",
    "PHB",
    "DMG",
    "TCE",
    "LMoP",
    "ESK",
    "DIP",
    "FTD",
    "MM",
    "MTF",
    "VGM"
  ],
  "paths": {
    "rules": "/compendium/rules/"
  },
  "excludePattern": [
    "race|.*|dmg"
  ],
  "exclude": [
    "monster|expert|dc",
    "monster|expert|sdw",
    "monster|expert|slw"
  ]
}
```

- `from` defines the array of sources that should be included. Only include content from sources you own. If you omit this parameter (and don't specify any other sources on the command line), this tool will only include content from the SRD.

    - **Source abbreviations** are found in the [source code](https://github.com/ebullient/fc5-convert-cli/blob/74e5a8e4f1d1ec5f3ba996d8ac9476a3a1dbe892/src/main/java/dev/ebullient/fc5/json5e/CompendiumSources.java#L113)

- `paths` allows you to redefine vault paths for cross-document links, and to link to documents defining conditions, and weapon/item properties. By default, items, spells, monsters, backgrounds, races, and classes are in `/compendium/`, while files defining conditions and weapon properties are in `/rules/`. You can reconfigure either of these path roots in this block: 

    ```json
    "paths": {
      "compendium": "/compendium/",
      "rules": "/rules/"
    },
    ```
    > Note: the leading slash indicates the path starting at the root of your vault.

- `exclude`, and `excludePattern` work against the identifiers (listed in the generated index files). They allow you to further tweak/constrain what is emitted as formatted markdown. In the above example, I'm excluding all of the race variants from the DMG, and the monster-form of the expert sidekick from the Essentials Kit. I own both of these books, but I don't want those creatures in the formatted bestiary.

For example, to generate player-focused reference content for a Wild Beyond the Witchlight campaign, I've constrained things further: I am pulling from a much smaller set of sources. I included Elemental Evil Player's Companion (Genasi) and Volo's Guide to Monsters (Tabaxi), but then added exclude patterns to remove elements from these sourcebooks that I don't want my players to use in this campaign (some simplification for beginners). My JSON looks like this:

```json
{
  "from": [
    "PHB",
    "DMG",
    "XGE",
    "TCE",
    "EEPC",
    "WBtW",
    "VGM"
  ],
  "includeGroups": [
    "familiars"
  ],
  "excludePattern": [
    ".*sidekick.*",
    "race|.*|dmg",
    "race|(?!tabaxi).*|vgm",
    "subrace|.*|aasimar|vgm",
    "item|.*|vgm",
    "monster|.*|tce",
    "monster|.*|dmg",
    "monster|.*|vgm",
    "monster|.*|wbtw",
    "monster|animated object.*|phb"
  ],
  "exclude": [
    "race|aarakocra|eepc",
    "feat|actor|phb",
    "feat|artificer initiate|tce",
    "feat|athlete|phb",
    "feat|bountiful luck|xge",
    "feat|chef|tce",
    "feat|dragon fear|xge",
    "feat|dragon hide|xge",
    "feat|drow high magic|xge",
    "feat|durable|phb",
    "feat|dwarven fortitude|xge",
    "feat|elven accuracy|xge",
    "feat|fade away|xge",
    "feat|fey teleportation|xge",
    "feat|fey touched|tce",
    "feat|flames of phlegethos|xge",
    "feat|gunner|tce",
    "feat|heavily armored|phb",
    "feat|heavy armor master|phb",
    "feat|infernal constitution|xge",
    "feat|keen mind|phb",
    "feat|lightly armored|phb",
    "feat|linguist|phb",
    "feat|lucky|phb",
    "feat|medium armor master|phb",
    "feat|moderately armored|phb",
    "feat|mounted combatant|phb",
    "feat|observant|phb",
    "feat|orcish fury|xge",
    "feat|piercer|tce",
    "feat|poisoner|tce",
    "feat|polearm master|phb",
    "feat|prodigy|xge",
    "feat|resilient|phb",
    "feat|second chance|xge",
    "feat|shadow touched|tce",
    "feat|skill expert|tce",
    "feat|slasher|tce",
    "feat|squat nimbleness|xge",
    "feat|tavern brawler|phb",
    "feat|telekinetic|tce",
    "feat|telepathic|tce",
    "feat|weapon master|phb",
    "feat|wood elf magic|xge",
    "item|iggwilv's cauldron|wbtw"
  ]
}
```

## Starting with FightClub 5 XML data

1. Grab a copy of the FC5 XML repo: Clone https://github.com/kinkofer/FightClub5eXML

2. Look in the Collections directory of the Fight Club repository to see how collections work. Choose one of those or make your own.

3. Validate the Collection (against a built-in XML schema)
    ```shell
    fc5-convert validate --help
    fc5-convert validate FightClub5eXML/Collections/CoreRulebooks.xml
    ```    

4. Merge and transform the collected XML documents using XSLT 2.0 (a default xslt file is in src/main/resources):
    ```shell
    fc5-convert transform --help
    fc5-convert transform -o target -x '-merged' FightClub5eXML/Collections/CoreRulebooks.xml
    ```
      
    This will create `target/CoreRulebooks-merged.xml` (notice `-o target` for the output directory)

5. Convert the merged XML document to Obsidian markdown (basically CommonMark with YAML front-matter). For testing/tooling around purposes, you can tool around with test files (notice `-o target/reference` for the output directory). 
    ```shell
    fc5-convert obsidian --help
    fc5-convert obsidian \
      -o target/reference \
      target/CoreRulebooks-merged.xml
    ```    

6. Export items to CSV (notice `-o target` for the output directory)
   ```shell
   fc5-convert transform -o target -x .csv \
     -t src/main/resources/itemExport.xslt \
     target/CoreRulebooks-merged.xml
   ```    

## Templates

This applicaiton uses the [Qute Templating Engine](https://quarkus.io/guides/qute). Simple customizations to markdown output can be achieved by copying a template from src/main/resources/templates, making the desired modifications, and then specifying that template on the command line.

### When starting with 5etools JSON data

```
fc5-convert 5etools --xml --md --index  -o dm \
  --background src/main/resources/templates/background2md.txt \
  dm-sources.json ~/git/dnd/5etools-mirror-1.github.io/data my-items.json
```

### When starting from FC5 XML data

```shell
fc5-convert obsidian \
  --background src/main/resources/templates/background2md.txt \
  -o target/reference target/CoreRulebooks-merged.xml
```    

### Template examples

- [Default templates](https://github.com/ebullient/fc5-convert-cli/tree/main/src/main/resources/templates)
- [Alternative templates](https://github.com/ebullient/fc5-convert-cli/tree/main/src/test/resources/customTemplates)

