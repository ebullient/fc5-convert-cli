package dev.ebullient.fc5;

import java.util.List;

import dev.ebullient.fc5.Convert.FileMap;
import dev.ebullient.fc5.model.BackgroundType;
import dev.ebullient.fc5.model.ClassType;
import dev.ebullient.fc5.model.FeatType;
import dev.ebullient.fc5.model.ItemType;
import dev.ebullient.fc5.model.MonsterType;
import dev.ebullient.fc5.model.RaceType;
import dev.ebullient.fc5.model.SpellType;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@CheckedTemplate
public class Templates {
    public static native TemplateInstance index(String heading, List<FileMap> fileMappings);

    public static native TemplateInstance background2md(BackgroundType background);

    public static native TemplateInstance class2md(ClassType pcClass);

    public static native TemplateInstance feat2md(FeatType feat);

    public static native TemplateInstance item2md(ItemType item);

    public static native TemplateInstance monster2md(MonsterType monsterType);

    public static native TemplateInstance race2md(RaceType pcRace);

    public static native TemplateInstance spell2md(SpellType spell);
}
