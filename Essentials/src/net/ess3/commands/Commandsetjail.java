package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.utils.Util;


public class Commandsetjail extends EssentialsCommand
{
	@Override
	public void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}
		ess.getJails().setJail(args[0], user.getPlayer().getLocation());
		user.sendMessage(_("§6Jail§c {0} §6has been set.", Util.sanitizeString(args[0])));

	}
}
