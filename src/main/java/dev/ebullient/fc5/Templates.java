package dev.ebullient.fc5;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import dev.ebullient.fc5.model.BackgroundType;
import dev.ebullient.fc5.model.ClassType;
import dev.ebullient.fc5.model.FeatType;
import dev.ebullient.fc5.model.ItemType;
import dev.ebullient.fc5.model.MarkdownWriter.FileMap;
import dev.ebullient.fc5.model.MonsterType;
import dev.ebullient.fc5.model.RaceType;
import dev.ebullient.fc5.model.SpellType;
import io.quarkus.qute.Template;

@ApplicationScoped
public class Templates {

    @Inject
    public Template index;

    public String renderIndex(String name, List<FileMap> resources) {
        return index
                .data("name", name)
                .data("resources", resources)
                .render();
    }

    @Inject
    public Template background2md;

    public String renderBackground(BackgroundType resource) {
        return background2md
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template class2md;

    public String renderClass(ClassType resource) {
        return class2md
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template feat2md;

    public String renderFeat(FeatType resource) {
        return feat2md
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template item2md;

    public String renderItem(ItemType resource) {
        return item2md
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template monster2md;

    public String renderMonster(MonsterType resource) {
        return monster2md
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template race2md;

    public String renderRace(RaceType resource) {
        return race2md
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template spell2md;

    public String renderSpell(SpellType resource) {
        return spell2md
                .data("resource", resource)
                .render().trim();
    }

    @Override
    public String toString() {
        return "Templates [background2md=" + background2md + ", class2md=" + class2md + ", feat2md=" + feat2md
                + ", index=" + index + ", item2md=" + item2md + ", monster2md=" + monster2md + ", race2md=" + race2md
                + ", spell2md=" + spell2md + "]";
    }
}
