package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import net.ess3.utils.FormatUtil;


public class Commandme extends EssentialsCommand
{
	@Override
	public void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		if (user.getData().isMuted())
		{
			throw new Exception(_("§6Your voice has been silenced!"));
		}

		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}

		String message = getFinalArg(args, 0);
		if (Permissions.CHAT_COLOR.isAuthorized(user))
		{
			message = FormatUtil.replaceFormat(message);
		}
		else
		{
			message = FormatUtil.stripColor(message);
		}

		ess.broadcastMessage(user, _("{0}", user.getPlayer().getDisplayName(), message));
	}
}
