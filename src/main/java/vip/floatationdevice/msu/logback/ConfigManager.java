package vip.floatationdevice.msu.logback;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager
{
    private static final int CONFIG_VERSION = 1;
    static YamlConfiguration cfg;

    static void initialize() throws Exception
    {
        LogBack.log.info("Loading configurations");
        File cfgFile = new File(LogBack.instance.getDataFolder(), "config.yml");
        if(!cfgFile.exists()) LogBack.instance.saveResource("config.yml", false);
        cfg = YamlConfiguration.loadConfiguration(cfgFile);
        if(getConfigVersion() != CONFIG_VERSION)
            throw new InvalidConfigurationException("Incorrect configuration version");
        LogBack.log.info("Configurations loaded");
    }

    static int getConfigVersion(){return cfg.getInt("version");}

    static String getLanguage(){return cfg.getString("language");}

    static boolean nofityEnabled(){return cfg.getBoolean("notify");}

    static boolean useMinecraftSpawnPoint(){return cfg.getBoolean("useMinecraftSpawnPoint");}

    static int getRecordExpirationSeconds(){return cfg.getInt("recordExpirationSeconds");}
}
