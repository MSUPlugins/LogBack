package vip.floatationdevice.msu.logback;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class I18nUtil
{
    private static FileConfiguration l;
    private static String lang = "en_US";

    public static void setLanguage(String language)
    {
        LogBack.log.info("Loading translations");
        if (language == null || language.equals("")) return;
        lang = language;
        File langFile = new File(LogBack.instance.getDataFolder(), "lang_" + lang + ".yml");
        if (!langFile.exists()) LogBack.instance.saveResource("lang_" + lang + ".yml", false);
        l = YamlConfiguration.loadConfiguration(langFile);
        LogBack.log.info("Language: " + translate("language") + " by " + translate("language-file-contributor"));
    }

    public static String translate(String key)
    {
        String msg = l.getString(key);
        return msg == null ? "[NO TRANSLATION: " + key + "]" : msg;
    }

    public String getLanguage()
    {
        return lang;
    }
}