package net.ess3.economy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.ess3.I18n._;
import net.ess3.api.ChargeException;
import net.ess3.api.IEssentials;
import net.ess3.api.ISettings;
import net.ess3.api.IUser;
import net.ess3.craftbukkit.InventoryWorkaround;
import net.ess3.permissions.Permissions;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;


public class Trade
{
	private final String command;
	private final String fallbackCommand;
	private final Double money;
	private final ItemStack itemStack;
	private final Integer exp;
	private final IEssentials ess;

	public Trade(final String command, final IEssentials ess)
	{
		this(command, null, null, null, null, ess);
	}

	public Trade(final String command, final String fallback, final IEssentials ess)
	{
		this(command, fallback, null, null, null, ess);
	}

	public Trade(final double money, final IEssentials ess)
	{
		this(null, null, money, null, null, ess);
	}

	public Trade(final ItemStack items, final IEssentials ess)
	{
		this(null, null, null, items, null, ess);
	}

	public Trade(final int exp, final IEssentials ess)
	{
		this(null, null, null, null, exp, ess);
	}

	private Trade(final String command, final String fallback, final Double money, final ItemStack item, final Integer exp, final IEssentials ess)
	{
		this.command = command;
		this.fallbackCommand = fallback;
		this.money = money;
		this.itemStack = item;
		this.exp = exp;
		this.ess = ess;
	}

	public void isAffordableFor(final IUser user) throws ChargeException
	{
		if (getMoney() != null && getMoney() > 0 && !Permissions.ECO_LOAN.isAuthorized(user) && !user.canAfford(getMoney()))
		{
			throw new ChargeException(_("§4You do not have sufficient funds."));
		}

		if (getItemStack() != null && !user.getPlayer().getInventory().containsAtLeast(itemStack, itemStack.getAmount()))
		{
			throw new ChargeException(
					_("§4You do not have {0}x {1}.", getItemStack().getAmount(), getItemStack().getType().toString().toLowerCase(Locale.ENGLISH).replace("_", " ")));
		}


		if (command != null && !command.isEmpty() && 0 < getCommandCost(user) && !Permissions.ECO_LOAN.isAuthorized(user))
		{
			throw new ChargeException(_("§4You do not have sufficient funds."));
		}

		if (exp != null && exp > 0 && user.getPlayer().getTotalExperience() < exp)
		{
			throw new ChargeException(_("§4You do not have enough experience."));
		}
	}

	public void pay(final IUser user)
	{
		pay(user, true);
	}

	public boolean pay(final IUser user, final boolean dropItems)
	{
		boolean success = true;
		if (getMoney() != null && getMoney() > 0)
		{
			user.giveMoney(getMoney());
		}
		if (getItemStack() != null)
		{
			if (dropItems)
			{
				final Map<Integer, ItemStack> leftOver = InventoryWorkaround.addItems(user.getPlayer().getInventory(), getItemStack());
				final Location loc = user.getPlayer().getLocation();
				for (ItemStack dropStack : leftOver.values())
				{
					final int maxStackSize = dropStack.getType().getMaxStackSize();
					final int stacks = dropStack.getAmount() / maxStackSize;
					final int leftover = dropStack.getAmount() % maxStackSize;
					final ItemStack[] itemStacks = new ItemStack[stacks + (leftover > 0 ? 1 : 0)]; // TODO: Written, but never read?
					for (int i = 0; i < stacks; i++)
					{
						final ItemStack stack = dropStack.clone();
						stack.setAmount(maxStackSize);
						itemStacks[i] = user.getPlayer().getWorld().dropItem(loc, stack).getItemStack();
					}
					if (leftover > 0)
					{
						final ItemStack stack = dropStack.clone();
						stack.setAmount(leftover);
						itemStacks[stacks] = user.getPlayer().getWorld().dropItem(loc, stack).getItemStack();
					}
				}
			}
			else
			{
				success = InventoryWorkaround.addAllItems(user.getPlayer().getInventory(), getItemStack());
			}
			user.getPlayer().updateInventory();
		}
		if (getExperience() != null)
		{
			user.getPlayer().setTotalExperience(user.getPlayer().getTotalExperience() + getExperience());
		}
		return success;
	}

	public void charge(final IUser user) throws ChargeException
	{
		if (getMoney() != null)
		{
			if (!user.canAfford(getMoney()) && getMoney() > 0)
			{
				throw new ChargeException(_("§4You do not have sufficient funds."));
			}
			user.takeMoney(getMoney());
		}
		if (getItemStack() != null)
		{
			if (!user.getPlayer().getInventory().containsAtLeast(itemStack, itemStack.getAmount()))
			{
				throw new ChargeException(
						_("§4You do not have {0}x {1}.", getItemStack().getAmount(), getItemStack().getType().toString().toLowerCase(Locale.ENGLISH).replace("_", " ")));
			}
			user.getPlayer().getInventory().removeItem(getItemStack());
			user.getPlayer().updateInventory();
		}
		if (command != null && !command.isEmpty() && !Permissions.NOCOMMANDCOST.isAuthorized(user, command))
		{

			final ISettings settings = ess.getSettings();
			final double cost = settings.getData().getEconomy().getCommandCost(command.charAt(0) == '/' ? command.substring(1) : command);
			if (!user.canAfford(cost) && cost > 0)
			{
				throw new ChargeException(_("§4You do not have sufficient funds."));
			}
			user.takeMoney(cost);
		}
		if (getExperience() != null)
		{
			final int experience = user.getPlayer().getTotalExperience();
			if (experience < getExperience() && getExperience() > 0)
			{
				throw new ChargeException(_("§4You do not have enough experience."));
			}
			user.getPlayer().setTotalExperience(experience - getExperience());
		}
	}

	public Double getMoney()
	{
		return money;
	}

	public ItemStack getItemStack()
	{
		return itemStack;
	}

	public Integer getExperience()
	{
		return exp;
	}

	public Double getCommandCost(final IUser user)
	{
		double cost = 0d;
		if (command != null && !command.isEmpty() && !Permissions.NOCOMMANDCOST.isAuthorized(user, command))
		{
			cost = ess.getSettings().getData().getEconomy().getCommandCost(command.charAt(0) == '/' ? command.substring(1) : command);
			if (cost == 0.0 && fallbackCommand != null && !fallbackCommand.isEmpty())
			{
				cost = ess.getSettings().getData().getEconomy().getCommandCost(
						fallbackCommand.charAt(0) == '/' ? fallbackCommand.substring(1) : fallbackCommand);
			}
		}
		return cost;
	}
	private static FileWriter fw = null;

	public static void log(String type, String subtype, String event, String sender, Trade charge, String receiver, Trade pay, Location loc, IEssentials ess)
	{
		final ISettings settings = ess.getSettings();
		if (!settings.getData().getEconomy().isLogEnabled())
		{
			return;
		}
		if (fw == null)
		{
			try
			{
				fw = new FileWriter(new File(ess.getPlugin().getDataFolder(), "trade.log"), true);
			}
			catch (IOException ex)
			{
				Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			}
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(type).append(",").append(subtype).append(",").append(event).append(",\"");
		sb.append(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date()));
		sb.append("\",\"");
		if (sender != null)
		{
			sb.append(sender);
		}
		sb.append("\",");
		if (charge == null)
		{
			sb.append("\"\",\"\",\"\"");
		}
		else
		{
			if (charge.getItemStack() != null)
			{
				sb.append(charge.getItemStack().getAmount()).append(",");
				sb.append(charge.getItemStack().getType().toString()).append(",");
				sb.append(charge.getItemStack().getDurability());
			}
			if (charge.getMoney() != null)
			{
				sb.append(charge.getMoney()).append(",");
				sb.append("money").append(",");
				sb.append(settings.getData().getEconomy().getCurrencySymbol());
			}
			if (charge.getExperience() != null)
			{
				sb.append(charge.getExperience()).append(",");
				sb.append("exp").append(",");
				sb.append("\"\"");
			}
		}
		sb.append(",\"");
		if (receiver != null)
		{
			sb.append(receiver);
		}
		sb.append("\",");
		if (pay == null)
		{
			sb.append("\"\",\"\",\"\"");
		}
		else
		{
			if (pay.getItemStack() != null)
			{
				final ItemStack is = pay.getItemStack();
				sb.append(is.getAmount()).append(",");
				sb.append(is.getType().toString()).append(",");
				sb.append(is.getDurability());
			}
			if (pay.getMoney() != null)
			{
				sb.append(pay.getMoney()).append(",");
				sb.append("money").append(",");
				sb.append(settings.getData().getEconomy().getCurrencySymbol());
			}
			if (pay.getExperience() != null)
			{
				sb.append(pay.getExperience()).append(",");
				sb.append("exp").append(",");
				sb.append("\"\"");
			}
		}
		if (loc == null)
		{
			sb.append(",\"\",\"\",\"\",\"\"");
		}
		else
		{
			sb.append(",\"");
			sb.append(loc.getWorld().getName()).append("\",");
			sb.append(loc.getBlockX()).append(",");
			sb.append(loc.getBlockY()).append(",");
			sb.append(loc.getBlockZ()).append(",");
		}
		sb.append("\n");
		try
		{
			fw.write(sb.toString());
			fw.flush();
		}
		catch (IOException ex)
		{
			Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
		}
	}

	public static void closeLog()
	{
		if (fw != null)
		{
			try
			{
				fw.close();
			}
			catch (IOException ex)
			{
				Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			}
			fw = null;
		}
	}
}
