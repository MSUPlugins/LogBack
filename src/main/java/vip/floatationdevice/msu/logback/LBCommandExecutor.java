package vip.floatationdevice.msu.logback;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static vip.floatationdevice.msu.logback.I18nUtil.*;

public class LBCommandExecutor implements Listener, CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(translate("err-player-only"));
            return false;
        }
        switch(args.length)
        {
            case 0:
            {
                if(sender.hasPermission("logback.logback"))
                {
                    try
                    {
                        if(DataManager.isRecorded(((Player)sender).getUniqueId()))
                        {
                            ((Player)sender).teleport(DataManager.readLocation(((Player)sender).getUniqueId()), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            sender.sendMessage(translate("logback-success"));
                            DataManager.removeLocation(((Player)sender).getUniqueId());
                            return true;
                        }
                        else
                        {
                            sender.sendMessage(translate("err-no-record"));
                            return false;
                        }
                    }
                    catch(Exception e)
                    {
                        sender.sendMessage(translate("err-logback-fail"));
                        LogBack.log.severe(translate("err-logback-fail-console")
                                .replace("{0}",sender.getName())
                                .replace("{1}",e.toString()));
                        return false;
                    }
                }
                else
                {
                    sender.sendMessage(translate("err-permission-denied"));
                    return false;
                }
            }
            case 1:
            {
                if(args[0].equalsIgnoreCase("setspawn"))
                {
                    if(sender.hasPermission("logback.setspawn"))
                    {
                        try
                        {
                            DataManager.writeLocation((Player)sender,((Player)sender).getLocation(),true);
                            sender.sendMessage(translate("setspawn-success"));
                            return true;
                        }
                        catch (Exception e)
                        {
                            sender.sendMessage(translate("err-setspawn-fail"));
                            e.printStackTrace();
                            return false;
                        }
                    }
                    else
                    {
                        sender.sendMessage(translate("err-permission-denied"));
                        return false;
                    }
                }
            }
            default:
            {
                sender.sendMessage(translate("usage"));
                return false;
            }
        }
    }
}
