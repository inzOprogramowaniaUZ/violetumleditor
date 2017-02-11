package com.horstmann.violet.framework.language;

/**
 * Created by Marcin on 10.01.2017.
 */
public class Language {

    private String shortcut;
    private String name;

    /**
     * Constructor Language
     *
     * @param shortcut, name
     */
    public Language(String shortcut, String name) {
        this.shortcut = shortcut;
        this.name = name;
    }

    /**
     * Get language name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set language name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get language shortcut
     *
     * @return shortcut
     */
    public String getShortcut() {
        return shortcut;
    }

    /**
     * Set language shortcut
     *
     * @param shortcut
     */
    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }
}
