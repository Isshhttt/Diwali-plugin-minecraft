package com.ishttt.diwali.gifts;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class GiftBoxManager implements Listener {

    private final DiwaliPlugin plugin;
    private final NamespacedKey giftIdKey;
    private final NamespacedKey recipientKey;
    private final NamespacedKey senderKey;
    private final NamespacedKey contentsKey;

    // Track open packing sessions: player UUID -> session data
    private final Map<UUID, PackingSession> activeSessions = new HashMap<>();

    public static class PackingSession {
        public Entity boxEntity;
        public String selectedRecipient = null;
        public Inventory inventory;

        public PackingSession(Entity boxEntity, Inventory inventory) {
            this.boxEntity = boxEntity;
            this.inventory = inventory;
        }
    }

    public GiftBoxManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.giftIdKey = new NamespacedKey(plugin, "gift_box_id");
        this.recipientKey = new NamespacedKey(plugin, "gift_box_recipient");
        this.senderKey = new NamespacedKey(plugin, "gift_box_sender");
        this.contentsKey = new NamespacedKey(plugin, "gift_box_contents");
    }

    public NamespacedKey getGiftIdKey() {
        return giftIdKey;
    }

    public void placeGiftBox(Player player, Location targetLoc, ItemStack itemInHand) {
        World world = targetLoc.getWorld();
        if (world == null) return;

        Location spawnLoc = targetLoc.clone().add(0.5, 0.05, 0.5);
        ItemStack displayItem = plugin.getItemManager().createItem(DiwaliItemType.DIWALI_GIFT_BOX, 1);

        // Check if item in hand is already a sealed gift
        ItemMeta handMeta = itemInHand.getItemMeta();
        String sealedRecipient = null;
        String sealedSender = null;
        String sealedContents = null;

        if (handMeta != null) {
            PersistentDataContainer handPdc = handMeta.getPersistentDataContainer();
            sealedRecipient = handPdc.get(recipientKey, PersistentDataType.STRING);
            sealedSender = handPdc.get(senderKey, PersistentDataType.STRING);
            sealedContents = handPdc.get(contentsKey, PersistentDataType.STRING);
        }

        final String finalRecipient = sealedRecipient;
        final String finalSender = sealedSender;
        final String finalContents = sealedContents;

        ItemDisplay display = world.spawn(spawnLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(displayItem);
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setBrightness(new Display.Brightness(12, 12));
            ent.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(0.6f, 0.6f, 0.6f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));

            PersistentDataContainer pdc = ent.getPersistentDataContainer();
            pdc.set(giftIdKey, PersistentDataType.STRING, UUID.randomUUID().toString());

            if (finalRecipient != null) {
                pdc.set(recipientKey, PersistentDataType.STRING, finalRecipient);
            }
            if (finalSender != null) {
                pdc.set(senderKey, PersistentDataType.STRING, finalSender);
            }
            if (finalContents != null) {
                pdc.set(contentsKey, PersistentDataType.STRING, finalContents);
            }
        });

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }

        ChatUtils.playSound(spawnLoc, Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.2f);
        if (finalRecipient != null) {
            player.sendMessage(ChatUtils.color("&aPlaced sealed Diwali Gift for &e" + finalRecipient + "&a! They can left-click to open it."));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("gifts.placed"));
        }
    }

    public void openPackingGUI(Player player, Entity boxEntity) {
        PersistentDataContainer pdc = boxEntity.getPersistentDataContainer();
        if (pdc.has(recipientKey, PersistentDataType.STRING)) {
            String recipient = pdc.get(recipientKey, PersistentDataType.STRING);
            player.sendMessage(ChatUtils.color("&cThis gift box is already sealed for &e" + recipient + "&c!"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, ChatUtils.color("&6&lDiwali Gift Box &8- Pack Gifts"));

        // Fill border separator (row 1 slots 9 to 17)
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(ChatUtils.color("&7↑ Place items above ↑"));
            border.setItemMeta(bMeta);
        }
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, border);
        }

        // Active players heads for recipient selection (row 2 slots 18 to 25)
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        int slot = 18;
        for (Player target : online) {
            if (slot > 25) break;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(target);
                sm.setDisplayName(ChatUtils.color("&eGift to: &6" + target.getName()));
                head.setItemMeta(sm);
            }
            inv.setItem(slot++, head);
        }

        // Seal button in slot 26
        ItemStack seal = new ItemStack(Material.LIME_DYE);
        ItemMeta sMeta = seal.getItemMeta();
        if (sMeta != null) {
            sMeta.setDisplayName(ChatUtils.color("&a&lClick to Seal Gift"));
            sMeta.setLore(Arrays.asList(ChatUtils.color("&7Select a recipient first!")));
            seal.setItemMeta(sMeta);
        }
        inv.setItem(26, seal);

        activeSessions.put(player.getUniqueId(), new PackingSession(boxEntity, inv));
        player.openInventory(inv);
        ChatUtils.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PackingSession session = activeSessions.get(player.getUniqueId());
        if (session == null || event.getInventory() != session.inventory) return;

        int rawSlot = event.getRawSlot();

        // Prevent taking border glass
        if (rawSlot >= 9 && rawSlot < 18) {
            event.setCancelled(true);
            return;
        }

        // Clicking a player head to select recipient
        if (rawSlot >= 18 && rawSlot < 26) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                SkullMeta sm = (SkullMeta) clicked.getItemMeta();
                if (sm != null && sm.getOwningPlayer() != null) {
                    session.selectedRecipient = sm.getOwningPlayer().getName();
                    ChatUtils.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);

                    // Update seal button
                    ItemStack seal = new ItemStack(Material.EMERALD);
                    ItemMeta sMeta = seal.getItemMeta();
                    if (sMeta != null) {
                        sMeta.setDisplayName(ChatUtils.color("&a&l✓ Seal Gift for &e" + session.selectedRecipient));
                        sMeta.setLore(Arrays.asList(ChatUtils.color("&7Click to finish packing!")));
                        seal.setItemMeta(sMeta);
                    }
                    session.inventory.setItem(26, seal);
                }
            }
            return;
        }

        // Clicking Seal Button
        if (rawSlot == 26) {
            event.setCancelled(true);
            if (session.selectedRecipient == null) {
                player.sendMessage(ChatUtils.color("&cPlease select a recipient player first from the heads below!"));
                ChatUtils.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Collect gift items in top 9 slots (0..8)
            List<ItemStack> gifts = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack item = session.inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    gifts.add(item);
                }
            }

            if (gifts.isEmpty()) {
                player.sendMessage(ChatUtils.color("&cPlease put at least one item into the gift box!"));
                ChatUtils.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Save to box entity
            String encodedContents = serializeItemStacks(gifts);
            PersistentDataContainer pdc = session.boxEntity.getPersistentDataContainer();
            pdc.set(recipientKey, PersistentDataType.STRING, session.selectedRecipient);
            pdc.set(senderKey, PersistentDataType.STRING, player.getName());
            pdc.set(contentsKey, PersistentDataType.STRING, encodedContents);

            player.closeInventory();
            activeSessions.remove(player.getUniqueId());

            ChatUtils.playSound(session.boxEntity.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.2f);
            ChatUtils.playSound(session.boxEntity.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);

            String msg = plugin.getConfigManager().getMessage("gifts.sealed")
                    .replace("{recipient}", session.selectedRecipient);
            player.sendMessage(msg);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            PackingSession session = activeSessions.remove(player.getUniqueId());
            if (session != null) {
                // If closed without sealing, drop placed items back to player
                PersistentDataContainer pdc = session.boxEntity.getPersistentDataContainer();
                if (!pdc.has(recipientKey, PersistentDataType.STRING)) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack item = session.inventory.getItem(i);
                        if (item != null && item.getType() != Material.AIR) {
                            player.getInventory().addItem(item);
                        }
                    }
                }
            }
        }
    }

    public boolean handleBreak(Entity entity, Player player) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(giftIdKey, PersistentDataType.STRING)) return false;

        Location dropLoc = entity.getLocation();
        ItemStack dropItem = plugin.getItemManager().createItem(DiwaliItemType.DIWALI_GIFT_BOX, 1);

        if (pdc.has(recipientKey, PersistentDataType.STRING)) {
            String recipient = pdc.get(recipientKey, PersistentDataType.STRING);
            String sender = pdc.get(senderKey, PersistentDataType.STRING);
            String contents = pdc.get(contentsKey, PersistentDataType.STRING);

            ItemMeta meta = dropItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatUtils.color("&e&lDiwali Gift &8(&aFor: &f" + recipient + "&8)"));
                List<String> lore = Arrays.asList(
                        ChatUtils.color("&7A beautifully wrapped festival gift."),
                        ChatUtils.color("&6From: &e" + sender),
                        ChatUtils.color("&6For: &a" + recipient),
                        ChatUtils.color("&ePlace and left-click to open!")
                );
                meta.setLore(lore);

                PersistentDataContainer itemPdc = meta.getPersistentDataContainer();
                itemPdc.set(recipientKey, PersistentDataType.STRING, recipient);
                itemPdc.set(senderKey, PersistentDataType.STRING, sender);
                if (contents != null) {
                    itemPdc.set(contentsKey, PersistentDataType.STRING, contents);
                }
                dropItem.setItemMeta(meta);
            }
        }

        entity.remove();
        if (dropLoc.getWorld() != null) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, dropItem);
            ChatUtils.playSound(dropLoc, Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.2f);
        }
        return true;
    }

    public boolean handleUnwrap(Entity entity, Player player) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(giftIdKey, PersistentDataType.STRING)) return false;

        if (!pdc.has(recipientKey, PersistentDataType.STRING)) {
            return false; // Not sealed yet
        }

        String recipient = pdc.get(recipientKey, PersistentDataType.STRING);
        String sender = pdc.get(senderKey, PersistentDataType.STRING);
        String contents = pdc.get(contentsKey, PersistentDataType.STRING);

        // Only the assigned recipient (or the original sender) can open it
        if (!player.getName().equalsIgnoreCase(recipient) && !player.getName().equalsIgnoreCase(sender)) {
            String msg = plugin.getConfigManager().getMessage("gifts.not-yours")
                    .replace("{recipient}", recipient != null ? recipient : "Someone");
            player.sendMessage(msg);
            ChatUtils.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return true;

        // Celebratory unwrap: confetti, firework, sound burst!
        world.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 0.5, 0), 40, 0.6, 0.6, 0.6, 0.1);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 0.5, 0), 30, 0.5, 0.5, 0.5, 0.1);

        ChatUtils.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 1.0f);
        ChatUtils.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, 1.2f);
        ChatUtils.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        // Drop contents
        if (contents != null) {
            List<ItemStack> items = deserializeItemStacks(contents);
            for (ItemStack item : items) {
                world.dropItemNaturally(loc, item);
            }
        }

        String msg = plugin.getConfigManager().getMessage("gifts.unwrapped")
                .replace("{sender}", sender != null ? sender : "A Friend");
        player.sendMessage(msg);

        entity.remove();
        return true;
    }

    private static String serializeItemStacks(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.size());
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return "";
        }
    }

    private static List<ItemStack> deserializeItemStacks(String base64) {
        List<ItemStack> items = new ArrayList<>();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                items.add((ItemStack) dataInput.readObject());
            }
            dataInput.close();
        } catch (Exception ignored) {}
        return items;
    }
}
