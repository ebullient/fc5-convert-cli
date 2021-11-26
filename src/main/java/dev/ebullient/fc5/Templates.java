package dev.ebullient.fc5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import dev.ebullient.fc5.data.BackgroundType;
import dev.ebullient.fc5.data.ClassType;
import dev.ebullient.fc5.data.FeatType;
import dev.ebullient.fc5.data.ItemType;
import dev.ebullient.fc5.data.MarkdownWriter.FileMap;
import dev.ebullient.fc5.data.MonsterType;
import dev.ebullient.fc5.data.RaceType;
import dev.ebullient.fc5.data.SpellType;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

@ApplicationScoped
public class Templates {
    Map<String, Template> templates = new HashMap<>();
    TemplatePaths templatePaths = null;

    @Inject
    Engine engine;

    public void setCustomTemplates(TemplatePaths templatePaths) {
        this.templatePaths = templatePaths;
    }

    private Template customTemplateOrDefault(String id, Template defaultTemplate) {
        Path customPath = templatePaths == null ? null : templatePaths.get(id);
        if (customPath != null) {
            try {
                return engine.parse(Files.readString(customPath));
            } catch (IOException e) {
                Log.errorf(e, "Failed reading template for %s from %s", id, customPath);
            }
        }
        return defaultTemplate;
    }

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
        Template tpl = templates.computeIfAbsent("background2md.txt", k -> customTemplateOrDefault(k, background2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template class2md;

    public String renderClass(ClassType resource) {
        Template tpl = templates.computeIfAbsent("class2md.txt", k -> customTemplateOrDefault(k, class2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template feat2md;

    public String renderFeat(FeatType resource) {
        Template tpl = templates.computeIfAbsent("feat2md.txt", k -> customTemplateOrDefault(k, feat2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template item2md;

    public String renderItem(ItemType resource) {
        Template tpl = templates.computeIfAbsent("item2md.txt", k -> customTemplateOrDefault(k, item2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template monster2md;

    public String renderMonster(MonsterType resource) {
        Template tpl = templates.computeIfAbsent("monster2md.txt", k -> customTemplateOrDefault(k, monster2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template race2md;

    public String renderRace(RaceType resource) {
        Template tpl = templates.computeIfAbsent("race2md.txt", k -> customTemplateOrDefault(k, race2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Inject
    public Template spell2md;

    public String renderSpell(SpellType resource) {
        Template tpl = templates.computeIfAbsent("spell2md.txt", k -> customTemplateOrDefault(k, spell2md));
        return tpl
                .data("resource", resource)
                .render().trim();
    }

    @Override
    public String toString() {
        return "Templates [background2md=" + background2md + ", class2md=" + class2md + ", feat2md=" + feat2md
                + ", index=" + index + ", item2md=" + item2md + ", monster2md=" + monster2md + ", race2md=" + race2md
                + ", spell2md=" + spell2md + ", templates=" + templates + "]";
    }
}
