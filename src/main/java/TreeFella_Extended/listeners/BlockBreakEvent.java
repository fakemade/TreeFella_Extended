package TreeFella_Extended.listeners;

import TreeFella_Extended.utils.PluginState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import TreeFella_Extended.TreeFella;
import TreeFella_Extended.files.PlacedBlocks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockBreakEvent implements Listener {

    private final Set<Location> processedBlocks = new HashSet<>();

    private final ImmutableList<Material> AXES = ImmutableList.of(
            Material.DIAMOND_AXE,
            Material.GOLDEN_AXE,
            Material.IRON_AXE,
            Material.COPPER_AXE,
            Material.STONE_AXE,
            Material.NETHERITE_AXE,
            Material.WOODEN_AXE
    );

    private final ImmutableList<Material> PICKAXES = ImmutableList.of(
            Material.DIAMOND_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE,
            Material.COPPER_PICKAXE,
            Material.STONE_PICKAXE,
            Material.NETHERITE_PICKAXE,
            Material.WOODEN_PICKAXE
    );

    // Маппинг руд к их "группе" (один тип руды может быть в разных блоках)
    private final ImmutableMap<Material, String> ORE_GROUPS = ImmutableMap.<Material, String>builder()
            // Угольная руда
            .put(Material.COAL_ORE, "COAL")
            .put(Material.DEEPSLATE_COAL_ORE, "COAL")
            // Железная руда
            .put(Material.IRON_ORE, "IRON")
            .put(Material.DEEPSLATE_IRON_ORE, "IRON")
            // Медная руда
            .put(Material.COPPER_ORE, "COPPER")
            .put(Material.DEEPSLATE_COPPER_ORE, "COPPER")
            // Золотая руда
            .put(Material.GOLD_ORE, "GOLD")
            .put(Material.DEEPSLATE_GOLD_ORE, "GOLD")
            // Редстоун руда
            .put(Material.REDSTONE_ORE, "REDSTONE")
            .put(Material.DEEPSLATE_REDSTONE_ORE, "REDSTONE")
            // Изумрудная руда
            .put(Material.EMERALD_ORE, "EMERALD")
            .put(Material.DEEPSLATE_EMERALD_ORE, "EMERALD")
            // Лазуритовая руда
            .put(Material.LAPIS_ORE, "LAPIS")
            .put(Material.DEEPSLATE_LAPIS_ORE, "LAPIS")
            // Алмазная руда
            .put(Material.DIAMOND_ORE, "DIAMOND")
            .put(Material.DEEPSLATE_DIAMOND_ORE, "DIAMOND")
            // Незеритовая руда
            .put(Material.NETHER_GOLD_ORE, "NETHER_GOLD")
            .put(Material.NETHER_QUARTZ_ORE, "NETHER_QUARTZ")
            .put(Material.ANCIENT_DEBRIS, "ANCIENT_DEBRIS")
            .build();

    private PluginState _state;

    public BlockBreakEvent(PluginState state) {
        _state = state;
    }

    @EventHandler
    public void onBlockBreakEvent(org.bukkit.event.block.BlockBreakEvent e) {
        if (!_state.isEnabled()) return;
        Player p = e.getPlayer();
        if(_state.isSneaking() && !p.isSneaking()) return;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        Block b = e.getBlock();

        int blockX = b.getLocation().getBlockX();
        int blockY = b.getLocation().getBlockY();
        int blockZ = b.getLocation().getBlockZ();

        String path = blockX + "," + blockY + "," + blockZ;

        boolean isPlayerPlacedBlock = PlacedBlocks.get().isInt(path);

        if (
                (
                        (
                                AXES.contains(itemInHand.getType()) && TreeFella.LOGS.contains(b.getType())
                        ) || (
                                PICKAXES.contains(itemInHand.getType()) && TreeFella.ORES.contains(b.getType())
                        )
                ) && !isPlayerPlacedBlock
        ) {
            // Отменяем стандартный дроп первого блока
            e.setDropItems(false);

            // Обрабатываем первый блок вручную
            Collection<ItemStack> drops = b.getDrops(itemInHand);
            HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(drops.toArray(new ItemStack[0]));

            // Если не влезло - дропаем
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    b.getWorld().dropItemNaturally(b.getLocation(), item);
                }
            }

            // Определяем группу руды или тип блока
            String oreGroup = getOreGroup(b.getType());
            this.startChainBreak(b, p, oreGroup);

        } else if (isPlayerPlacedBlock && TreeFella.LOGS.contains(b.getType())) {
            PlacedBlocks.get().set(path, null);
        }
    }

    // Получить группу руды или вернуть null для брёвен
    private String getOreGroup(Material material) {
        return ORE_GROUPS.getOrDefault(material, null);
    }

    // Проверить, принадлежит ли блок к той же группе руды или типу
    private boolean isSameOreGroup(Material material, String oreGroup, Material originalType) {
        // Если это бревно, проверяем точное совпадение типа
        if (TreeFella.LOGS.contains(material)) {
            return material == originalType;
        }

        // Если это руда, проверяем группу
        if (oreGroup != null && ORE_GROUPS.containsKey(material)) {
            return oreGroup.equals(ORE_GROUPS.get(material));
        }

        return false;
    }

    public void startChainBreak(Block startBlock, Player player, String oreGroup) {
        processedBlocks.clear();

        // Добавляем первый блок в обработанные
        processedBlocks.add(startBlock.getLocation());

        checkNearbyBlocks(startBlock, player, oreGroup, startBlock.getType(), 0);
    }

    private void checkNearbyBlocks(Block block, Player player, String oreGroup, Material originalType, int delay) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        int x = -1;
        int y = -1;
        int z = -1;

        int currentDelay = delay;

        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Location center = block.getLocation().clone();
                    center.add(x, y, z);

                    // Проверяем, не обработан ли уже блок
                    if (processedBlocks.contains(center)) {
                        z++;
                        continue;
                    }

                    Block nearbyBlock = center.getBlock();

                    // Проверяем, принадлежит ли блок к той же группе
                    if ((TreeFella.LOGS.contains(nearbyBlock.getType()) || TreeFella.ORES.contains(nearbyBlock.getType())) &&
                            isSameOreGroup(nearbyBlock.getType(), oreGroup, originalType)) {

                        processedBlocks.add(center);

                        // ЗАДЕРЖКА: Запускаем разрушение блока через N тиков
                        final int finalDelay = currentDelay;
                        Bukkit.getScheduler().runTaskLater(TreeFella.getPlugin(TreeFella.class), () -> {
                            breakBlockWithEffects(nearbyBlock, player, oreGroup, originalType, finalDelay);
                        }, currentDelay);

                        currentDelay += 1;
                    }

                    z++;
                }

                x++;
                z = -1;
            }

            y++;
            x = -1;
            z = -1;
        }
    }

    private void breakBlockWithEffects(Block nearbyBlock, Player player, String oreGroup, Material originalType, int currentDelay) {
        // Проверяем, что блок ещё принадлежит к нужной группе
        if (!isSameOreGroup(nearbyBlock.getType(), oreGroup, originalType)) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Проверяем прочность инструмента
        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable dmg = (Damageable) meta;
            int toolDurability = tool.getType().getMaxDurability();

            // Если инструмент сейчас сломается
            if (dmg.getDamage() >= toolDurability - 1) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                return;
            }
        }

        Location center = nearbyBlock.getLocation();

        // Спавним частицы
        nearbyBlock.getWorld().spawnParticle(
                Particle.BLOCK,
                center.clone().add(0.5, 0.5, 0.5),
                10,
                0.25, 0.25, 0.25,
                0.1,
                nearbyBlock.getBlockData()
        );

        // Звук разрушения
        nearbyBlock.getWorld().playSound(
                center,
                nearbyBlock.getBlockSoundGroup().getBreakSound(),
                SoundCategory.BLOCKS,
                0.5f,
                1.0f
        );

        // Получаем дропы ПЕРЕД удалением блока
        Collection<ItemStack> drops = nearbyBlock.getDrops(tool);

        // Удаляем блок
        nearbyBlock.setType(Material.AIR);

        // Добавляем в инвентарь
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drops.toArray(new ItemStack[0]));

        // Дропаем остатки
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                nearbyBlock.getWorld().dropItemNaturally(center, item);
            }
        }

        // Изнашиваем инструмент
        if (meta instanceof Damageable) {
            Damageable dmg = (Damageable) meta;
            dmg.setDamage(dmg.getDamage() + 1);
            tool.setItemMeta(dmg);
            player.getInventory().setItemInMainHand(tool);
        }

        // Рекурсивно обрабатываем соседние блоки
        this.checkNearbyBlocks(nearbyBlock, player, oreGroup, originalType, currentDelay + 1);
    }
}