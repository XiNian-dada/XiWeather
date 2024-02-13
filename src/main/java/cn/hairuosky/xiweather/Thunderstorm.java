package cn.hairuosky.xiweather;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Thunderstorm extends BukkitRunnable implements Listener {
    private final JavaPlugin plugin;
    private final World world;
    private int duration;
    private final Random random = new Random();
    private final double LIGHTNING_PROBABILITY; // 设置闪电概率

    private final int STRIKE_RADIUS; // 设置闪电打击半径

    private final int delay;

    public Thunderstorm(JavaPlugin plugin, World world, int duration, int radius, double chance, int delay) {
        this.plugin = plugin;
        this.world = world;
        this.duration = duration;
        this.LIGHTNING_PROBABILITY = chance;
        this.STRIKE_RADIUS = radius;
        this.delay = delay;
        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {
        // 在世界中的所有玩家上检查闪电打击
        for (Player player : world.getPlayers()) {
            if (isPlayerExposedToStorm(player)) {
                if (random.nextDouble() <= LIGHTNING_PROBABILITY) {
                    strikeLightning(player);
                }
            }
        }
        world.setStorm(true);

        // 如果持续时间大于0，则减少持续时间；否则，停止雷暴效果
        if (duration > 0) {
            duration -= 20; // 每次减少一秒的时间（20 ticks）
        } else {
            cancel();
            world.setStorm(false);
        }
    }

    // 判断玩家是否处于雷暴中
    private boolean isPlayerExposedToStorm(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();
        return world.hasStorm() && playerLocation.getBlockY() >= world.getHighestBlockYAt(playerLocation);
    }


    // 模拟闪电打击
    private void strikeLightning(Player player) {
        Location playerLocation = player.getLocation();
        for (int i = 0; i < 3; i++) { // 重复3次以增加打击频率
            Location strikeLocation = playerLocation.clone().add(random.nextInt(STRIKE_RADIUS * 2) - STRIKE_RADIUS, 0, random.nextInt(STRIKE_RADIUS * 2) - STRIKE_RADIUS);


            new BukkitRunnable() {
                @Override
                public void run() {
                    world.strikeLightning(strikeLocation);
                    plugin.getLogger().info("Lightning strikes at " + strikeLocation + " in world '" + world.getName() + "'.");
                }
            }.runTaskLater(plugin, random.nextInt(delay)); // 为每个闪电设置一个不同的延迟时间，这里设置为最多延迟5秒，单位为tick
        }
    }
}
