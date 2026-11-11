
package com.Daniel.changeItemName.main;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class UserItemName extends JavaPlugin {

	public static Economy Eco;

	protected Set<String> bannedItem = new HashSet<>();
	protected double price = 6000;

	protected String BannedMsg = "§eServer §7>> §c해당 아이템은 이름변경이 불가능합니다.";
	protected String PermDeny = "§eServer §7>> §c권한이 없습니다.";
	protected String NoMoney = "§eServer §7>> §c충분한 돈이 없습니다. 부족한 돈: %price%원";
	protected String NoItem = "§eServer §7>> §c손에 아이템을 들고 있어야 합니다.";
	protected String NoColorCode = "§eServer §7>> §a\"&\"§c 문자는 사용이 불가능합니다.";
	protected String Done = "§eServer §7>> §f%price%원을 사용해 아이템 이름을 변경 하였습니다.";

	@Override
	public void onEnable() {
		reloadConfiguration();

		if (!SetupEconomy()) {
			Bukkit.getConsoleSender().sendMessage(
					"§6§l[ Item Name ] §c§lEconomy §f플러그인이 인식되지 않았으므로, 비활성화 됩니다."
			);

			getServer().getPluginManager().disablePlugin(this);
			Bukkit.shutdown();
			return;
		}

		Bukkit.getConsoleSender().sendMessage(
				"§6§l[ Item Name ] §a§l" + Eco.getName()
						+ " §f플러그인이 인식 되었습니다."
		);

		getLogger().info("Plugin enabled.");
	}

	@Override
	public void onDisable() {
		getLogger().info("Plugin disabled.");
	}

	private boolean SetupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getConsoleSender().sendMessage(
					"§6§l[ Item Name ] §c§lVault §f플러그인이 인식되지 않았으므로, 서버가 종료 됩니다."
			);
			Bukkit.shutdown();
			return false;
		}

		Bukkit.getConsoleSender().sendMessage(
				"§6§l[ Item Name ] §a§lVault §f플러그인이 인식 되었습니다."
		);

		RegisteredServiceProvider<Economy> provider =
				getServer().getServicesManager()
						.getRegistration(Economy.class);

		if (provider == null) {
			return false;
		}

		Eco = provider.getProvider();
		return Eco != null;
	}

	public void reloadConfiguration() {
		saveDefaultConfig();
		reloadConfig();

		FileConfiguration config = getConfig();

		bannedItem = new HashSet<>(
				config.getStringList("BannedItem")
		);

		price = config.getInt("price");

		BannedMsg = loadMessage(config, "BannedMsg", BannedMsg);
		PermDeny = loadMessage(config, "PermDeny", PermDeny);
		NoMoney = loadMessage(config, "NoMoney", NoMoney);
		NoItem = loadMessage(config, "NoItem", NoItem);
		NoColorCode = loadMessage(config, "NoColorCode", NoColorCode);
		Done = loadMessage(config, "Done", Done);
	}

	private String loadMessage(
			FileConfiguration config,
			String key,
			String fallback) {

		String message = config.getString(key);

		if (message == null || message.isEmpty()) {
			return fallback;
		}

		return ChatColor.translateAlternateColorCodes('&', message);
	}

	@Override
	public boolean onCommand(
			final CommandSender sender,
			final Command command,
			final String commandLabel,
			final String[] args) {

		// Reload commands
		if (commandLabel.equalsIgnoreCase("아이템이름변경리로드")
				|| commandLabel.equalsIgnoreCase("이름변경리로드")) {

			if (!sender.hasPermission("UserItemName.reload")
					&& !sender.isOp()) {
				sender.sendMessage(PermDeny);
				return true;
			}

			reloadConfiguration();
			sender.sendMessage(
					"§8[ §b 아이템 이름 변경 §8]§a리로드가 완료되었습니다. "
			);
			return true;
		}

		// Rename commands
		if (!commandLabel.equalsIgnoreCase("아이템이름변경")
				&& !commandLabel.equalsIgnoreCase("이름변경")) {
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("콘솔에서는 사용이 불가능한 명령어 입니다");
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(
					"§eServer §7>>§f /아이템이름변경 <아이템 이름 문자열> : 아이템 한개당 "
							+ price + "원을 소비 하여 아이템 이름을 바꿉니다. "
			);

			sender.sendMessage(
					"§eServer §7>>§f /이름변경 <아이템 이름 문자열> : 아이템 한개당 "
							+ price + "원을 소비 하여 아이템 이름을 바꿉니다. "
			);
			return true;
		}

		// Preserve original trailing space in item name.
		StringBuilder builder = new StringBuilder();

		for (String arg : args) {
			builder.append(arg).append(' ');
		}

		String name = builder.toString();

		if (name.contains("&")) {
			sender.sendMessage(NoColorCode);
			return true;
		}

		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInHand();

		if (item == null || item.getType() == Material.AIR) {
			sender.sendMessage(NoItem);
			return true;
		}

		if (bannedItem.contains(item.getType().toString())) {
			sender.sendMessage(BannedMsg);
			return true;
		}

		double total = price * item.getAmount();

		// Read balance only once.
		double balance = Eco.getBalance(player);

		if (balance < total) {
			sender.sendMessage(
					NoMoney.replace("%price%", String.valueOf(total - balance))
			);
			return true;
		}

		ItemMeta itemMeta = item.getItemMeta();

		if (itemMeta == null) {
			sender.sendMessage(NoItem);
			return true;
		}

		itemMeta.setDisplayName(name);
		item.setItemMeta(itemMeta);

		Eco.withdrawPlayer(player, total);

		sender.sendMessage(
				Done.replace("%price%", String.valueOf(total))
		);

		return true;
	}
}
