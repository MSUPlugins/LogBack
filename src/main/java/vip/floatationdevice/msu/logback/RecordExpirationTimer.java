package vip.floatationdevice.msu.logback;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static vip.floatationdevice.msu.logback.LogBack.*;

public class RecordExpirationTimer extends Thread implements Listener
{
    final UUID u;

    public RecordExpirationTimer(UUID u)
    {
        setName("Logout location expiration timer " + u);
        this.u = u;
    }

    @Override
    public void run()
    {
//        log.info("Started expiration timer for " + u + ".txt");
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
        try
        {
            sleep(cm.get(Integer.class, "recordExpirationSeconds") * 1000L);
            dm.removeLocation(u);
//            log.info("Logout location " + u + ".txt expired");
        }
        catch(InterruptedException e)
        {
//            log.info("Cancelled expiration for " + u + ".txt");
        }
        PlayerQuitEvent.getHandlerList().unregister(this);
        expirationTimers.remove(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if(event.getPlayer().getUniqueId().equals(u))
        {
//            log.info("Player quit, cancel expiration");
            interrupt();
        }
    }
}
