package net.ess3.commands;

import java.util.Locale;
import java.util.regex.Pattern;
import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import org.bukkit.command.CommandSender;


public class Commanddelhome extends EssentialsCommand
{
	private final Pattern colon = Pattern.compile(":");

	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}

		IUser user = sender instanceof IUser ? (IUser)sender : null;
		String name;
		String[] expandedArg;

		//Allowing both formats /sethome khobbits house | /sethome khobbits:house
		final String[] nameParts = colon.split(args[0]);
		if (nameParts[0].length() != args[0].length())
		{
			expandedArg = nameParts;
		}
		else
		{
			expandedArg = args;
		}

		if (expandedArg.length > 1 && (user == null || Permissions.DELHOME_OTHERS.isAuthorized(user)))
		{
			user = ess.getUserMap().matchUser(expandedArg[1], true);
			name = expandedArg[1];
		}
		else if (user == null)
		{
			throw new NotEnoughArgumentsException();
		}
		else
		{
			name = expandedArg[0];
		}
		if (name.equalsIgnoreCase("bed"))
		{
			throw new Exception(_("vanillaHome"));
		}
		user.getData().removeHome(name.toLowerCase(Locale.ENGLISH));
		user.queueSave();
		sender.sendMessage(_("§6Home§c {0} §6has been removed.", name));
	}
}
