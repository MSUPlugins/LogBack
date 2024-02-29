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

import static vip.floatationdevice.msu.logback.LogBack.*;

public class LBEventListener implements Listener
{
    /**
     * When a player leaves the server, teleport him to spawn point.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent e)
    {
        // spawn point hasn't been set, LogBack will not work
        if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !DataManager.isSpawnSet())
        {
            log.warning(i18n.translate("warn-spawn-not-set"));
            return;
        }

        // get spawn location
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        Location spawn;
        try
        {
            spawn = DataManager.readSpawnLocation();
        }
        catch(Exception ex)
        {
            // failed to read spawn point, use MC spawn instead
            spawn = instance.getServer().getWorlds().get(0).getSpawnLocation();
            log.severe(i18n.translate("err-read-spawn-fail").replace("{0}", ex.toString()));
        }

        // save player's logout location
        Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable()
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

        // teleport the player to spawn point. next time player logs in, he will be there
        p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * Teleport new players to spawn point, and notify players about using "/logback".
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        // spawn point hasn't been set, LogBack will not work
        if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !DataManager.isSpawnSet())
        {
            log.warning(i18n.translate("warn-spawn-not-set"));
            if(p.hasPermission("logback.setspawn"))
                p.sendMessage(i18n.translate("warn-spawn-not-set"));
            return;
        }

        // check if the player has logout location recorded
        if(DataManager.isRecorded(p.getUniqueId()))
        {
            // should notify the player about /logback or not
            if(cm.get(Boolean.class, "notify"))
                p.sendMessage(i18n.translate("notify"));

            // if config.yml has recordExpirationSeconds > 0, start expiration timer for the player
            if(cm.get(Integer.class, "recordExpirationSeconds") > 0)
            {
                RecordExpirationTimer t = new RecordExpirationTimer(p.getUniqueId());
                expirationTimers.add(t);
                t.start();
            }
        }
        else
        {
            // doesn't have logout location. maybe new player or plugin's bug
            log.info(p.getName() + " doesn't have a logout location");
            // teleport to spawn
            Location spawn;
            try
            {
                spawn = DataManager.readSpawnLocation();
            }
            catch(Exception ex)
            {
                spawn = instance.getServer().getWorlds().get(0).getSpawnLocation();
                log.severe(i18n.translate("err-read-spawn-fail").replace("{0}", e.toString()));
            }
            p.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }
}
