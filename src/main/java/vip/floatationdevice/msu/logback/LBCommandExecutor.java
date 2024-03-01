package vip.floatationdevice.msu.logback;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import static vip.floatationdevice.msu.logback.LogBack.*;

public class LBCommandExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // skip console
        if(!(sender instanceof Player))
        {
            sender.sendMessage(i18n.translate("err-player-only"));
            return false;
        }

        Player p = (Player) sender;

        // /logback
        if(args.length == 0)
        {
            // check permission
            if(!p.hasPermission("logback.logback"))
            {
                p.sendMessage(i18n.translate("err-permission-denied"));
                return true;
            }

            try
            {
                if(dm.isRecorded(p.getUniqueId()))
                {
                    p.teleport(dm.readLocation(p.getUniqueId()), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    p.sendMessage(i18n.translate("logback-success"));
                    dm.removeLocation(p.getUniqueId());
                    for(RecordExpirationTimer t : expirationTimers)
                        if(t.u == p.getUniqueId())
                        {
                            t.interrupt();
                            break;
                        }
                }
                else
                {
                    p.sendMessage(i18n.translate("err-no-record"));
                }
            }
            catch(Exception e)
            {
                p.sendMessage(i18n.translate("err-logback-fail"));
                log.severe(i18n.translate("err-logback-fail-console")
                        .replace("{0}", p.getName())
                        .replace("{1}", e.toString()));
                e.printStackTrace();
            }

            return true;
        }
        // /logback setspawn
        else if(args.length == 1 && args[0].equalsIgnoreCase("setspawn"))
        {
            if(!p.hasPermission("logback.setspawn"))
            {
                p.sendMessage(i18n.translate("err-permission-denied"));
                return true;
            }

            try
            {
                dm.writeLocation(p, p.getLocation(), true);
                p.sendMessage(i18n.translate("setspawn-success"));
            }
            catch(Exception e)
            {
                p.sendMessage(i18n.translate("err-setspawn-fail"));
                e.printStackTrace();
            }

            return true;
        }
        // wrong usage
        else
        {
            p.sendMessage(i18n.translate("usage"));
            return false;
        }
    }
}
