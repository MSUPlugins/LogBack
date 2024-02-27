package vip.floatationdevice.msu.logback;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static vip.floatationdevice.msu.logback.LogBack.cm;

public class RecordExpirationTimer extends Thread implements Listener
{
    private final UUID u;

    public RecordExpirationTimer(UUID u)
    {
        setName("Logout location expiration timer " + u);
        this.u = u;
    }

    @Override
    public void run()
    {
        //LogBack.log.info("Started expiration timer for " + u + ".txt");
        Bukkit.getServer().getPluginManager().registerEvents(this, LogBack.instance);
        try
        {
            sleep(cm.get(Integer.class, "recordExpirationSeconds") * 1000L);
            DataManager.removeLocation(u);
            //LogBack.log.info("Logout location " + u + ".txt expired");
        }
        catch(InterruptedException e)
        {
            //LogBack.log.info("Cancelled expiration for " + u + ".txt");
        }
        PlayerQuitEvent.getHandlerList().unregister(this);
        LogBack.expirationTimers.remove(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if(event.getPlayer().getUniqueId().equals(u))
        {
            //LogBack.log.info("Player quit, cancel expiration");
            interrupt();
        }
    }
}
