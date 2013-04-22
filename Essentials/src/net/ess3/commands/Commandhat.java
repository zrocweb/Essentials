package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.craftbukkit.InventoryWorkaround;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class Commandhat extends EssentialsCommand
{
	@Override
	protected void run(IUser user, String commandLabel, String[] args) throws Exception
	{
		if (args.length > 0 && (args[0].contains("rem") || args[0].contains("off") || args[0].equalsIgnoreCase("0")))
		{
			final PlayerInventory inv = user.getPlayer().getInventory();
			final ItemStack head = inv.getHelmet();
			if (head == null || head.getType() == Material.AIR)
			{
				user.sendMessage(_("§4You are not wearing a hat."));
			}
			else
			{
				final ItemStack air = new ItemStack(Material.AIR);
				inv.setHelmet(air);
				InventoryWorkaround.addItems(user.getPlayer().getInventory(), head);
				user.sendMessage(_("§6Your hat has been removed."));
			}
		}
		else
		{
			final Player player = user.getPlayer();
			if (player.getItemInHand().getType() != Material.AIR)
			{
				final ItemStack hand = player.getItemInHand().clone();
				if (hand.getType().getMaxDurability() == 0)
				{
					final PlayerInventory inv = player.getInventory();
					final ItemStack head = inv.getHelmet();
					hand.setAmount(1);
					inv.remove(hand);
					inv.setHelmet(hand);
					inv.setItemInHand(head);
					user.sendMessage(_("§6Enjoy your new hat!"));
				}
				else
				{
					user.sendMessage(_("§4You cannot use this item as a hat!"));
				}
			}
			else
			{
				user.sendMessage(_("§4You must have something to wear in your hand."));
			}
		}
	}
}
