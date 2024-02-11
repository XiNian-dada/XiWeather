package cn.hairuosky.xiweather;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Random;

public class Fog extends BukkitRunnable {

    private final World world;
    private int durationTicks;
    private int density; // 每次运行播放的粒子数量
    private final int maxDensity; // 最大粒子密度
    private final int radius;
    private final Random random = new Random();

    public Fog(World world, int durationTicks, int maxDensity, int radius) {
        this.world = world;
        this.durationTicks = durationTicks;
        this.maxDensity = maxDensity;
        this.radius = radius;
        this.density = 0; // 初始粒子密度为0
    }

    @Override
    public void run() {
        // 逐渐增加粒子密度
        if (density < maxDensity) {
            density = density + random.nextInt(5000); // 增加粒子密度
        }

        // 获取所有在线玩家的位置并在其位置播放烟雾效果
        Collection<? extends Player> players = world.getPlayers();
        for (Player player : players) {
            Location playerLocation = player.getLocation();
            generateParticles(playerLocation);
        }

        // 如果粒子密度达到最大值，给玩家添加失明和迟钝效果
        if (density >= maxDensity - 1000) {
            for (Player player : players) {
                // 给玩家添加失明效果，设置较低的失明强度
                //player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationTicks, 0));
            }
        }

        // 减少剩余tick数
        durationTicks--;

        // 如果持续时间已经达到或超过预设的持续时间，取消任务
        if (durationTicks <= 0) {
            cancel();
            for (Player player : players) {
               // player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
            }
        }
    }

    // 生成新的粒子效果
    private void generateParticles(Location playerLocation) {
        for (int i = 0; i < density; i++) {
            // 根据传入的半径参数调整生成范围
            double offsetX = random.nextDouble() * radius * 2 - radius;
            double offsetY = random.nextDouble() * radius * 2 - radius;
            double offsetZ = random.nextDouble() * radius * 2 - radius;

            // 计算粒子效果的位置
            Location particleLocation = playerLocation.clone().add(offsetX, offsetY, offsetZ);

            // 生成随机灰度
            int greyScale = random.nextInt(256); // 0-255之间的随机数

            // 使用红石粒子模拟颜色，RGB值相同表示灰色
            Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(greyScale, greyScale, greyScale), 1);

            // 使用随机延迟
            int delayTicks = random.nextInt(20); // 在0到20之间生成随机延迟的ticks
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(XiWeather.class), () -> {
                // 播放粒子效果
                world.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, dustOptions);
            }, delayTicks);
        }
    }
}
