package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.ChargeException;
import net.ess3.api.ISettings;
import net.ess3.api.IUser;
import net.ess3.economy.Trade;
import net.ess3.permissions.Permissions;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;


public class Commandtpaccept extends EssentialsCommand
{
	@Override
	public void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		if (user.getTeleportRequester() == null)
		{
			throw new Exception(_("§4You do not have a pending request."));
		}

		final IUser target = user.getTeleportRequester();
		if (target == null || !target.isOnline() || (user.isTpRequestHere() && !Permissions.TPAHERE.isAuthorized(
													 target)) || (!user.isTpRequestHere() && !Permissions.TPA.isAuthorized(target) && !Permissions.TPAALL.isAuthorized(target)))
		{
			throw new Exception(_("§4You do not have a pending request."));
		}

		if (args.length > 0 && !target.getName().contains(args[0]))
		{
			throw new Exception(_("§4You do not have a pending request."));
		}


		ISettings settings = ess.getSettings();
		int tpaAcceptCancellation = settings.getData().getCommands().getTeleport().getRequestTimeout();

		if (tpaAcceptCancellation != 0 && (System.currentTimeMillis() - user.getTeleportRequestTime()) / 1000 > tpaAcceptCancellation)
		{
			user.requestTeleport(null, false);
			throw new Exception(_("§4Teleport request has timed out."));
		}

		final Trade charge = new Trade(commandName, ess);
		user.sendMessage(_("§6Teleport request accepted."));
		target.sendMessage(_("§c{0} §6accepted your teleport request.", user.getPlayer().getDisplayName()));

		try
		{
			if (user.isTpRequestHere())
			{
				target.getTeleport().teleportToMe(user, charge, TeleportCause.COMMAND);
			}
			else
			{
				target.getTeleport().teleport(user.getPlayer(), charge, TeleportCause.COMMAND);
			}
		}
		catch (ChargeException ex)
		{
			user.sendMessage(_("§4Pending teleportation request cancelled."));
			//ess.showError(target, ex, commandLabel); TODO: equivalent to ess.showError() could not be found?
		}
		user.requestTeleport(null, false);
		throw new NoChargeException();
	}
}
