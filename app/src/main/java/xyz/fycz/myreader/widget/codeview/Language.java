package xyz.fycz.myreader.widget.codeview;


import java.util.HashMap;
import java.util.Map;

public enum Language {

    AUTO(""),
    HTML("html"),
    JAVA("java"),
    JAVASCRIPT("javascript"),
    JSON("json");

    private static final Map<String, Language> LANGUAGES = new HashMap<>();
    private final String name;

    static {
        for (Language language : values()) {
            if (language != AUTO) {
                LANGUAGES.put(language.name, language);
            }
        }
    }

    Language(String name) {
        this.name = name;
    }

    public static Language getLanguageByName(String name) {
        return LANGUAGES.get(name);
    }

    public String getLanguageName() {
        return name;
    }
}

