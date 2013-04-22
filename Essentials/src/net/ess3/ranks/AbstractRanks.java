package net.ess3.ranks;

import java.text.MessageFormat;
import net.ess3.api.IUser;
import net.ess3.utils.FormatUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public abstract class AbstractRanks
{
	protected Player getPlayer(CommandSender sender)
	{
		if (sender instanceof IUser)
		{
			return ((IUser)sender).getPlayer();
		}
		if (sender instanceof Player)
		{
			return (Player)sender;
		}
		throw new IllegalArgumentException();
	}

	//TODO: Reimplement caching
	public MessageFormat getChatFormat(final CommandSender player)
	{
		String format = getRawChatFormat(player);
		format = FormatUtil.replaceFormat(format);
		format = format.replace("{DISPLAYNAME}", "%1$s");
		format = format.replace("{GROUP}", "{0}");
		format = format.replace("{MESSAGE}", "%2$s");
		format = format.replace("{WORLDNAME}", "{1}");
		format = format.replace("{SHORTWORLDNAME}", "{2}");
		format = format.replaceAll("\\{(\\D*)\\}", "\\[$1\\]");
		return new MessageFormat(format);
	}

	protected abstract String getRawChatFormat(final CommandSender sender);
}
