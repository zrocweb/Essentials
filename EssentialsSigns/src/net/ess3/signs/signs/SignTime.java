package net.ess3.signs.signs;

import static net.ess3.I18n._;
import net.ess3.api.ChargeException;
import net.ess3.api.IEssentials;
import net.ess3.api.IUser;
import net.ess3.economy.Trade;
import net.ess3.signs.EssentialsSign;
import org.bukkit.World;


public class SignTime extends EssentialsSign
{
	public SignTime()
	{
		super("Time");
	}

	@Override
	protected boolean onSignCreate(final ISign sign, final IUser player, final String username, final IEssentials ess) throws SignException
	{
		validateTrade(sign, 2, ess);
		final String timeString = sign.getLine(1);
		if ("Day".equalsIgnoreCase(timeString))
		{
			sign.setLine(1, "§2Day");
			return true;
		}
		if ("Night".equalsIgnoreCase(timeString))
		{
			sign.setLine(1, "§2Night");
			return true;
		}
		throw new SignException(_("/time only supports day/night."));
	}

	@Override
	protected boolean onSignInteract(final ISign sign, final IUser player, final String username, final IEssentials ess) throws SignException, ChargeException
	{
		final Trade charge = getTrade(sign, 2, ess);
		charge.isAffordableFor(player);
		final String timeString = sign.getLine(1);
		final World world = player.getPlayer().getWorld();
		long time = world.getTime();
		time -= time % 24000;
		if ("§2Day".equalsIgnoreCase(timeString))
		{
			world.setTime(time + 24000);
			charge.charge(player);
			return true;
		}
		if ("§2Night".equalsIgnoreCase(timeString))
		{
			world.setTime(time + 37700);
			charge.charge(player);
			return true;
		}
		throw new SignException(_("/time only supports day/night."));
	}
}
