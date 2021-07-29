# convert-cli Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .



## Crib sheet

Regenerate POJO based on compendium.xsd: 

```shell
xjc -d src/main/java -p dev.ebullient.fc5.model ../FightClub5eXML/Utilities/compendium.xsd
```

some methods have been added..