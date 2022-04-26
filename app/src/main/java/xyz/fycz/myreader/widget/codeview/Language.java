/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

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

