package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import net.ess3.user.UserData.TimestampType;
import net.ess3.utils.DateUtil;
import org.bukkit.command.CommandSender;


public class Commandtogglejail extends EssentialsCommand
{
	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}

		final IUser player = ess.getUserMap().matchUser(args[0], true);

		if (args.length >= 2 && !player.getData().isJailed())
		{
			if (!player.isOnline())
			{
				if (Permissions.TOGGLEJAIL_OFFLINE.isAuthorized(sender))
				{
					sender.sendMessage(_("§4You may not jail that person!"));
					return;
				}
			}
			else
			{
				if (Permissions.JAIL_EXEMPT.isAuthorized(player))
				{
					sender.sendMessage(_("§4You may not jail that person!"));
					return;
				}
			}
			if (player.isOnline())
			{
				ess.getJails().sendToJail(player, args[1]);
			}
			else
			{
				// Check if jail exists
				ess.getJails().getJail(args[1]);
			}
			player.getData().setJailed(true);
			player.sendMessage(_("§6You have been jailed!"));
			player.getData().setJail(args[1]);
			long timeDiff = 0;
			if (args.length > 2)
			{
				final String time = getFinalArg(args, 2);
				timeDiff = DateUtil.parseDateDiff(time, true);
				player.setTimestamp(TimestampType.JAIL, timeDiff);
			}
			player.queueSave();
			sender.sendMessage(
					(timeDiff > 0 ? _("§6Player§c {0} §6jailed for {1}.", player.getName(), DateUtil.formatDateDiff(timeDiff)) : _("§6Player§c {0} §6jailed.", player.getName())));
			return;
		}

		if (args.length >= 2 && player.getData().isJailed() && !args[1].equalsIgnoreCase(player.getData().getJail()))
		{
			sender.sendMessage(_("§4Person is already in jail:§c {0}", player.getData().getJail()));
			return;
		}

		if (args.length >= 2 && player.getData().isJailed() && args[1].equalsIgnoreCase(player.getData().getJail()))
		{
			final String time = getFinalArg(args, 2);
			final long timeDiff = DateUtil.parseDateDiff(time, true);
			player.setTimestamp(TimestampType.JAIL, timeDiff);
			player.queueSave();
			sender.sendMessage(_("§6Jail time extend to: {0}", DateUtil.formatDateDiff(timeDiff)));
			return;
		}

		if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase(player.getData().getJail())))
		{
			if (!player.getData().isJailed())
			{
				throw new NotEnoughArgumentsException();
			}
			player.getData().setJailed(false);
			player.setTimestamp(TimestampType.JAIL, 0);
			player.sendMessage(_("§6You have been released!"));
			player.getData().setJail(null);
			if (player.isOnline())
			{
				player.getTeleport().back();
			}
			player.queueSave();
			sender.sendMessage(_("§6Player §c{0}§6 unjailed.", player.getName()));
		}
	}
}
