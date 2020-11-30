package xyz.fycz.myreader.enums;

public enum Language {

    normal,
    simplified,//繁转简
    traditional;//简转繁


    Language() {

    }

    public static Language get(int var0) {
        return values()[var0];
    }

    public static Language fromString(String string) {
        return Language.valueOf(string);
    }
}
