package vip.floatationdevice.msu.logback;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.UUID;

import static vip.floatationdevice.msu.logback.LogBack.cm;

public class DataManager
{
    private final LogBack instance;
    private final File dataDir; // data folder: (server_root)/plugins/LogBack/data
    private Location cachedSpawnLocation;

    public DataManager(LogBack instance)
    {
        this.instance = instance;
        dataDir = new File(instance.getDataFolder(), "data");
        String dataDirPath = dataDir.toPath().toAbsolutePath().toString();
//        log.info("Data folder: " + dataDirPath);
        if(!dataDir.exists())
            if(!dataDir.mkdirs())
                throw new RuntimeException("Failed to create data folder: " + dataDirPath);
    }

    /**
     * Save a location record to the data folder.
     * The record will be saved as "(player UUID).txt", or "spawn.txt" if `isSpawnPoint` is true.
     * @param p The player who requested for saving location.
     * @param l The location to save.
     * @param isSpawnPoint If true, the location will be used as login location.
     * @throws Exception If any IO exception or other problem occurs.
     */
    void writeLocation(Player p, Location l, boolean isSpawnPoint) throws Exception
    {
        File f = new File(dataDir, (isSpawnPoint ? "spawn" : p.getUniqueId()) + ".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));

        bw.write(l.getWorld().getName());
        bw.write(' ');
        bw.write(String.valueOf(l.getX()));
        bw.write(' ');
        bw.write(String.valueOf(l.getY()));
        bw.write(' ');
        bw.write(String.valueOf(l.getZ()));
        bw.write(' ');
        bw.write(String.valueOf(l.getYaw()));
        bw.write(' ');
        bw.write(String.valueOf(l.getPitch()));
        bw.write('\n');
        bw.write(p.getName());
        bw.flush();
        bw.close();

        if(isSpawnPoint)
            cachedSpawnLocation = l;
    }

    /**
     * Read a location record from the data folder.
     * @param u The UUID of the record. If not null, read "(UUID).txt"; else read "spawn.txt".
     * @return The saved location.
     * @throws Exception If any IO exception or other problem occurs.
     */
    Location readLocation(UUID u) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(new File(dataDir, (u == null ? "spawn" : u) + ".txt")));
        String line = br.readLine();
        br.close();
        String[] data = line.split(" ");
        return new Location(
                instance.getServer().getWorld(data[0]),
                Double.parseDouble(data[1]),
                Double.parseDouble(data[2]),
                Double.parseDouble(data[3]),
                Float.parseFloat(data[4]),
                Float.parseFloat(data[5]));
    }

    /**
     * Get the login location used by LogBack.
     * @return Location saved in the data folder, or Minecraft spawn point if config `useMinecraftSpawnPoint` is true.
     * @throws Exception If any IO exception or other problem occurs.
     */
    Location readSpawnLocation() throws Exception
    {
        if(cachedSpawnLocation != null)
            return cachedSpawnLocation;

        if(cm.get(Boolean.class, "useMinecraftSpawnPoint"))
            return cachedSpawnLocation = instance.getServer().getWorlds().get(0).getSpawnLocation();
        else
            return cachedSpawnLocation = readLocation(null);
    }

    /**
     * Remove a record from the data folder.
     * @param u The UUID of the record.
     */
    void removeLocation(UUID u)
    {
        if(isRecorded(u))
            new File(dataDir, u + ".txt").delete();
    }

    /**
     * Check if the login location has been set.
     * @return True if "spawn.txt" exists, false otherwise.
     */
    boolean isSpawnSet()
    {
        File f = new File(dataDir, "spawn.txt");
        return f.exists() && f.canRead() && f.isFile();
    }

    /**
     * Check if a record with the specified UUID exists.
     * @param u The UUID of the record.
     * @return True if "(UUID).txt" exists, false otherwise.
     */
    boolean isRecorded(UUID u)
    {
        File f = new File(dataDir, u + ".txt");
        return f.exists() && f.canRead() && f.isFile();
    }
}
