package fidas.server.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.Server;
import fidas.server.gameserver.cache.CrestCache;
import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.custom.AutoVoteRewardManager;
import fidas.server.gameserver.datatables.AdminTable;
import fidas.server.gameserver.datatables.ArmorSetsData;
import fidas.server.gameserver.datatables.AugmentationData;
import fidas.server.gameserver.datatables.ChampionData;
import fidas.server.gameserver.datatables.CharNameTable;
import fidas.server.gameserver.datatables.CharSummonTable;
import fidas.server.gameserver.datatables.CharTemplateTable;
import fidas.server.gameserver.datatables.ClanTable;
import fidas.server.gameserver.datatables.ClassListData;
import fidas.server.gameserver.datatables.DoorTable;
import fidas.server.gameserver.datatables.EnchantGroupsData;
import fidas.server.gameserver.datatables.EnchantHPBonusData;
import fidas.server.gameserver.datatables.EnchantItemData;
import fidas.server.gameserver.datatables.EnchantOptionsData;
import fidas.server.gameserver.datatables.EventDroplist;
import fidas.server.gameserver.datatables.ExperienceTable;
import fidas.server.gameserver.datatables.FakePcsTable;
import fidas.server.gameserver.datatables.FishData;
import fidas.server.gameserver.datatables.FishingMonstersData;
import fidas.server.gameserver.datatables.FishingRodsData;
import fidas.server.gameserver.datatables.HennaData;
import fidas.server.gameserver.datatables.HerbDropTable;
import fidas.server.gameserver.datatables.HitConditionBonus;
import fidas.server.gameserver.datatables.InitialEquipmentData;
import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.datatables.ManorData;
import fidas.server.gameserver.datatables.MerchantPriceConfigTable;
import fidas.server.gameserver.datatables.MultiSell;
import fidas.server.gameserver.datatables.MultilangMsgData;
import fidas.server.gameserver.datatables.NpcBufferTable;
import fidas.server.gameserver.datatables.NpcPersonalAIData;
import fidas.server.gameserver.datatables.NpcTable;
import fidas.server.gameserver.datatables.NpcWalkerRoutesData;
import fidas.server.gameserver.datatables.OfflineTradersTable;
import fidas.server.gameserver.datatables.PetDataTable;
import fidas.server.gameserver.datatables.PremiumTable;
import fidas.server.gameserver.datatables.ProductItemTable;
import fidas.server.gameserver.datatables.RecipeData;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.datatables.SkillTreesData;
import fidas.server.gameserver.datatables.SpawnTable;
import fidas.server.gameserver.datatables.StaticObjects;
import fidas.server.gameserver.datatables.SummonItemsData;
import fidas.server.gameserver.datatables.SummonSkillsTable;
import fidas.server.gameserver.datatables.TeleportLocationTable;
import fidas.server.gameserver.datatables.UITable;
import fidas.server.gameserver.events.EventsInterface;
import fidas.server.gameserver.events.Main;
import fidas.server.gameserver.fence.FenceBuilderManager;
import fidas.server.gameserver.fence.MovieMakerManager;
import fidas.server.gameserver.geoeditorcon.GeoEditorListener;
import fidas.server.gameserver.handler.EffectHandler;
import fidas.server.gameserver.idfactory.IdFactory;
import fidas.server.gameserver.instancemanager.AirShipManager;
import fidas.server.gameserver.instancemanager.AntiFeedManager;
import fidas.server.gameserver.instancemanager.AuctionManager;
import fidas.server.gameserver.instancemanager.BoatManager;
import fidas.server.gameserver.instancemanager.BonusExpManager;
import fidas.server.gameserver.instancemanager.CHSiegeManager;
import fidas.server.gameserver.instancemanager.CastleManager;
import fidas.server.gameserver.instancemanager.CastleManorManager;
import fidas.server.gameserver.instancemanager.ClanHallManager;
import fidas.server.gameserver.instancemanager.CoupleManager;
import fidas.server.gameserver.instancemanager.CursedWeaponsManager;
import fidas.server.gameserver.instancemanager.DayNightSpawnManager;
import fidas.server.gameserver.instancemanager.DimensionalRiftManager;
import fidas.server.gameserver.instancemanager.ExpirableServicesManager;
import fidas.server.gameserver.instancemanager.FortManager;
import fidas.server.gameserver.instancemanager.FortSiegeManager;
import fidas.server.gameserver.instancemanager.FourSepulchersManager;
import fidas.server.gameserver.instancemanager.GlobalVariablesManager;
import fidas.server.gameserver.instancemanager.GraciaSeedsManager;
import fidas.server.gameserver.instancemanager.GrandBossManager;
import fidas.server.gameserver.instancemanager.HellboundManager;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.instancemanager.ItemAuctionManager;
import fidas.server.gameserver.instancemanager.ItemsOnGroundManager;
import fidas.server.gameserver.instancemanager.MailManager;
import fidas.server.gameserver.instancemanager.MapRegionManager;
import fidas.server.gameserver.instancemanager.MercTicketManager;
import fidas.server.gameserver.instancemanager.PcCafePointsManager;
import fidas.server.gameserver.instancemanager.PetitionManager;
import fidas.server.gameserver.instancemanager.QuestManager;
import fidas.server.gameserver.instancemanager.RaidBossPointsManager;
import fidas.server.gameserver.instancemanager.RaidBossSpawnManager;
import fidas.server.gameserver.instancemanager.SiegeManager;
import fidas.server.gameserver.instancemanager.SoIManager;
import fidas.server.gameserver.instancemanager.TerritoryWarManager;
import fidas.server.gameserver.instancemanager.TransformationManager;
import fidas.server.gameserver.instancemanager.WalkingManager;
import fidas.server.gameserver.instancemanager.ZoneManager;
import fidas.server.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import fidas.server.gameserver.instancemanager.leaderboards.CraftLeaderboard;
import fidas.server.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import fidas.server.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import fidas.server.gameserver.model.AutoSpawnHandler;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.PartyMatchRoomList;
import fidas.server.gameserver.model.PartyMatchWaitingList;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Hero;
import fidas.server.gameserver.model.entity.Hitman;
import fidas.server.gameserver.model.entity.TownWarManager;
import fidas.server.gameserver.model.entity.TvTManager;
import fidas.server.gameserver.model.entity.TvTRoundManager;
import fidas.server.gameserver.model.olympiad.Olympiad;
import fidas.server.gameserver.network.L2GameClient;
import fidas.server.gameserver.network.L2GamePacketHandler;
import fidas.server.gameserver.network.communityserver.CommunityServerThread;
import fidas.server.gameserver.pathfinding.PathFinding;
import fidas.server.gameserver.script.faenor.FaenorScriptEngine;
import fidas.server.gameserver.scripting.L2ScriptEngineManager;
import fidas.server.gameserver.taskmanager.AutoAnnounceTaskManager;
import fidas.server.gameserver.taskmanager.KnownListUpdateTaskManager;
import fidas.server.gameserver.taskmanager.TaskManager;
import fidas.server.status.Status;
import fidas.server.tools.discord.DiscordRichPresenceManager;
import fidas.server.util.DeadLockDetector;
import fidas.server.util.IPv4Filter;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	static ScheduledExecutorService autoPotionScheduler = Executors.newScheduledThreadPool(1);
	static ScheduledExecutorService autoFarmScheduler = Executors.newScheduledThreadPool(1);

	public static void startAutoPotionTask() {
	    autoPotionScheduler.scheduleAtFixedRate(() -> {
	    	for (L2PcInstance player : L2World.getInstance().getAllPlayersArray()) {
	    	    player.checkAndUseAutoPotion();
	    	    player.checkAndUseAutoFarm();
	    	}
	    }, 0, 500, TimeUnit.MILLISECONDS); // Ejecuta cada segundo
	}
	/*
	public static void startAutoFarmTask() {
		autoFarmScheduler.scheduleAtFixedRate(() -> {
			System.out.println("EMPIEZA EL STAR AUTOFARM");
		    for (L2PcInstance player : L2World.getInstance().getAllPlayersArray()) {
		       player.checkAndUseAutoFarm();
		    }
		}, 1, 2, TimeUnit.SECONDS); // Ejecuta cada 2 segundos
	}*/
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		_log.finest(getClass().getSimpleName() + ": Memoria usada:" + getUsedMemoryMB() + "MB");
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe(getClass().getSimpleName() + ": Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("Mundo");
		// start game time control early
		GameTimeController.getInstance();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillTable.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		EnchantItemData.getInstance();
		EnchantOptionsData.getInstance();
		SummonItemsData.getInstance();
		EnchantHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		TradeController.getInstance();
		MultiSell.getInstance();
		ProductItemTable.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		ExperienceTable.getInstance();
		HitConditionBonus.getInstance();
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		AdminTable.getInstance();
		GmListTable.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();
		
		PremiumTable.getInstance();
		ExpirableServicesManager.getInstance();
		
		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			PathFinding.getInstance();
		}
		
		printSection("NPCs");
		HerbDropTable.getInstance();
		NpcTable.getInstance();
		if (Config.L2JMOD_CHAMPION_ENABLE)
		{
			ChampionData.getInstance();
		}
		NpcWalkerRoutesData.getInstance();
		WalkingManager.getInstance();
		NpcPersonalAIData.getInstance();
		StaticObjects.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		if (Config.FENCE_MOVIE_BUILDER)
		{
			MovieMakerManager.getInstance();
			FenceBuilderManager.getInstance();
		}
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		FakePcsTable.getInstance();
		SpawnTable.getInstance();
		HellboundManager.getInstance();
		
		if (Config.ENABLE_BONUS_MANAGER)
		{
			BonusExpManager.getInstance();
		}
		
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		PcCafePointsManager.getInstance();
		ManorData.getInstance();
		
		printSection("Olimpiadas");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();
		TeleportLocationTable.getInstance();
		UITable.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		MultilangMsgData.getInstance();
		TransformationManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		Hitman.start();
		GraciaSeedsManager.getInstance();
		SoIManager.getInstance();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceConfigTable.getInstance().updateReferences();
		
		if (Config.VOTE_SYSTEM_ENABLE == true)
		{
			AutoVoteRewardManager.getInstance();
		}
		
		try
		{
			_log.info(getClass().getSimpleName() + ": Cargando Scripts");
			File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
			}
		}
		catch (IOException ioe)
		{
			_log.severe(getClass().getSimpleName() + ": Failed loading scripts.cfg, no script going to be loaded");
		}
		
		QuestManager.getInstance().report();
		TransformationManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
			
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoSpawnHandler: " + AutoSpawnHandler.getInstance().size() + " handlers no total.");
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		if (Config.RANK_ARENA_ENABLED)
		{
			ArenaLeaderboard.getInstance();
		}
		
		if (Config.RANK_FISHERMAN_ENABLED)
		{
			FishermanLeaderboard.getInstance();
		}
		
		if (Config.RANK_CRAFT_ENABLED)
		{
			CraftLeaderboard.getInstance();
		}
		
		if (Config.RANK_TVT_ENABLED)
		{
			TvTLeaderboard.getInstance();
		}
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		KnownListUpdateTaskManager.getInstance();

		printSection("Events Manager");
		Main.main();
		EventsInterface.start();
		TvTManager.getInstance();
		TownWarManager.getInstance();
		TvTRoundManager.getInstance();

		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();

		if (Config.AUTO_RESTART_ENABLE)
		{
			GameServerRestart.getInstance().StartCalculationOfNextRestartTime();
		}
		else
		{
			_log.info("[Auto Restart]: System is disabled.");
		}
				
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the
		// allocation pool
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(getClass().getSimpleName() + ": Iniciado, memoria livre " + freeMem + " Mb de " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Numero maximo de players conectados: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Servidor carregado em " + ((serverLoadEnd - serverLoadStart) / 1000) + " segundos");
		
		AutoAnnounceTaskManager.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;

		DiscordRichPresenceManager.initialize();
		 
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		printSection("Database");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info(GameServer.class.getSimpleName() + ": Telnet desativado.");
		}
		
		// start scheduler autopotion
		startAutoPotionTask();
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}