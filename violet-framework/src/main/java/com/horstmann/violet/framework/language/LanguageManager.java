package com.horstmann.violet.framework.language;

import com.horstmann.violet.framework.injection.bean.ManiocFramework;
import com.horstmann.violet.framework.userpreferences.UserPreferencesService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.horstmann.violet.framework.injection.bean.ManiocFramework.InjectedBean;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;

/**
 * Created by Marcin on 10.01.2017.
 */
public class LanguageManager {

    private List<Language> languages = new ArrayList<Language>();
    private static final String resourceDir = "com/horstmann/violet/framework/language/";
    /**
     * Default constructor
     */
    public LanguageManager() {

        BeanInjector.getInjector().inject(this);
        loadAvailableLanguage();
    }

    /**
     * Get languages List
     *
     * @return languages
     */
    public List<Language> getLanguages() {
        return languages;
    }

    /**
     * Set PreferedLanguage
     */
    public void setPreferedLanguage(String language) {
        this.userPreferencesServices.setPreferedLanguage(language);
    }

    /**
     * Get PreferedLanguage
     *
     * @return PreferedLanguage
     */
    public String getPreferedLanguage() {
        return this.userPreferencesServices.getPreferedLanguage();
    }

    /**
     * Load languages and add to list
     */
    public void loadAvailableLanguage() {


        String[] languages = Locale.getISOLanguages();
        for (String countryCode : languages) {

            String path = "Language_" + countryCode + ".properties";
            URL file = ClassLoader.getSystemResource(resourceDir + path);

            if (file != null) {

                Locale locale = new Locale(countryCode);
                String languageName = locale.getDisplayLanguage(locale);
                this.languages.add(new Language(countryCode, languageName));

            }

        }


    }

    /**
     * Apply Prefered Language
     */
    public void applyPreferedLanguage() {

        Locale locale = new Locale(getPreferedLanguage());
        Locale.setDefault(locale);

    }


    @InjectedBean
    private UserPreferencesService userPreferencesServices;
}