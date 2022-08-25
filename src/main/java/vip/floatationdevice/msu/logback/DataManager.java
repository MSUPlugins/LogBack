package vip.floatationdevice.msu.logback;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.UUID;

public class DataManager
{
    // ./plugins/LogBack/data/
    private static final String DATA_DIR = "." + File.separator + "plugins" + File.separator + LogBack.instance.getName() + File.separator + "data" + File.separator;

    static void writeLocation(Player p, Location l, boolean isSpawnPoint) throws Exception
    {
        String filePath = DATA_DIR + (isSpawnPoint ? "spawn" : p.getUniqueId()) + ".txt";
        new File(new File(filePath).getParent()).mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        bw.write(
                l.getWorld().getName() + " "
                        + l.getX() + " "
                        + l.getY() + " "
                        + l.getZ() + " "
                        + l.getYaw() + " "
                        + l.getPitch() + "\n"
                        + p.getName()
        );
        bw.flush();
        bw.close();
    }

    static Location readLocation(UUID u) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(DATA_DIR + u + ".txt"));
        String line = br.readLine();
        br.close();
        String[] data = line.split(" ");
        return new Location(
                LogBack.instance.getServer().getWorld(data[0]),
                Double.parseDouble(data[1]),
                Double.parseDouble(data[2]),
                Double.parseDouble(data[3]),
                Float.parseFloat(data[4]),
                Float.parseFloat(data[5]));
    }

    static Location readSpawnLocation() throws Exception
    {
        if(ConfigManager.useMinecraftSpawnPoint())
        {
            return LogBack.instance.getServer().getWorlds().get(0).getSpawnLocation();
        }
        else
        {
            BufferedReader br = new BufferedReader(new FileReader(DATA_DIR + "spawn.txt"));
            String line = br.readLine();
            br.close();
            String[] data = line.split(" ");
            return new Location(
                    LogBack.instance.getServer().getWorld(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Double.parseDouble(data[3]),
                    Float.parseFloat(data[4]),
                    Float.parseFloat(data[5]));
        }
    }

    static void removeLocation(UUID u)
    {
        if(isRecorded(u))
            new File(DATA_DIR + u + ".txt").delete();
    }

    static boolean isSpawnSet(){return new File(DATA_DIR + "spawn.txt").exists();}

    static boolean isRecorded(UUID u)
    {
        return new File(DATA_DIR + u + ".txt").exists();
    }
}
