
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

	protected String BannedMsg = "��eServer ��7>> ��c�ش� �������� �̸������� �Ұ����մϴ�.";
	protected String PermDeny = "��eServer ��7>> ��c������ �����ϴ�.";
	protected String NoMoney = "��eServer ��7>> ��c����� ���� �����ϴ�. ������ ��: %price%��";
	protected String NoItem = "��eServer ��7>> ��c�տ� �������� ��� �־�� �մϴ�.";
	protected String NoColorCode = "��eServer ��7>> ��a\"&\"��c ���ڴ� ����� �Ұ����մϴ�.";
	protected String Done = "��eServer ��7>> ��f%price%���� ����� ������ �̸��� ���� �Ͽ����ϴ�.";

	public UserItemName() {
		this.logger = Logger.getLogger("Minecraft");
	}

	public void onDisable() {
		final PluginDescriptionFile pdFile = this.getDescription();
		System.out.println(String.valueOf(pdFile.getName()) + pdFile.getVersion() + "��(��) ��Ȱ��ȭ �Ǿ����ϴ�.");
	}

	public void onEnable() {
		final PluginDescriptionFile pdFile = this.getDescription();
		System.out.println(String.valueOf(pdFile.getName()) + pdFile.getVersion() + "��(��) Ȱ��ȭ �Ǿ����ϴ�.");
		this.reloadConfiguration();

		if (!SetupEconomy()) {
			Bukkit.getConsoleSender().sendMessage("��6��l[ Item Name ] ��c��lEconomy ��f�÷������� �νĵ��� �ʾ����Ƿ�, ��Ȱ��ȭ �˴ϴ�.");
			getServer().getPluginManager().disablePlugin(this);
			Bukkit.shutdown();
			return;
		}

		Bukkit.getConsoleSender().sendMessage("��6��l[ Item Name ] ��a��l" + Eco.getName() + " ��f�÷������� �ν� �Ǿ����ϴ�.");

	}

	private boolean SetupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getConsoleSender().sendMessage("��6��l[ Item Name ] ��c��lVault ��f�÷������� �νĵ��� �ʾ����Ƿ�, ������ ���� �˴ϴ�.");
			Bukkit.shutdown();
			return false;
		}

		Bukkit.getConsoleSender().sendMessage("��6��l[ Item Name ] ��a��lVault ��f�÷������� �ν� �Ǿ����ϴ�.");
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

		if (commandLabel.equalsIgnoreCase("�������̸�����") | commandLabel.equalsIgnoreCase("�̸�����")) {
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
					sender.sendMessage("��eServer ��7>>��f /�������̸����� <������ �̸� ���ڿ�> : ������ �Ѱ��� " + price + "���� �Һ� �Ͽ� ������ �̸��� �ٲߴϴ�. ");
					sender.sendMessage("��eServer ��7>>��f /�̸����� <������ �̸� ���ڿ�> : ������ �Ѱ��� " + price + "���� �Һ� �Ͽ� ������ �̸��� �ٲߴϴ�. ");

				}

			}else {

			sender.sendMessage("�ֿܼ����� ����� �Ұ����� ��ɾ� �Դϴ�");

			}
		} else {

			if (commandLabel.equalsIgnoreCase("�������̸����渮�ε�") || commandLabel.equalsIgnoreCase("�̸����渮�ε�")) {
				if (sender.hasPermission("UserItemName.reload") || sender.isOp()) {

					this.reloadConfiguration();
					sender.sendMessage("��8[ ��b ������ �̸� ���� ��8]��a���ε尡 �Ϸ�Ǿ����ϴ�. ");

				} else {
					sender.sendMessage(this.PermDeny);
				}

			}

		}

		return false;
	}

}
