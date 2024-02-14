package cn.hairuosky.xiweather;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Hailstorm extends BukkitRunnable implements Listener {
    private final JavaPlugin plugin;
    private final World world;
    private int duration;
    private final Random random = new Random();

    private final int SNOWBALL_RADIUS; // 设置雪球掉落半径

    private final int delay;
    private final int density;

    public Hailstorm(JavaPlugin plugin, World world, int duration, int radius, int delay, int density) {
        this.plugin = plugin;
        this.world = world;
        this.duration = duration;
        this.SNOWBALL_RADIUS = radius;
        this.delay = delay;
        this.density = density;
        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {
        // 在世界中的所有玩家上检查雪球掉落
        for (Player player : world.getPlayers()) {
            if (isPlayerExposedToStorm(player)) {
                dropSnowball(player);
            }
        }
        world.setStorm(true);
        // 如果持续时间大于0，则减少持续时间；否则，停止冰雹效果
        if (duration > 0) {
            duration -= 20; // 每次减少一秒的时间（20 ticks）
        } else {
            cancel();
            world.setStorm(false);
        }
    }

    // 判断玩家是否处于冰雹中
    private boolean isPlayerExposedToStorm(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();
        return playerLocation.getBlockY() >= world.getHighestBlockYAt(playerLocation);
    }


// 模拟雪球自由坠落
    private void dropSnowball(Player player) {
        Location playerLocation = player.getLocation();
        for (int i = 0; i < density; i++) { // 重复3次以增加掉落频率
            // 生成随机的 x 和 z 坐标，偏移不超过 SNOWBALL_RADIUS 的范围
            double offsetX = random.nextDouble() * SNOWBALL_RADIUS * 2 - SNOWBALL_RADIUS;
            double offsetZ = random.nextDouble() * SNOWBALL_RADIUS * 2 - SNOWBALL_RADIUS;

            Location dropLocation = playerLocation.clone().add(offsetX, 200, offsetZ); // 在玩家头顶200米高度处生成雪球

            // 使用BukkitRunnable延迟生成雪球
            new BukkitRunnable() {
                @Override
                public void run() {
                    // 生成雪球实体
                    Snowball snowball = (Snowball) world.spawnEntity(dropLocation, EntityType.SNOWBALL);
                    snowball.setGravity(true); // 雪球受重力影响
                    plugin.getLogger().info("Snowball dropped at " + dropLocation + " in world '" + world.getName() + "'.");
                }
            }.runTaskLater(plugin, random.nextInt(delay)); // 为每个雪球设置一个不同的延迟时间，这里设置为最多延迟5秒，单位为tick
        }
    }

}
