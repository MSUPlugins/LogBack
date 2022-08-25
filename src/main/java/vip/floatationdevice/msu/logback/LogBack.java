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

import java.util.logging.Logger;

import static vip.floatationdevice.msu.logback.I18nUtil.*;

public final class LogBack extends JavaPlugin implements Listener
{
    public static LogBack instance;
    public static Logger log;

    @Override
    public void onEnable()
    {
        log=getLogger();
        log.info("Initializing");
        instance=this;
        getServer().getPluginManager().registerEvents(this,this);
        try
        {
            ConfigManager.initialize();
            setLanguage(ConfigManager.getLanguage());
            this.setEnabled(true);
            getCommand("logback").setExecutor(new LBCommandExecutor());
            log.info("Initialization complete");
        }
        catch (Exception e)
        {
            log.severe("Initialization failed");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        if(!ConfigManager.useMinecraftSpawnPoint() && !DataManager.isSpawnSet())
            log.warning(translate("warn-spawn-not-set"));
    }

    @Override
    public void onDisable()
    {
        // save all players' location
        log.info("Saving all players' location");
        int count=0;
        for(Player p:getServer().getOnlinePlayers())
        {
            Location loc=p.getLocation();
            try
            {
                DataManager.writeLocation(p,loc,false);
                count++;
            }
            catch(Exception e)
            {
                log.severe(translate("err-write-location-fail")
                        .replace("{0}",p.getName())
                        .replace("{1}",e.toString()));
            }
        }
        log.info("Saving complete, "+count+" players' location saved");
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e)
    {
        if(!ConfigManager.useMinecraftSpawnPoint() && !DataManager.isSpawnSet())
        {
            log.warning(translate("warn-spawn-not-set"));
            return;
        }
        Player p=e.getPlayer();
        Location loc=p.getLocation();
        Location spawn;
        try
        {
            spawn=DataManager.readSpawnLocation();
        }
        catch (Exception ex)
        {
            spawn=getServer().getWorlds().get(0).getSpawnLocation();
            log.severe(translate("err-read-spawn-fail").replace("{0}",ex.toString()));
        }
        Bukkit.getScheduler().runTaskAsynchronously(this,new Runnable(){
            @Override public void run()
            {
                try
                {
                    DataManager.writeLocation(p,loc,false);
                }
                catch(Exception ex)
                {
                    log.severe(translate("err-write-location-fail")
                            .replace("{0}",p.getName())
                            .replace("{1}",ex.toString()));
                }
            }
        });
        p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if(!ConfigManager.useMinecraftSpawnPoint() && !DataManager.isSpawnSet())
        {
            log.warning(translate("warn-spawn-not-set"));
            return;
        }
        Player p=e.getPlayer();
        Location spawn;
        try
        {
            spawn=DataManager.readSpawnLocation();
        }
        catch (Exception ex)
        {
            spawn=getServer().getWorlds().get(0).getSpawnLocation();
            log.severe(translate("err-read-spawn-fail"));
        }
        if(!DataManager.isRecorded(p.getUniqueId())) log.info(p.getName()+" doesn't have a logout location");
        p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
        if(ConfigManager.nofityEnabled()) p.sendMessage(translate("notify"));
    }
}
