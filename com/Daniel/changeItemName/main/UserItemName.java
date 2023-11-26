
package com.Daniel.changeItemName.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.command.Command;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class UserItemName extends JavaPlugin {
	public final Logger logger;
	public static Command plugin;
	public static Economy Eco;
	protected List<String> bannedItem;
	protected double price = 6000;

	protected String BannedMsg = "§eServer §7>> §c해당 아이템은 이름변경이 불가능합니다.";
	protected String PermDeny = "§eServer §7>> §c권한이 없습니다.";
	protected String NoMoney = "§eServer §7>> §c충분한 돈이 없습니다. 부족한 돈: %price%원";
	protected String NoItem = "§eServer §7>> §c손에 아이템을 들고 있어야 합니다.";
	protected String NoColorCode = "§eServer §7>> §a\"&\"§c 문자는 사용이 불가능합니다.";
	protected String Done = "§eServer §7>> §f%price%원을 사용해 아이템 이름을 변경 하였습니다.";

	public UserItemName() {
		this.logger = Logger.getLogger("Minecraft");
	}

	public void onDisable() {
		final PluginDescriptionFile pdFile = this.getDescription();
		System.out.println(String.valueOf(pdFile.getName()) + pdFile.getVersion() + "이(가) 비활성화 되었습니다.");
	}

	public void onEnable() {
		final PluginDescriptionFile pdFile = this.getDescription();
		System.out.println(String.valueOf(pdFile.getName()) + pdFile.getVersion() + "이(가) 활성화 되었습니다.");
		this.reloadConfiguration();

		if (!SetupEconomy()) {
			Bukkit.getConsoleSender().sendMessage("§6§l[ Item Name ] §c§lEconomy §f플러그인이 인식되지 않았으므로, 비활성화 됩니다.");
			getServer().getPluginManager().disablePlugin(this);
			Bukkit.shutdown();
			return;
		}

		Bukkit.getConsoleSender().sendMessage("§6§l[ Item Name ] §a§l" + Eco.getName() + " §f플러그인이 인식 되었습니다.");

	}

	private boolean SetupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getConsoleSender().sendMessage("§6§l[ Item Name ] §c§lVault §f플러그인이 인식되지 않았으므로, 서버가 종료 됩니다.");
			Bukkit.shutdown();
			return false;
		}

		Bukkit.getConsoleSender().sendMessage("§6§l[ Item Name ] §a§lVault §f플러그인이 인식 되었습니다.");
		RegisteredServiceProvider<Economy> EconomyProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (EconomyProvider != null) {
			Eco = (Economy) EconomyProvider.getProvider();
		}
		return Eco != null;
	}

	public void reloadConfiguration() {
		PluginDescriptionFile pdFile = this.getDescription();
		File config = new File("plugins/" + pdFile.getName() + "/config.yml");
		if (config.exists()) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
			this.saveDefaultConfig();
			for (String key : cfg.getConfigurationSection("").getKeys(true)) {
				if (!this.getConfig().contains(key)) {
					this.getConfig().set(key, cfg.get(key));
				}
			}
		} else {
			this.saveDefaultConfig();
		}
		this.reloadConfig();

		this.bannedItem = this.getConfig().getStringList("BannedItem");
		
		

		this.price = this.getConfig().getInt("price");

		if (!getConfig().getString("BannedMsg").isEmpty()) {

			this.BannedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("BannedMsg"));

		}

		if (!getConfig().getString("PermDeny").isEmpty()) {

			this.PermDeny = ChatColor.translateAlternateColorCodes('&', getConfig().getString("PermDeny"));

		}

		if (!getConfig().getString("NoMoney").isEmpty()) {

			this.NoMoney = ChatColor.translateAlternateColorCodes('&', getConfig().getString("NoMoney"));

		}
		if (!getConfig().getString("NoItem").isEmpty()) {

			this.NoItem = ChatColor.translateAlternateColorCodes('&', getConfig().getString("NoItem"));

		}

		if (!getConfig().getString("NoColorCode").isEmpty()) {

			this.NoColorCode = ChatColor.translateAlternateColorCodes('&', getConfig().getString("NoColorCode"));

		}

		if (!getConfig().getString("Done").isEmpty()) {

			this.Done = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Done"));

		}

	}

	public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
			final String[] args) {

		if (commandLabel.equalsIgnoreCase("아이템이름변경") | commandLabel.equalsIgnoreCase("이름변경")) {
			if (sender instanceof Player) {
				if (args.length > 0) {

					StringBuilder str = new StringBuilder();
					for (int i = 0; i < args.length; i++) {
						str.append(args[i] + " ");
					}
					if (!(str.toString().contains("&"))) {
						Player player = (Player) sender;


							ItemStack item = player.getInventory().getItemInHand();
							ItemMeta itemMeta = item.getItemMeta();
							String name = str.toString();
							if (!((item == null) || (item.getType() == Material.AIR))) {
								if (!(bannedItem.contains(item.getType().toString()))) {
									int amount = item.getAmount();
									double total = price * amount;
									if ((Eco.getBalance(player) >= Double.valueOf(total).doubleValue())) {
									itemMeta.setDisplayName(name);
									item.setItemMeta(itemMeta);
									Eco.withdrawPlayer(player, Double.valueOf(total).doubleValue());
									sender.sendMessage(this.Done.replaceAll("%price%",total+""));

								} else {
									sender.sendMessage(this.NoMoney.replaceAll("%price%", total - Eco.getBalance(player)+"") );
								}
							} else {
								sender.sendMessage(BannedMsg);
						
							}
						} else {
							sender.sendMessage(this.NoItem);

						}
					} else {
						sender.sendMessage(this.NoColorCode);

					}

				} else {
					sender.sendMessage("§eServer §7>>§f /아이템이름변경 <아이템 이름 문자열> : 아이템 한개당 " + price + "원을 소비 하여 아이템 이름을 바꿉니다. ");
					sender.sendMessage("§eServer §7>>§f /이름변경 <아이템 이름 문자열> : 아이템 한개당 " + price + "원을 소비 하여 아이템 이름을 바꿉니다. ");

				}

			}else {

			sender.sendMessage("콘솔에서는 사용이 불가능한 명령어 입니다");

			}
		} else {

			if (commandLabel.equalsIgnoreCase("아이템이름변경리로드") || commandLabel.equalsIgnoreCase("이름변경리로드")) {
				if (sender.hasPermission("UserItemName.reload") || sender.isOp()) {

					this.reloadConfiguration();
					sender.sendMessage("§8[ §b 아이템 이름 변경 §8]§a리로드가 완료되었습니다. ");

				} else {
					sender.sendMessage(this.PermDeny);
				}

			}

		}

		return false;
	}

}
