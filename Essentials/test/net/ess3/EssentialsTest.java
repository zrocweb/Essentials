package net.ess3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import net.ess3.api.IPlugin;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public abstract class EssentialsTest extends TestCase
{
	protected final Server server;
	protected final IPlugin plugin;
	protected final World world;
	protected final Logger logger;
	protected Essentials ess;
	protected final List<Player> playerList;
	private File folder;

	public EssentialsTest(final String testName)
	{
		super(testName);
		logger = Logger.getLogger(this.getName());
		world = mock(World.class);
		playerList = new ArrayList<Player>();
		if (Bukkit.getServer() == null)
		{
			server = mock(Server.class);
			PluginManager pluginManager = mock(PluginManager.class);
			when(pluginManager.getPlugins()).thenReturn(new Plugin[0]);
			when(server.getLogger()).thenReturn(logger);
			when(server.getOnlinePlayers()).thenReturn(playerList.toArray(new Player[0]));
			when(server.getPlayerExact(anyString())).thenAnswer(
					new Answer<Player>()
					{
						@Override
						public Player answer(InvocationOnMock invocation) throws Throwable
						{
							Object[] args = invocation.getArguments();
							String name = (String)args[0];
							for (Player player : playerList)
							{
								if (player.getName().equalsIgnoreCase(name))
								{
									return player;
								}
							}
							return null;
						}
					});
			when(server.getPluginManager()).thenReturn(pluginManager);
			when(server.getOfflinePlayers()).thenReturn(playerList.toArray(new Player[0]));
			when(server.getOfflinePlayer(anyString())).thenAnswer(
					new Answer<OfflinePlayer>()
					{
						@Override
						public OfflinePlayer answer(InvocationOnMock invocation) throws Throwable
						{
							Object[] args = invocation.getArguments();
							String name = (String)args[0];
							OfflinePlayer player = mock(OfflinePlayer.class);
							when(player.getName()).thenReturn(name);
							return player;
						}
					});
			Bukkit.setServer(server);
		}
		else
		{
			server = Bukkit.getServer();
		}
		plugin = mock(IPlugin.class);


		folder = FileUtils.getTempDirectory();
		folder = new File(folder, "Essentials-" + System.currentTimeMillis());
		when(plugin.getDataFolder()).thenReturn(folder);
		when(world.getName()).thenReturn("world");

	}

	protected void createFolder()
	{
		logger.log(Level.INFO, "Creating folder for testing: {0}", folder.getAbsolutePath());
		if (!folder.mkdir() || !folder.isDirectory())
		{
			throw new IllegalStateException();
		}
	}

	protected void cleanup()
	{
		logger.log(Level.INFO, "Deleting folder {0}", folder.getAbsolutePath());
		FileUtils.deleteQuietly(folder);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		createFolder();

		ess = new Essentials(server, logger, plugin);
		ess.getI18n().updateLocale("en_US");
		Essentials.testing = true;
		ess.onEnable();
	}

	@Override
	public void tearDown() throws Exception
	{
		super.tearDown();
		ess.onDisable();
		cleanup();
	}

	protected void addPlayer(String name)
	{
		Player player = mock(Player.class);
		when(player.getName()).thenReturn(name);
		playerList.add(player);
	}
}
