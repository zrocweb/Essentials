package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.utils.FormatUtil;
import org.bukkit.command.CommandSender;


public class Commandbroadcast extends EssentialsCommand
{
	@Override
	public void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}
		ess.broadcastMessage(null, _("§r§6[§4Broadcast§6]§a {0}", FormatUtil.replaceFormat(getFinalArg(args, 0))));
	}
}
