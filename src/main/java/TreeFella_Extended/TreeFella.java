package TreeFella_Extended;

import TreeFella_Extended.utils.PluginState;
import com.google.common.collect.ImmutableList;
import TreeFella_Extended.files.PlacedBlocks;
import TreeFella_Extended.listeners.BlockBreakEvent;
import TreeFella_Extended.listeners.BlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;

public final class TreeFella extends JavaPlugin implements EventRegistrar {

    public static final ImmutableList<Material> LOGS = ImmutableList.of(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.MANGROVE_LOG,
            Material.SPRUCE_LOG,
            Material.DARK_OAK_LOG,
            Material.CHERRY_LOG,
            Material.WARPED_STEM,
            Material.CRIMSON_STEM
    );

    public static final ImmutableList<Material> ORES = ImmutableList.of(
            Material.COAL_ORE,
            Material.COPPER_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.EMERALD_ORE,
            Material.DIAMOND_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS
    );

    private final Map<String, String> emote_ids_list = Map.of("waving","4c8ae710-df2e-47cd-814d-cc7bf21a3d67",
            "asking everyone to follow","17428c4c-3813-4ea1-b3a9-d6a32f83afca",
            "pointing overthere","ce5c0300-7f03-455d-aaf1-352e4927b54d",
            "clapping", "9a469a61-c83b-4ba9-b507-bdbe64430582");

    // Статическое поле для хранения экземпляра плагина
    private static TreeFella instance;
    private boolean isGeyser;
    private PluginState state;
    private String toggle_emote;
    private String sneaking_emote;

    @Override
    public void onEnable() {
        instance = this;
        this.isGeyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null;
        if(this.isGeyser){
            GeyserApi.api().eventBus().register(this, this);
            GeyserApi.api().eventBus().subscribe(this, ClientEmoteEvent.class, this::onClientEmoteEvent);
            getLogger().info("You have geyser! Can use emotes for toggling!");
        }
        else getLogger().info("YOu don't have Geyser!");
        // Plugin startup logic
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        state = new PluginState();

        PlacedBlocks.setup();
        PlacedBlocks.get().options().copyDefaults(true);
        PlacedBlocks.save();

        this.getServer().getPluginManager().registerEvents(new BlockPlaceEvent(), this);
        this.getServer().getPluginManager().registerEvents(new BlockBreakEvent(state), this);
        getLogger().info("TreeFella_Extended enabled!");
        boolean sn = this.getConfig().getBoolean("need_sneaking");
        if (sn){
            state.enableSneaking();
            getLogger().info("TreeFella_Extended now requires sneaking!");
        }
        else{
            state.disableSneaking();
            getLogger().info("TreeFella_Extended now does not require sneaking!");
        }
         this.toggle_emote = this.getConfig().getString("emote_toggle");
         this.sneaking_emote = this.getConfig().getString("emote_sneaking");
    }



    @Override
    public void onDisable() {
        // Логирование при отключении плагина
        getLogger().info("TreeFella disabled!");
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, String @NonNull [] args){
        if (!command.getName().equalsIgnoreCase("tfetoggle") & !command.getName().equalsIgnoreCase("tfesneaking")){
            return false;
        }

        if (args.length ==0){
            sender.sendMessage("Using:\n/tfetoggle on|off\n/tfesneaking on|off");
        }
        String cmd = command.getName();
        switch(cmd){
            case "tfetoggle" -> {
                switch(args[0].toLowerCase()){
                    case "on" ->{
                        if(state.isEnabled()){
                            sender.sendMessage("TreeFella_Extended already works!");
                        }
                        else {
                            state.enable();
                            sender.sendMessage("TreeFella_Extended enabled");
                        }
                    }
                    case "off" -> {
                        if(!state.isEnabled()){
                            sender.sendMessage("TreeFella_Extended disabled already!");
                        }
                        else {
                            state.disable();
                            sender.sendMessage("TreeFella_Extended disabled");
                        }
                    }
                }
            }
            case "tfesneaking" ->{
                switch(args[0].toLowerCase()){
                    case "on" ->{
                        if(state.isSneaking()){
                            sender.sendMessage("TreeFella_Extended already requires sneaking!");
                        }
                        else {
                            state.enableSneaking();
                            sender.sendMessage("TreeFella_Extended require sneaking now!");
                        }
                    }
                    case "off" -> {
                        if(!state.isSneaking()){
                            sender.sendMessage("TreeFella_Extended already does not require sneaking!");
                        }
                        else {
                            state.disableSneaking();
                            sender.sendMessage("TreeFella_Extended does not require sneaking now!");
                        }
                    }
                }
            }
            default -> sender.sendMessage("Unknown command");
        }

        return true;
    }

    // Метод для получения экземпляра плагина
    public static TreeFella getInstance() {
        return instance;
    }

    @Subscribe
    public void onClientEmoteEvent(ClientEmoteEvent event){
        String emote_id = event.emoteId();

        if (emote_id.equalsIgnoreCase(this.emote_ids_list.get(this.toggle_emote))){
            Player p = Bukkit.getPlayer(Objects.requireNonNull(event.connection().playerUuid()));
            if(p==null) {
                getLogger().info("Player does not exist");
                return;
            }
            if (state.isEnabled()){
                Bukkit.getScheduler().runTask(this,() ->
                {Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tfetoggle off");});
            }
            else{
                Bukkit.getScheduler().runTask(this,() ->
                { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tfetoggle on");});
            }
        }
        else if (emote_id.equalsIgnoreCase(this.emote_ids_list.get(this.sneaking_emote))){
            Player p = Bukkit.getPlayer(Objects.requireNonNull(event.connection().playerUuid()));
            if(p==null) {
                getLogger().info("Player does not exist");
                return;
            }
            if (state.isSneaking()){
                Bukkit.getScheduler().runTask(this,() ->
                {Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tfesneaking off");});
            }
            else{
                Bukkit.getScheduler().runTask(this,() ->
                { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tfesneaking on");});
            }
        }
    }

}
