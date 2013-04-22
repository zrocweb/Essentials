package net.ess3.user;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;
import net.ess3.Console;
import static net.ess3.I18n._;
import net.ess3.Teleport;
import net.ess3.api.*;
import net.ess3.craftbukkit.InventoryWorkaround;
import net.ess3.economy.register.Method;
import net.ess3.economy.register.Methods;
import net.ess3.permissions.Permissions;
import net.ess3.utils.DateUtil;
import net.ess3.utils.FormatUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class User extends UserBase implements IUser
{
	private CommandSender replyTo = null;
	@Getter
	private IUser teleportRequester;
	@Getter
	private boolean tpRequestHere;
	@Getter
	private final ITeleport teleport;
	@Getter
	private long teleportRequestTime;
	@Getter
	@Setter
	private long lastOnlineActivity;
	private long lastActivity = System.currentTimeMillis();
	/*@Getter
	 @Setter
	 private boolean hidden = false;*/
	@Getter
	private boolean vanished;
	@Getter
	@Setter
	private boolean invSee = false;
	@Getter
	@Setter
	private boolean enderSee = false;
	private long lastThrottledAction;
	private Location afkPosition;
	private AtomicBoolean gotMailInfo = new AtomicBoolean(false);
	private WeakReference<Player> playerCache;
	@Getter
	@Setter
	private boolean recipeSee = false;

	public User(final OfflinePlayer base, final IEssentials ess) throws InvalidNameException
	{
		super(base, ess);
		teleport = new Teleport(this, ess);
	}

	@Override
	public void setPlayerCache(final Player player)
	{
		playerCache = new WeakReference<Player>(player);
	}

	@Override
	public Player getPlayer()
	{
		Player player = playerCache == null ? null : playerCache.get();
		if (player == null)
		{
			player = super.getPlayer();
		}
		return player;
	}

	@Override
	public void checkCooldown(final UserData.TimestampType cooldownType, final double cooldown, final boolean set, final IPermission bypassPermission) throws CooldownException
	{
		final Calendar now = new GregorianCalendar();
		if (getTimestamp(cooldownType) > 0)
		{
			final Calendar cooldownTime = new GregorianCalendar();
			cooldownTime.setTimeInMillis(getTimestamp(cooldownType));
			cooldownTime.add(Calendar.SECOND, (int)cooldown);
			cooldownTime.add(Calendar.MILLISECOND, (int)((cooldown * 1000.0) % 1000.0));
			if (cooldownTime.after(now) && !bypassPermission.isAuthorized(this))
			{
				throw new CooldownException(DateUtil.formatDateDiff(cooldownTime.getTimeInMillis()));
			}
		}
		if (set)
		{
			setTimestamp(cooldownType, now.getTimeInMillis());
		}
	}

	@Override
	public void giveMoney(final double value)
	{
		giveMoney(value, null);
	}

	@Override
	public void giveMoney(final double value, final CommandSender initiator)
	{

		if (value == 0)
		{
			return;
		}
		setMoney(getMoney() + value);
		sendMessage(_("§a{0} has been added to your account.", FormatUtil.displayCurrency(value, ess)));
		if (initiator != null)
		{
			initiator.sendMessage(_("§a{0} added to {1}§a account. New balance: {2}", FormatUtil.displayCurrency(value, ess), this.getPlayer().getDisplayName()));
		}
		queueSave();
	}

	@Override
	public void payUser(final IUser receiver, final double value) throws Exception
	{
		if (value == 0)
		{
			return;
		}
		if (canAfford(value))
		{
			setMoney(getMoney() - value);
			receiver.setMoney(receiver.getMoney() + value);
			sendMessage(_("§a{0} has been sent to {1}.", FormatUtil.displayCurrency(value, ess), receiver.getPlayer().getDisplayName()));
			receiver.sendMessage(_("§a{0} has been received from {1}.", FormatUtil.displayCurrency(value, ess), getPlayer().getDisplayName()));
		}
		else
		{
			throw new Exception(_("§4You do not have sufficient funds."));
		}
	}

	@Override
	public void takeMoney(final double value)
	{
		takeMoney(value, null);
	}

	@Override
	public void takeMoney(final double value, final CommandSender initiator)
	{
		if (value == 0)
		{
			return;
		}
		setMoney(getMoney() - value);
		sendMessage(_("§a{0} has been taken from your account.", FormatUtil.displayCurrency(value, ess)));
		if (initiator != null)
		{
			initiator.sendMessage(_("§a{0} taken from {1}§a account. New balance: {2}.", FormatUtil.displayCurrency(value, ess), this.getPlayer().getDisplayName()));
		}
	}

	public void setHome()
	{
		setHome("home", getPlayer().getLocation());
	}

	public void setHome(final String name)
	{
		setHome(name, getPlayer().getLocation());
	}

	@Override
	public void setLastLocation()
	{
		getData().setLastLocation(new net.ess3.storage.StoredLocation(getPlayer().getLocation()));
		queueSave();
	}

	public String getNick(boolean addprefixsuffix)
	{

		final String nick = getData().getNickname();
		final ISettings settings = ess.getSettings();
		final IRanks groups = ess.getRanks();
		// default: {PREFIX}{NICKNAMEPREFIX}{NAME}{SUFFIX}
		String displayname = settings.getData().getChat().getDisplaynameFormat();
		if (settings.getData().getCommands().isDisabled("nick") || nick == null || nick.isEmpty() || nick.equals(getName()))
		{
			displayname = displayname.replace("{NAME}", getName());
			displayname = displayname.replace("{NICKNAMEPREFIX}", "");
		}
		else
		{
			displayname = displayname.replace("{NAME}", nick);
			displayname = displayname.replace("{NICKNAMEPREFIX}", settings.getData().getChat().getNicknamePrefix());
		}

		if (displayname.contains("{PREFIX}"))
		{
			displayname = displayname.replace("{PREFIX}", addprefixsuffix ? groups.getPrefix(this) : "");
		}
		if (displayname.contains("{SUFFIX}"))
		{
			displayname = displayname.replace("{SUFFIX}", addprefixsuffix ? groups.getSuffix(this) : "");
		}
		displayname = displayname.replace("{WORLDNAME}", this.getPlayer().getWorld().getName());
		displayname = displayname.replace('&', '\u00a7');
		displayname = displayname.concat("\u00a7r");

		return displayname;
	}

	@Override
	public void setDisplayNick()
	{
		String name = getNick(true);
		getPlayer().setDisplayName(name);
		if (name.length() > 16)
		{
			name = getNick(false);
		}
		if (name.length() > 16)
		{
			name = name.substring(0, name.charAt(15) == '\u00a7' ? 15 : 16);
		}
		try
		{
			getPlayer().setPlayerListName(name);
		}
		catch (IllegalArgumentException e)
		{
			ess.getLogger().info("Playerlist for " + name + " was not updated. Use a shorter displayname prefix."); //TODO: TL key?
		}
	}

	@Override
	public void updateDisplayName()
	{
		final Boolean changeDisplayname = ess.getSettings().getData().getChat().getChangeDisplayname();

		if (isOnline() && ((changeDisplayname == null && ess.getPlugin().isModuleEnabled("Chat")) || changeDisplayname == true))
		{
			setDisplayNick();
		}
	}

	@Override
	public double getMoney()
	{
		if (Methods.hasMethod())
		{
			try
			{
				final Method method = Methods.getMethod();
				if (!method.hasAccount(this.getName()))
				{
					throw new Exception();
				}
				final Method.MethodAccount account = Methods.getMethod().getAccount(this.getName());
				return account.balance();
			}
			catch (Exception ex)
			{
			}
		}
		return super.getMoney();
	}

	@Override
	public void setMoney(final double value)
	{
		if (Methods.hasMethod())
		{
			try
			{
				final Method method = Methods.getMethod();
				if (!method.hasAccount(this.getName()))
				{
					throw new Exception();
				}
				final Method.MethodAccount account = Methods.getMethod().getAccount(this.getName());
				account.set(value);
			}
			catch (Exception ex)
			{
			}
		}
		super.setMoney(value);
	}

	public void setAfk(final boolean set)
	{

		this.getPlayer().setSleepingIgnored(Permissions.SLEEPINGIGNORED.isAuthorized(this) ? true : set);
		if (set && !getData().isAfk())
		{
			afkPosition = getPlayer().getLocation();
		}
		getData().setAfk(set);
		queueSave();
	}

	@Override
	public boolean toggleAfk()
	{
		final boolean now = super.toggleAfk();
		this.getPlayer().setSleepingIgnored(Permissions.SLEEPINGIGNORED.isAuthorized(this) ? true : now);
		return now;
	}

	//Returns true if status expired during this check
	@Override
	public boolean checkJailTimeout(final long currentTime)
	{
		if (getTimestamp(UserData.TimestampType.JAIL) > 0 && getTimestamp(UserData.TimestampType.JAIL) < currentTime && getData().isJailed())
		{


			setTimestamp(UserData.TimestampType.JAIL, 0);
			getData().setJailed(false);
			sendMessage(_("§6You have been released."));
			getData().setJail(null);
			queueSave();

			try
			{
				teleport.back();
			}
			catch (Exception ex)
			{
			}
			return true;
		}
		return false;
	}

	//Returns true if status expired during this check
	@Override
	public boolean checkMuteTimeout(final long currentTime)
	{
		if (getTimestamp(UserData.TimestampType.MUTE) > 0 && getTimestamp(UserData.TimestampType.MUTE) < currentTime && getData().isMuted())
		{
			setTimestamp(UserData.TimestampType.MUTE, 0);
			sendMessage(_("§6You can now talk again."));
			getData().setMuted(false);
			queueSave();
			return true;
		}
		return false;
	}

	//Returns true if status expired during this check
	@Override
	public boolean checkBanTimeout(final long currentTime)
	{
		if (getData().getBan() != null && getData().getBan().getTimeout() > 0 && getData().getBan().getTimeout() < currentTime && isBanned())
		{
			getData().setBan(null);
			setBanned(false);
			queueSave();
			return true;
		}
		return false;
	}

	@Override
	public void updateActivity(final boolean broadcast)
	{
		if (getData().isAfk())
		{
			getData().setAfk(false);
			queueSave();
			if (broadcast)
			{
				ess.broadcastMessage(this, _("§5{0} §5is no longer AFK.", getPlayer().getDisplayName()));
			}
		}
		lastActivity = System.currentTimeMillis();
	}

	@Override
	public void checkActivity()
	{
		final ISettings settings = ess.getSettings();
		final long autoafkkick = settings.getData().getCommands().getAfk().getAutoAFKKick();
		if (autoafkkick > 0 && lastActivity > 0 && (lastActivity + (autoafkkick * 1000)) < System.currentTimeMillis()
			//&& !hidden
			&& !Permissions.KICK_EXEMPT.isAuthorized(this) && !Permissions.AFK_KICKEXEMPT.isAuthorized(this))
		{
			final String kickReason = _("You have been kicked for idling more than {0} minutes.", autoafkkick / 60.0);
			lastActivity = 0;
			getPlayer().kickPlayer(kickReason);


			for (Player player : ess.getServer().getOnlinePlayers())
			{
				if (Permissions.KICK_NOTIFY.isAuthorized(player))
				{
					player.sendMessage(_("§6Player§c {0} §6kicked {1} for {2}.", Console.NAME, getName(), kickReason));
				}
			}
		}
		final long autoafk = settings.getData().getCommands().getAfk().getAutoAFK();

		if (!getData().isAfk() && autoafk > 0 && lastActivity + autoafk * 1000 < System.currentTimeMillis() && Permissions.AFK_AUTO.isAuthorized(this))
		{
			setAfk(true);
			ess.broadcastMessage(this, _("§5{0} §5is now AFK.", getPlayer().getDisplayName()));
		}
	}

	@Override
	public Location getAfkPosition()
	{
		return afkPosition;
	}

	@Override
	public boolean isGodModeEnabled()
	{
		final ISettings settings = ess.getSettings();
		return (getData().isGodmode() && !settings.getData().getWorldOptions(
				getPlayer().getLocation().getWorld().getName()).isGodmode()) || (getData().isAfk() && settings.getData().getCommands().getAfk().isFreezeAFKPlayers());
	}

	@Override
	public void setGodModeEnabled(boolean set)
	{
		getData().setGodmode(set);
	}

	@Override
	public void updateCompass()
	{
		try
		{
			Location loc = getHome(getPlayer().getLocation());
			if (loc == null)
			{
				loc = getBedSpawnLocation();
			}
			if (loc != null)
			{
				getPlayer().setCompassTarget(loc);
			}
		}
		catch (Exception ex)
		{
			// Ignore
		}
	}

	@Override
	public int compareTo(final IUser t)
	{
		return FormatUtil.stripColor(this.getPlayer().getDisplayName()).compareTo(FormatUtil.stripColor(t.getPlayer().getDisplayName()));
	}

	@Override
	public void requestTeleport(IUser player, boolean here)
	{
		teleportRequestTime = System.currentTimeMillis();
		teleportRequester = player;
		tpRequestHere = here;
	}

	@Override
	public void setReplyTo(CommandSender user)
	{
		replyTo = user;
	}

	@Override
	public CommandSender getReplyTo()
	{
		return replyTo;
	}

	@Override
	public boolean gotMailInfo()
	{
		return gotMailInfo.getAndSet(true);
	}

	@Override
	public void addMail(String mail)
	{
		super.addMail(mail);
		gotMailInfo.set(false);
	}

	@Override
	public void giveItems(ItemStack itemStack, Boolean canSpew) throws ChargeException
	{
		if (giveItemStack(itemStack, canSpew))
		{
			sendMessage(_("InvFull"));
		}
		getPlayer().updateInventory();
	}

	@Override
	public void giveItems(List<ItemStack> itemStacks, Boolean canSpew) throws ChargeException
	{
		boolean spew = false;
		for (ItemStack itemStack : itemStacks)
		{
			if (giveItemStack(itemStack, canSpew))
			{
				spew = true;
			}
		}
		if (spew)
		{
			sendMessage(_("InvFull"));
		}
		getPlayer().updateInventory();
	}

	private boolean giveItemStack(ItemStack itemStack, Boolean canSpew) throws ChargeException
	{
		boolean spew = false;

		if (itemStack == null || itemStack.getTypeId() == 0)
		{
			return spew;
		}

		final Map<Integer, ItemStack> overfilled;
		if (Permissions.OVERSIZEDSTACKS.isAuthorized(this))
		{
			final ISettings settings = ess.getSettings();
			int oversizedStackSize = settings.getData().getGeneral().getOversizedStacksize();

			overfilled = InventoryWorkaround.addOversizedItems(getPlayer().getInventory(), oversizedStackSize, itemStack);
		}
		else
		{
			overfilled = InventoryWorkaround.addItems(getPlayer().getInventory(), itemStack);
		}
		if (canSpew)
		{
			for (ItemStack overflowStack : overfilled.values())
			{
				getPlayer().getWorld().dropItemNaturally(getPlayer().getLocation(), overflowStack);
				spew = true;
			}
		}
		else
		{
			if (!overfilled.isEmpty())
			{
				throw new ChargeException("Inventory full");
			}
		}
		return spew;
	}

	@Override
	public boolean canAfford(final double cost)
	{
		final double mon = getMoney();
		if (Permissions.ECO_LOAN.isAuthorized(this))
		{
			final ISettings settings = ess.getSettings();
			return (mon - cost) >= settings.getData().getEconomy().getMinMoney();
		}
		return cost <= mon;
	}

	@Override
	public void updateMoneyCache(double userMoney)
	{
		if (super.getMoney() != userMoney)
		{
			super.setMoney(userMoney);
		}
	}

	@Override
	public boolean canAfford(double amount, boolean b)
	{
		return true;
	}
	private long teleportInvulnerabilityTimestamp = 0;

	public void enableInvulnerabilityAfterTeleport()
	{
		final ISettings settings = ess.getSettings();

		final long time = settings.getData().getCommands().getTeleport().getInvulnerability();
		if (time > 0)
		{
			teleportInvulnerabilityTimestamp = System.currentTimeMillis() + time;
		}
	}

	@Override
	public void resetInvulnerabilityAfterTeleport()
	{
		if (teleportInvulnerabilityTimestamp != 0 && teleportInvulnerabilityTimestamp < System.currentTimeMillis())
		{
			teleportInvulnerabilityTimestamp = 0;
		}
	}

	@Override
	public boolean hasInvulnerabilityAfterTeleport()
	{
		return teleportInvulnerabilityTimestamp != 0 && teleportInvulnerabilityTimestamp >= System.currentTimeMillis();
	}

	@Override
	public void setVanished(boolean set)
	{
		vanished = set;
		if (set)
		{
			for (Player p : ess.getServer().getOnlinePlayers())
			{
				if (!Permissions.VANISH_SEE_OTHERS.isAuthorized(ess.getUserMap().getUser(p)))
				{
					p.hidePlayer(getPlayer());
				}
			}
			ess.getVanishedPlayers().add(getName());
			if (Permissions.VANISH_EFFECT.isAuthorized(this))
			{
				getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false));
			}
		}
		else
		{
			for (Player p : ess.getServer().getOnlinePlayers())
			{
				p.showPlayer(getPlayer());
			}
			ess.getVanishedPlayers().remove(getName());
			if (Permissions.VANISH_EFFECT.isAuthorized(this))
			{
				getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
			}
		}
	}

	@Override
	public void toggleVanished()
	{
		final boolean set = !vanished;
		this.setVanished(set);
	}

	@Override
	public boolean checkSignThrottle(int usageLimit)
	{
		if (isSignThrottled(usageLimit))
		{
			return true;
		}
		updateThrottle();
		return false;
	}

	public boolean isSignThrottled(int usageLimit)
	{
		final long minTime = lastThrottledAction + (1000 / usageLimit);
		return (System.currentTimeMillis() < minTime);
	}

	private void updateThrottle()
	{
		lastThrottledAction = System.currentTimeMillis();
	}
}
