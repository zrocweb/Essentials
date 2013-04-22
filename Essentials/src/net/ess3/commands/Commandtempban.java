package net.ess3.commands;

import net.ess3.Console;
import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import net.ess3.user.Ban;
import net.ess3.user.UserData;
import net.ess3.utils.DateUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;


public class Commandtempban extends EssentialsCommand
{
	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 2)
		{
			throw new NotEnoughArgumentsException();
		}
		final IUser user = ess.getUserMap().matchUser(args[0], true);
		if (!user.isOnline())
		{
			if (Permissions.TEMPBAN_OFFLINE.isAuthorized(sender))
			{
				sender.sendMessage(_("§4You may not tempban that player."));
				return;
			}
		}
		else
		{
			if (Permissions.TEMPBAN_EXEMPT.isAuthorized(user) && sender instanceof Player)
			{
				sender.sendMessage(_("§4You may not tempban that player."));
				return;
			}
		}
		final String time = getFinalArg(args, 1);
		final long banTimestamp = DateUtil.parseDateDiff(time, true);

		final long max = ess.getSettings().getData().getCommands().getTempban().getMaxTempbanTime();
		if (max != -1 && banTimestamp - Calendar.getInstance().getTimeInMillis() > max && !Permissions.TEMPBAN_UNLIMITED.isAuthorized(sender))
		{
			sender.sendMessage(_("tempbanOversized"));
			return;
		}

		final String banReason = _("Temporarily banned from server for {0}.", DateUtil.formatDateDiff(banTimestamp));
		final Ban ban = new Ban();
		final UserData userData = user.getData();
		ban.setReason(banReason);
		ban.setTimeout(banTimestamp);
		userData.setBan(ban);
		user.setBanned(true);
		user.queueSave();
		user.getPlayer().kickPlayer(banReason);
		final String senderName = isUser(sender) ? getPlayer(sender).getDisplayName() : Console.NAME;

		for (Player onlinePlayer : server.getOnlinePlayers())
		{
			final IUser player = ess.getUserMap().getUser(onlinePlayer);
			if (Permissions.BAN_NOTIFY.isAuthorized(player))
			{
				onlinePlayer.sendMessage(_("§6Player§c {0} §6banned {1} §6for {2}.", senderName, user.getName(), banReason));
			}
		}
	}
}
