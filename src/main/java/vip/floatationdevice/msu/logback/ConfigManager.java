package vip.floatationdevice.msu.logback;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager
{
    private static final int CONFIG_VERSION=1;
    protected static YamlConfiguration cfg;
    protected static void initialize() throws Exception
    {
        LogBack.log.info("Loading configurations");
        File cfgFile = new File(LogBack.instance.getDataFolder(), "config.yml");
        if (!cfgFile.exists()) LogBack.instance.saveResource("config.yml", false);
        cfg=YamlConfiguration.loadConfiguration(cfgFile);
        if(getConfigVersion()!=CONFIG_VERSION) throw new InvalidConfigurationException("Incorrect configuration version");
        LogBack.log.info("Configurations loaded");
    }
    protected static int getConfigVersion(){return cfg.getInt("version");}
    protected static String getLanguage(){return cfg.getString("language");}
    protected static boolean nofityEnabled(){return cfg.getBoolean("notify");}
    protected static boolean useMinecraftSpawnPoint(){return cfg.getBoolean("useMinecraftSpawnPoint");}
}
