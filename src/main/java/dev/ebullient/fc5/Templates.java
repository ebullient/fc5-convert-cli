package dev.ebullient.fc5;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@CheckedTemplate
public class Templates {
    public static native TemplateInstance background2md(); 
    public static native TemplateInstance class2md(); 
    public static native TemplateInstance feat2md(); 
    public static native TemplateInstance item2md(); 
    public static native TemplateInstance monster2md(); 
    public static native TemplateInstance race2md(); 
    public static native TemplateInstance spell2md(); 
}