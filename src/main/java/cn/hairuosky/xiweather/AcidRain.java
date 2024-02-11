package cn.hairuosky.xiweather;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.HandlerList; // 引入 HandlerList 类

public class AcidRain extends BukkitRunnable implements Listener {

    private final JavaPlugin plugin;
    private final World world;
    private int duration;

    public AcidRain(JavaPlugin plugin, World world, int duration) {
        this.plugin = plugin;
        this.world = world;
        this.duration = duration;

        // 注册移动事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {
        // 在世界中的所有玩家上应用凋零效果
        for (Player player : world.getPlayers()) {
            if (isPlayerExposedToRain(player)) {
                applyAcidRainEffect(player);
            }
        }

        // 设置世界天气为雨
        world.setStorm(true);

        // 如果持续时间大于0，则减少持续时间；否则，停止酸雨效果并清除玩家身上的凋零效果
        if (duration > 0) {
            duration -= 20; // 每次减少一秒的时间（20 ticks）
        } else {
            cancel();
            clearAcidRainEffects();
            world.setStorm(false);
            // 在酸雨结束时取消移动事件监听器
            HandlerList.unregisterAll(this); // 使用 HandlerList 类的 unregisterAll 方法
        }
    }

    private boolean isPlayerExposedToRain(Player player) {
        Location playerLocation = player.getLocation();
        int playerY = playerLocation.getBlockY();
        World world = player.getWorld();
        int maxHeight = world.getMaxHeight();

        // 从玩家所在Y坐标开始，一直到最高Y坐标，检查每个位置是否有方块
        for (int y = playerY + 1; y <= maxHeight; y++) {
            Location checkLocation = new Location(world, playerLocation.getX(), y, playerLocation.getZ());
            Material blockType = checkLocation.getBlock().getType();

            // 如果某个位置有方块，则认为玩家头顶到最高Y值之间有方块，不在雨中
            if (!blockType.isAir()) {
                player.removePotionEffect(PotionEffectType.WITHER);
                return false;
            }
        }

        // 如果头顶到最高Y值之间都没有方块，则认为玩家在雨中
        return true;
    }

    private void applyAcidRainEffect(Player player) {
        // 给玩家添加凋零效果
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0, false, false));

        // 在控制台输出提示信息
        plugin.getLogger().info("Player " + player.getName() + " is affected by acid rain in world '" + world.getName() + "'.");
    }

    private void clearAcidRainEffects() {
        // 清除所有玩家的凋零效果
        for (Player player : world.getPlayers()) {
            player.removePotionEffect(PotionEffectType.WITHER);
        }
    }

    private long lastAppliedTime = 0;
    private static final long COOLDOWN_DURATION = 20L; // 20 ticks = 1 second

    // 监听玩家移动事件
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isPlayerExposedToRain(player) && System.currentTimeMillis() - lastAppliedTime >= COOLDOWN_DURATION * 1000) {
            applyAcidRainEffect(player);
            lastAppliedTime = System.currentTimeMillis();
        }
    }

    // 新增方法，设置是否在酸雨中

}
