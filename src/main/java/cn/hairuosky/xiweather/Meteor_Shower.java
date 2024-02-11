package cn.hairuosky.xiweather;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Random;

public class Meteor_Shower extends BukkitRunnable {

    private final World world;
    private final int length;
    private final Random random = new Random();

    public Meteor_Shower(World world, int length) {
        this.world = world;
        this.length = length;
    }

    @Override
    public void run() {
        // 获取所有在线玩家的位置并在其附近生成流星雨效果
        Collection<? extends Player> players = world.getPlayers();
        for (Player player : players) {
            if (player.getLocation().getY() > 70) {
                Location playerLocation = player.getLocation();
                generateMeteorShower(playerLocation);
            }
        }
    }

    // 生成流星雨效果
    private void generateMeteorShower(Location playerLocation) {
        double playerX = playerLocation.getX();
        double playerZ = playerLocation.getZ();

        double startY = playerLocation.getY() + 30; // 流星的Y坐标始终在玩家头顶30米处
        double endY = playerLocation.getY() + 30; // 流星的Y坐标末尾也是在玩家头顶30米处

        double startX = playerX + random.nextDouble() * 30 - 15;
        double startZ = playerZ + random.nextDouble() * 30 - 15;
        double endX = playerX + random.nextDouble() * 30 - 15;
        double endZ = playerZ + random.nextDouble() * 30 - 15;


        // 计算流星的步长
        double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endZ - startZ, 2)); // 计算起点到终点的距离
        double stepX = (endX - startX) / (distance * 40); // 计算X方向上的步长，40是因为每米20步，要达到1秒两米的速度，需要40步
        double stepZ = (endZ - startZ) / (distance * 40); // 计算Z方向上的步长

        // 生成流星雨效果
        for (int i = 0; i < distance * 20; i++) { // 根据流星的长度计算生成粒子的次数
            double posX = startX + stepX * i + random.nextDouble() * 0.1 - 0.05;
            double posY = startY + random.nextDouble() * (endY - startY); // 在30米范围内随机选择流星的Y坐标
            double posZ = startZ + stepZ * i + random.nextDouble() * 0.1 - 0.05;

            long delay = (long) (i * 0.02);
            new BukkitRunnable() {
                @Override
                public void run() {
                    // 播放粒子效果
                    for (int j = 0; j < length; j++) {
                        double offsetY = random.nextDouble() * 0.2 - 0.1;
                        double offsetZ = random.nextDouble() * 0.2 - 0.1;
                        world.spawnParticle(Particle.FIREWORKS_SPARK, new Location(world, posX, posY + offsetY, posZ + offsetZ), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, new Location(world, posX, posY + offsetY, posZ + offsetZ), 1, 0, 0, 0, 0);
                    }
                }

            }.runTaskLater(JavaPlugin.getPlugin(XiWeather.class), delay); // 每隔0.25秒执行一次，增加密度
        }
    }

    public static void startMeteorShower(World world, int length) {
        Meteor_Shower meteorShower = new Meteor_Shower(world, length);
        meteorShower.runTaskTimer(JavaPlugin.getPlugin(XiWeather.class), 0, 100); // 每隔0.25秒执行一次任务，增加密度
    }
}
