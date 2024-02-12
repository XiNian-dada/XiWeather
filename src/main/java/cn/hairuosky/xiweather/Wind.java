package cn.hairuosky.xiweather;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Random;

public class Wind extends BukkitRunnable {

    private final World world;
    private final double angle; // 固定的角度
    private final int particleCount; // 粒子的数量
    private final double distance; // 粒子生成的总距离
    private final double particleSpeed; // 粒子移动的速度
    private final int radius; // 立方体的半径
    private final Random random = new Random(); // 创建Random对象
    private final int playerSpeed; // 玩家获得的速度
    private int duration; // 刮风持续时间（以ticks为单位）

    public Wind(World world, int particleCount, double distance, double particleSpeed, int playerSpeed, int duration,int radius) {
        this.world = world;
        this.angle = generateRandomAngle(); // 生成随机角度值
        this.particleCount = particleCount; // 设置粒子的数量
        this.distance = distance; // 设置粒子生成的总距离
        this.particleSpeed = particleSpeed; // 设置粒子移动的速度
        this.playerSpeed = playerSpeed; // 设置玩家获得的速度
        this.duration = duration; // 设置刮风持续时间（以ticks为单位）
        this.radius = radius;
    }

    @Override
    public void run() {
        if (duration <= 0) {
            cancel(); // 如果持续时间结束，取消任务
            clearEffect();
            return;
        }

        generateWindParticles();
        duration -= 20; // 每次运行任务，持续时间减少1s
    }

    private void generateWindParticles() {
        Collection<? extends Player> players = world.getPlayers();
        for (Player player : players) {
            Location playerLocation = player.getLocation().clone().add(0, 1, 0); // 获取玩家头顶位置

            // 随机选择立方体内的一个点作为起始点
            double startX = playerLocation.getX() - radius + random.nextDouble() * (2 * radius);
            double startY = playerLocation.getY() - radius + random.nextDouble() * (2 * radius);
            double startZ = playerLocation.getZ() - radius + random.nextDouble() * (2 * radius);

            // 计算步长
            double stepSize = distance / particleCount;

            for (int i = 0; i < particleCount; i++) {
                // 计算粒子生成的延迟时间
                long delayTicks = Math.round(i / particleSpeed);

                // 根据固定角度和步长计算粒子的位置，并添加微小随机偏移量
                double x = startX + Math.cos(angle) * (i * stepSize) + (random.nextDouble() - 0.5) * 0.1;
                double z = startZ + Math.sin(angle) * (i * stepSize) + (random.nextDouble() - 0.5) * 0.1;
                Location particleLocation = new Location(world, x, startY, z);
                spawnParticleLater(particleLocation, (int) delayTicks); // 使用粒子的延迟时间作为参数
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration + 30, playerSpeed));
            // 将速度效果应用于玩家，持续时间为duration ticks
        }
    }

    private void spawnParticleLater(Location location, int delayTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 1, new Particle.DustOptions(org.bukkit.Color.WHITE, 1));
            }
        }.runTaskLater(XiWeather.getPlugin(XiWeather.class), delayTicks);
    }

    private double generateRandomAngle() {
        return random.nextDouble() * 2 * Math.PI; // 生成随机角度（0 到 2π）
    }

    private void clearEffect(){
        // 清除所有玩家的速度效果
        for (Player player : world.getPlayers()) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }
}