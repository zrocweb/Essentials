package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.economy.Trade;


public class Commandback extends EssentialsCommand
{
	@Override
	protected void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		final Trade charge = new Trade(commandName, ess);
		charge.isAffordableFor(user);
		user.sendMessage(_("§6Returning to previous location."));
		user.getTeleport().back(charge);
		throw new NoChargeException();
	}
}
