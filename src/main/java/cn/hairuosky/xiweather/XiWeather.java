package cn.hairuosky.xiweather;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class XiWeather extends JavaPlugin implements Listener {

    private final Random random = new Random();
    private final Map<World, Integer> weatherTasks = new HashMap<>();
    private final Map<World, Integer> rainbowTasks = new HashMap<>();
    private boolean weatherInProgress = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        int interval = config.getInt("interval") * 20; // 将间隔转换为ticks
        getLogger().info("欢迎使用XiWeather");
        getLogger().info("好好享受全新的天气效果吧");
        // 启动定时任务，每隔一定时间生成随机天气效果
        Bukkit.getScheduler().runTaskTimer(this, this::generateRandomWeather, interval, interval);
        getServer().getPluginManager().registerEvents(this, this);
        startMeteorShower();
    }

    @Override
    public void onDisable() {
        // 在插件禁用时取消所有任务
        cancelTasks();
    }

    private void cancelTasks() {
        // 取消所有天气任务
        for (int taskId : weatherTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        weatherTasks.clear(); // 清空已记录的天气任务
    }

    private void generateRandomWeather() {
        if (!weatherInProgress) {
            FileConfiguration config = getConfig();
            List<String> fogWorlds = config.getStringList("fog.worlds");
            List<String> acidRainWorlds = config.getStringList("acid_rain.worlds");
            List<String > windWorlds = config.getStringList("wind.worlds");
            List<String> thunderstormWorlds = config.getStringList("thunderstorm.worlds");
            List<String> hailstormWorlds = config.getStringList("hailstorm.worlds");
            // 生成随机数，用于选择生成的天气效果
            int randomValue = random.nextInt(4) + 1; // 生成1到3的随机整数

            // 根据随机数选择生成的天气效果
            //switch (randomValue) {
            switch (5) {
                case 1:
                    if (!acidRainWorlds.isEmpty()) {
                        startAcidRainEffect(acidRainWorlds, config);
                    }
                    break;
                case 2:
                    if (!fogWorlds.isEmpty()) {
                        startFogEffect(fogWorlds, config);
                    }
                    break;
                case 3:
                    if (!windWorlds.isEmpty()){
                        startWindEffect(windWorlds, config);
                    }
                    break;
                case 4:
                    if (!thunderstormWorlds.isEmpty()){
                        startThunderstormEffect(thunderstormWorlds, config);
                    }
                    break;
                case 5:
                    if (!hailstormWorlds.isEmpty()){
                        startHailEffect(hailstormWorlds, config);
                    }
                    break;
                default:
                    getLogger().warning("Invalid random value: " + randomValue);
                    break;
            }
        }
    }

    private void startAcidRainEffect(List<String> worlds, FileConfiguration config) {
        for (String worldName : worlds) {
            // 获取指定的世界对象
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("World '" + worldName + "' not found! Acid rain effect will not be started in this world.");
                continue;
            }

            getLogger().info("Starting acid rain effect in world '" + worldName + "'...");

            // 取消先前的酸雨任务（如果存在）
            cancelPreviousWeatherTasks(world);

            // 从配置文件中读取酸雨效果的配置
            int duration = config.getInt("acid_rain.duration");

            // 创建 AcidRain 对象并传递 XiWeather 实例
            AcidRain acidRain = new AcidRain(this, world, duration * 20);

            // 启动 AcidRain 对象，会自动调度任务
            int taskId = acidRain.runTaskTimer(this, 0, 20).getTaskId(); // 每隔一秒调度一次任务
            weatherTasks.put(world, taskId); // 记录任务ID

            // 设置天气进行中标志
            weatherInProgress = true;

            // 延迟取消天气进行中标志
            Bukkit.getScheduler().runTaskLater(this, () -> weatherInProgress = false, duration * 20L);
        }
    }

    private void startFogEffect(List<String> worlds, FileConfiguration config) {
        for (String worldName : worlds) {
            // 获取指定的世界对象
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("World '" + worldName + "' not found! Fog effect will not be started in this world.");
                continue;
            }

            getLogger().info("Starting fog effect in world '" + worldName + "'...");

            // 从配置文件中读取雾霾效果的配置
            int radius = config.getInt("fog.radius");
            int density = config.getInt("fog.density");
            int duration = config.getInt("fog.duration");

            // 创建 Fog 对象并传递 XiWeather 实例
            Fog fog = new Fog(world, duration * 20, density, radius);

            // 启动 Fog 对象，会自动调度任务
            int taskId = fog.runTaskTimer(this, 0, 20).getTaskId(); // 每隔一秒调度一次任务
            weatherTasks.put(world, taskId); // 记录任务ID

            // 设置天气进行中标志
            weatherInProgress = true;

            // 延迟取消天气进行中标志
            Bukkit.getScheduler().runTaskLater(this, () -> weatherInProgress = false, duration * 20L);
        }
    }
    // 在适当的位置调用该方法，启动流星雨效果
    private void startMeteorShower() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            FileConfiguration config = getConfig();
            List<String> meteorShowerWorlds = config.getStringList("meteor_shower.worlds");
            int chance = config.getInt("meteor_shower.chance");

            // 检查每个世界的流星雨可能性
            for (String worldName : meteorShowerWorlds) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    getLogger().info("Checking meteor shower possibility in world '" + worldName + "'...");
                    long time = world.getFullTime() % 24000; // 获取世界时间的当前tick数
                    if (time >= 13000) { // 检查是否处于夜晚
                        int randomValue = random.nextInt(100) + 1;
                        if (randomValue <= chance) {
                            int length = config.getInt("meteor_shower.length");
                            int height = config.getInt("meteor_shower.min_height");
                            getLogger().info("Starting meteor shower in world '" + worldName + "'...");
                            Meteor_Shower.startMeteorShower(world, length, height);
                        } else {
                            getLogger().info("Meteor shower did not occur this time in world '" + worldName + "'.");
                        }
                    } else {
                        getLogger().info("It's not night time in world '" + worldName + "'. Meteor shower will not occur.");
                    }
                } else {
                    getLogger().warning("World '" + worldName + "' not found! Meteor shower will not be started in this world.");
                }
            }
        }, 0, 1200); // 每一分钟（1200 ticks）检查一次
    }
    private void startWindEffect(List<String> worlds, FileConfiguration config) {
        for (String worldName : worlds) {
            // 获取指定的世界对象
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("World '" + worldName + "' not found! Wind effect will not be started in this world.");
                continue;
            }

            getLogger().info("Starting wind effect in world '" + worldName + "'...");

            // 从配置文件中读取大风天效果的配置
            int duration = config.getInt("wind.duration");
            int radius = config.getInt("wind.radius");
            int particle_count = config.getInt("wind.particle_count");
            double strength = config.getDouble("wind.strength");
            int player_speed = config.getInt("wind.player_speed");
            double distance = config.getDouble("wind.distance");
            // 创建 Wind 对象并传递 XiWeather 实例
            Wind wind = new Wind(world,particle_count,distance,strength,player_speed - 1,duration*20,radius);

            // 启动 Wind 对象，会自动调度任务
            int taskId = wind.runTaskTimer(this, 0, 20).getTaskId(); // 每隔一秒调度一次任务
            weatherTasks.put(world, taskId); // 记录任务ID

            // 设置天气进行中标志
            weatherInProgress = true;

            // 延迟取消天气进行中标志
            Bukkit.getScheduler().runTaskLater(this, () -> weatherInProgress = false, duration * 20L);
        }
    }

    private void startThunderstormEffect(List<String> worlds, FileConfiguration config) {
        for (String worldName : worlds) {
            // 获取指定的世界对象
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("World '" + worldName + "' not found! Thunderstorm effect will not be started in this world.");
                continue;
            }

            getLogger().info("Starting thunderstorm effect in world '" + worldName + "'...");

            // 从配置文件中读取雷暴效果的配置
            int duration = config.getInt("thunderstorm.duration");
            int radius = config.getInt("thunderstorm.radius");
            double chance = config.getInt("thunderstorm.chance");
            double trans_chance = chance / 100;
            int delay = config.getInt("thunderstorm.delay");

            // 创建 Thunderstorm 对象并传递 XiWeather 实例
            Thunderstorm thunderstorm = new Thunderstorm(this, world, duration * 20, radius, trans_chance, delay);

            // 启动 Thunderstorm 对象，会自动调度任务
            int taskId = thunderstorm.runTaskTimer(this, 0, 20).getTaskId(); // 每隔一秒调度一次任务
            weatherTasks.put(world, taskId); // 记录任务ID

            // 设置天气进行中标志
            weatherInProgress = true;

            // 延迟取消天气进行中标志
            Bukkit.getScheduler().runTaskLater(this, () -> weatherInProgress = false, duration * 20L);
        }
    }
    // 启动彩虹效果
    private void startRainbowEffect(World world) {
        getLogger().info("Starting rainbow effect in world '" + world.getName() + "'...");

        // 从配置文件中读取彩虹效果的配置
        int radius = getConfig().getInt("rainbow.radius");
        int duration = getConfig().getInt("rainbow.duration");
        int density = getConfig().getInt("rainbow.density");
        int regeneration = getConfig().getInt("rainbow.regeneration");
        // 创建 Rainbow 对象并传递 XiWeather 实例
        Rainbow rainbow = new Rainbow(world, duration * 20, density ,radius, regeneration-1);

        // 启动 Rainbow 对象，会自动调度任务
        int taskId = rainbow.runTaskTimer(this, 0, 20).getTaskId(); // 每隔一秒调度一次任务
        rainbowTasks.put(world, taskId); // 记录任务ID

        // 设置彩虹进行中标志

        // 延迟取消彩虹进行中标志
        Bukkit.getScheduler().runTaskLater(this, () -> getLogger().info("Rainbow effect ended in world '" + world.getName() + "'."), duration * 20L);
    }
    private void startHailEffect(List<String> worlds, FileConfiguration config) {
        for (String worldName : worlds) {
            // 获取指定的世界对象
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("World '" + worldName + "' not found! Hail effect will not be started in this world.");
                continue;
            }

            getLogger().info("Starting hail effect in world '" + worldName + "'...");



            // 从配置文件中读取冰雹效果的配置
            int density = config.getInt("hailstorm.density");
            int duration = config.getInt("hailstorm.duration");
            int radius = config.getInt("hailstorm.radius");
            int delay = config.getInt("hailstorm.delay");

            // 创建 HailEffect 对象并传递 XiWeather 实例
            Hailstorm hailstorm = new Hailstorm(this, world, duration * 20, radius, delay,density);

            // 启动 HailEffect 对象，会自动调度任务
            int taskId = hailstorm.runTaskTimer(this, 0, 20).getTaskId(); // 每隔一秒调度一次任务
            weatherTasks.put(world, taskId); // 记录任务ID

            // 设置天气进行中标志
            weatherInProgress = true;

            // 延迟取消天气进行中标志
            Bukkit.getScheduler().runTaskLater(this, () -> weatherInProgress = false, duration * 20L);
        }
    }
    // 监听天气变化事件，如果天气停止后激活彩虹效果

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        List<String> rainbowWorlds = getConfig().getStringList("rainbow.worlds");

        if (!event.toWeatherState()) { // 如果天气变为晴朗
            if (rainbowWorlds.contains(worldName) && !rainbowTasks.containsKey(world) && !weatherInProgress && !weatherTasks.containsKey(world)) {
                // 从配置文件中读取彩虹出现的概率（百分比）
                double rainbowChance = getConfig().getDouble("rainbow.chance");
                // 将百分比转换为小数
                double rainbowProbability = rainbowChance / 100.0;
                // 判断是否启动彩虹效果
                if (random.nextDouble() <= rainbowProbability) {
                    startRainbowEffect(world); // 启动彩虹效果
                }
            }
        }
    }

    private void cancelPreviousWeatherTasks(World world) {
        if (weatherTasks.containsKey(world)) {
            int taskId = weatherTasks.get(world);
            Bukkit.getScheduler().cancelTask(taskId);
            weatherTasks.remove(world);
        }
    }
}
