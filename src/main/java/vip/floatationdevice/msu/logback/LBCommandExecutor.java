package vip.floatationdevice.msu.logback;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static vip.floatationdevice.msu.logback.LogBack.i18n;
import static vip.floatationdevice.msu.logback.LogBack.log;

public class LBCommandExecutor implements Listener, CommandExecutor
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
                if(DataManager.isRecorded(p.getUniqueId()))
                {
                    p.teleport(DataManager.readLocation(p.getUniqueId()), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    p.sendMessage(i18n.translate("logback-success"));
                    DataManager.removeLocation(p.getUniqueId());
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
                DataManager.writeLocation(p, p.getLocation(), true);
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
