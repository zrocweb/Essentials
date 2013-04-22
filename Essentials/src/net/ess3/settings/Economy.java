package net.ess3.settings;

import java.util.Map;
import lombok.*;
import net.ess3.economy.Worth;
import net.ess3.storage.Comment;
import net.ess3.storage.MapValueType;
import net.ess3.storage.StorageObject;


@Data
@EqualsAndHashCode(callSuper = false)
public class Economy implements StorageObject
{
	@Comment("Defines the balance with which new players begin. Defaults to 0.")
	private double startingBalance = 0.0;
	@MapValueType(Double.class)
	@Comment("Defines the cost to use the given commands PER USE")
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Map<String, Double> commandCosts = null;
	@Comment("Set this to a currency symbol you want to use.")
	private String currencySymbol = "$";

	public String getCurrencySymbol()
	{
		return currencySymbol == null || currencySymbol.isEmpty() ? "$" : currencySymbol.substring(0, 1);
	}
	private final static double MAXMONEY = 10000000000000.0;
	@Comment(
			"Set the maximum amount of money a player can have\n"
			 + "The amount is always limited to 10 trillions because of the limitations of a java double")
	private double maxMoney = MAXMONEY;

	public double getMaxMoney()
	{
		return Math.abs(maxMoney) > MAXMONEY ? MAXMONEY : Math.abs(maxMoney);
	}
	@Comment(
			"Set the minimum amount of money a player can have (must be above the negative of max-money).\n"
			 + "Setting this to 0, will disable overdrafts/loans completely.  Users need 'essentials.eco.loan' perm to go below 0.")
	private double minMoney = -MAXMONEY;

	public double getMinMoney()
	{
		return Math.abs(minMoney) > MAXMONEY ? -MAXMONEY : minMoney;
	}
	@Comment("Enable this to log all interactions with trade/buy/sell signs and sell command")
	private boolean logEnabled = false;
	private Worth worth = new Worth();
	private boolean tradeInStacks = false;

	public double getCommandCost(String command)
	{
		if (commandCosts == null)
		{
			return 0;
		}
		Double price = commandCosts.get(command);
		return price == null || Double.isNaN(price) || Double.isInfinite(price) ? 0 : price;
	}
}
