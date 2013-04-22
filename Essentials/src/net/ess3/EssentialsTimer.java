package net.ess3;

import java.util.*;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import net.ess3.api.ISettings;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import net.ess3.user.UserData.TimestampType;
import org.bukkit.entity.Player;


public class EssentialsTimer implements Runnable
{
	private final IEssentials ess;
	private final Set<IUser> onlineUsers = new HashSet<IUser>();
	private long lastPoll = System.currentTimeMillis();
	private final LinkedList<Float> history = new LinkedList<Float>();

	EssentialsTimer(final IEssentials ess)
	{
		this.ess = ess;
	}

	@Override
	public void run()
	{
		final long currentTime = System.currentTimeMillis();
		long timeSpent = (currentTime - lastPoll) / 1000;
		if (timeSpent == 0)
		{
			timeSpent = 1;
		}
		if (history.size() > 10)
		{
			history.remove();
		}
		final float tps = 100f / timeSpent;
		if (tps <= 20)
		{
			history.add(tps);
		}
		lastPoll = currentTime;
		for (Player player : ess.getServer().getOnlinePlayers())
		{

			try
			{
				final IUser user = ess.getUserMap().getUser(player);
				if (user == null)
				{
					continue;
				}
				onlineUsers.add(user);
				user.setLastOnlineActivity(currentTime);
				user.checkActivity();

				ISettings settings = ess.getSettings();

				boolean mailDisabled = settings.getData().getCommands().isDisabled("mail");

				// New mail notification
				if (!mailDisabled && Permissions.MAIL.isAuthorized(user) && !user.gotMailInfo())
				{
					final List<String> mail = user.getMails();
					if (mail != null && !mail.isEmpty())
					{
						user.sendMessage(I18n._("§6You have§c {0} §6messages! Type §c/mail read§6 to view your mail.", mail.size()));
					}
				}
			}
			catch (Exception e)
			{
				ess.getLogger().log(Level.WARNING, "EssentialsTimer Error:", e);
			}
		}

		final Iterator<IUser> iterator = onlineUsers.iterator();
		while (iterator.hasNext())
		{
			final IUser user = iterator.next();
			if (user.getLastOnlineActivity() < currentTime && user.getLastOnlineActivity() > user.getTimestamp(TimestampType.LOGOUT))
			{
				user.setTimestamp(TimestampType.LOGOUT, user.getLastOnlineActivity());
				iterator.remove();
				continue;
			}
			user.checkMuteTimeout(currentTime);
			user.checkJailTimeout(currentTime);
			user.resetInvulnerabilityAfterTeleport();
		}
	}

	public float getAverageTPS()
	{
		float avg = 0;
		for (Float f : history)
		{
			if (f != null)
			{
				avg += f;
			}
		}
		return avg / history.size();
	}
}
