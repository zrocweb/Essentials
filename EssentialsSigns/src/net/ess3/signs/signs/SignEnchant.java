package net.ess3.signs.signs;

import java.util.Locale;
import static net.ess3.I18n._;
import net.ess3.api.ChargeException;
import net.ess3.api.IEssentials;
import net.ess3.api.IUser;
import net.ess3.bukkit.Enchantments;
import net.ess3.economy.Trade;
import net.ess3.signs.EssentialsSign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;


public class SignEnchant extends EssentialsSign
{
	public SignEnchant()
	{
		super("Enchant");
	}

	@Override
	protected boolean onSignCreate(final ISign sign, final IUser player, final String username, final IEssentials ess) throws SignException, ChargeException
	{
		final ItemStack stack = sign.getLine(1).equals("*") || sign.getLine(1).equalsIgnoreCase("any") ? null : getItemStack(sign.getLine(1), 1, ess);
		final String[] enchantLevel = sign.getLine(2).split(":");
		if (enchantLevel.length != 2)
		{
			throw new SignException(_("§4Line§c {0} §4on sign is invalid.", 3));
		}
		final Enchantment enchantment = Enchantments.getByName(enchantLevel[0]);
		if (enchantment == null)
		{
			throw new SignException(_("§4Enchantment not found!"));
		}
		int level;
		try
		{
			level = Integer.parseInt(enchantLevel[1]);
		}
		catch (NumberFormatException ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}
		if (level < 1 || level > enchantment.getMaxLevel())
		{
			level = enchantment.getMaxLevel();
			sign.setLine(2, enchantLevel[0] + ":" + level);
		}
		try
		{
			if (stack != null)
			{
				stack.addEnchantment(enchantment, level);
			}
		}
		catch (Throwable ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}
		getTrade(sign, 3, ess);
		return true;
	}

	@Override
	protected boolean onSignInteract(ISign sign, IUser player, String username, IEssentials ess) throws SignException, ChargeException
	{
		final ItemStack search = sign.getLine(1).equals("*") || sign.getLine(1).equalsIgnoreCase("any") ? null : getItemStack(sign.getLine(1), 1, ess);
		final Trade charge = getTrade(sign, 3, ess);
		charge.isAffordableFor(player);
		final String[] enchantLevel = sign.getLine(2).split(":");
		if (enchantLevel.length != 2)
		{
			throw new SignException(_("§4Line§c {0} §4on sign is invalid.", 3));
		}
		final Enchantment enchantment = Enchantments.getByName(enchantLevel[0]);
		if (enchantment == null)
		{
			throw new SignException(_("§4Enchantment not found!"));
		}
		int level;
		try
		{
			level = Integer.parseInt(enchantLevel[1]);
		}
		catch (NumberFormatException ex)
		{
			level = enchantment.getMaxLevel();
		}

		final ItemStack playerHand = player.getPlayer().getItemInHand();
		if (playerHand == null || playerHand.getAmount() != 1 || (playerHand.containsEnchantment(enchantment) && playerHand.getEnchantmentLevel(
																  enchantment) == level))
		{
			throw new SignException(_("§4You do not have {0}x {1}.", 1, sign.getLine(1)));
		}
		if (search != null && playerHand.getType() != search.getType())
		{
			throw new SignException(_("§4You do not have {0}x {1}.", 1, search.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' ')));
		}

		final ItemStack toEnchant = playerHand;
		try
		{
			toEnchant.addEnchantment(enchantment, level);
		}
		catch (Exception ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}

		charge.charge(player);
		Trade.log("Sign", "Enchant", "Interact", username, charge, username, charge, sign.getBlock().getLocation(), ess);
		player.getPlayer().updateInventory();
		return true;
	}
}
