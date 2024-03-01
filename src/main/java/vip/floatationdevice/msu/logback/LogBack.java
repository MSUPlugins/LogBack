package vip.floatationdevice.msu.logback;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vip.floatationdevice.msu.ConfigManager;
import vip.floatationdevice.msu.I18nManager;

import java.util.Vector;
import java.util.logging.Logger;

public final class LogBack extends JavaPlugin
{
    static LogBack instance;
    static Logger log;
    static ConfigManager cm;
    static I18nManager i18n;
    static DataManager dm;
    static Vector<RecordExpirationTimer> expirationTimers;

    @Override
    public void onEnable()
    {
        instance = this;
        log = getLogger();
        cm = new ConfigManager(this, 1).initialize();
        i18n = new I18nManager(this).setLanguage(cm.get(String.class, "language"));
        dm = new DataManager(this);
        expirationTimers = new Vector<>();

        getServer().getPluginManager().registerEvents(new LBEventListener(), this);
        getCommand("logback").setExecutor(new LBCommandExecutor());

        if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !dm.isSpawnSet())
            log.warning(i18n.translate("warn-spawn-not-set"));

        log.info("LogBack loaded");
    }

    @Override
    public void onDisable()
    {
        log.info("LogBack is being disabled");

        // cancel all expiration timers
        for(RecordExpirationTimer t : expirationTimers)
            t.interrupt();

        /*
         * FIXME: saving all players' location on server shutdown doesn't work as expected
         *  Excerpted from: https://www.spigotmc.org/threads/is-playerquitevent-called-when-stop-is-used.269901/
         *   "Server disables plugins before disconnecting the players, and so, the event listener does not get the
         *   event, even if it would be fired."
         *  The facts are like: the server first unregisters the plugin's event listener, then kicks everyone, and
         *   finally calls onDisable(). This happens to cause the result of getOnlinePlayers() to be always empty when
         *   the server is being shut down.
         */
        log.info("Saving all players' location");
        int count = 0;
        for(Player p : getServer().getOnlinePlayers())
        {
            Location loc = p.getLocation();
            try
            {
                dm.writeLocation(p, loc, false);
                count++;
            }
            catch(Exception e)
            {
                log.severe(i18n.translate("err-write-location-fail")
                        .replace("{0}", p.getName())
                        .replace("{1}", e.toString()));
            }
        }
        log.info("Saving complete, " + count + " players' location saved");
    }
}
