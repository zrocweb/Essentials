package net.ess3.commands;

import static net.ess3.I18n._;
import org.bukkit.command.CommandSender;


public class Commanddeljail extends EssentialsCommand
{
	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}
		ess.getJails().removeJail(args[0]);
		sender.sendMessage(_("§6Jail§c {0} §6has been removed.", args[0]));
	}
}
