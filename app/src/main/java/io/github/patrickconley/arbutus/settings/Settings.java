package io.github.patrickconley.arbutus.settings;

// Keys must match those in preferences.xml
public enum Settings {
    LIBRARY_PATH("library path"), SCAN_NOW("scan library"),;

    private String key;

    Settings(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
