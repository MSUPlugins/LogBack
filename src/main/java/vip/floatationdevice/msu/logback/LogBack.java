package vip.floatationdevice.msu.logback;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import vip.floatationdevice.msu.ConfigManager;
import vip.floatationdevice.msu.I18nManager;

import java.util.Vector;
import java.util.logging.Logger;

public final class LogBack extends JavaPlugin implements Listener
{
    static LogBack instance;
    static Logger log;
    static ConfigManager cm;
    static I18nManager i18n;
    static Vector<RecordExpirationTimer> expirationTimers = new Vector<>();

    @Override
    public void onEnable()
    {
        instance = this;
        log = getLogger();
        log.info("Initializing");
        cm = new ConfigManager(this, 1).initialize();
        i18n = new I18nManager(this).setLanguage(cm.get(String.class, "language"));

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("logback").setExecutor(new LBCommandExecutor());

        if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !DataManager.isSpawnSet())
            log.warning(i18n.translate("warn-spawn-not-set"));

        log.info("Initialization complete");
    }

    @Override
    public void onDisable()
    {
        // cancel all expiration timers
        for(RecordExpirationTimer t : expirationTimers) t.interrupt();
        // save all players' location
        log.info("Saving all players' location");
        int count = 0;
        for(Player p : getServer().getOnlinePlayers())
        {
            Location loc = p.getLocation();
            try
            {
                DataManager.writeLocation(p, loc, false);
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e)
    {
        if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !DataManager.isSpawnSet())
        {
            log.warning(i18n.translate("warn-spawn-not-set"));
            return;
        }
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        Location spawn;
        try
        {
            spawn = DataManager.readSpawnLocation();
        }
        catch(Exception ex)
        {
            spawn = getServer().getWorlds().get(0).getSpawnLocation();
            log.severe(i18n.translate("err-read-spawn-fail").replace("{0}", ex.toString()));
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    DataManager.writeLocation(p, loc, false);
                }
                catch(Exception ex)
                {
                    log.severe(i18n.translate("err-write-location-fail")
                            .replace("{0}", p.getName())
                            .replace("{1}", ex.toString()));
                }
            }
        });
        p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !DataManager.isSpawnSet())
        {
            log.warning(i18n.translate("warn-spawn-not-set"));
            if(p.hasPermission("logback.setspawn"))
                p.sendMessage(i18n.translate("warn-spawn-not-set"));
            return;
        }
        Location spawn;
        try
        {
            spawn = DataManager.readSpawnLocation();
        }
        catch(Exception ex)
        {
            spawn = getServer().getWorlds().get(0).getSpawnLocation();
            log.severe(i18n.translate("err-read-spawn-fail").replace("{0}", e.toString()));
        }
        //if(!DataManager.isRecorded(p.getUniqueId())) log.info(p.getName() + " doesn't have a logout location");
        p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
        if(cm.get(Boolean.class, "notify"))
            p.sendMessage(i18n.translate("notify"));
        if(cm.get(Integer.class, "recordExpirationSeconds") > 0 && DataManager.isRecorded(p.getUniqueId()))
        {
            RecordExpirationTimer t = new RecordExpirationTimer(p.getUniqueId());
            expirationTimers.add(t);
            t.start();
        }
    }
}
