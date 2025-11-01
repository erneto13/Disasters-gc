package me.hhitt.disasters

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.arena.ArenaSetupManager
import me.hhitt.disasters.command.ArenaCommand
import me.hhitt.disasters.command.ArenaSetupCommand
import me.hhitt.disasters.command.DisastersCommand
import me.hhitt.disasters.disaster.DisasterTask
import me.hhitt.disasters.hook.PlaceholderAPIHook
import me.hhitt.disasters.listener.*
import me.hhitt.disasters.sidebar.SidebarService
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Filer
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.visual.CuboidVisualizer
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.zapper.ZapperJavaPlugin

class Disasters : ZapperJavaPlugin() {

    companion object {
        private lateinit var instance: Disasters
        fun getInstance(): Disasters = instance
    }

    private lateinit var arenaManager: ArenaManager
    private lateinit var arenaSetupManager: ArenaSetupManager
    private lateinit var sidebarService: SidebarService

    private lateinit var cuboidVisualizer: CuboidVisualizer

    override fun onEnable() {
        instance = this
        initStorage()
        initHooks()
        initVisualizers()
        initSidebar()
        registerCommands()
        registerListeners()
        initDisasters()

        logger.info("Disasters plugin enabled successfully!")
    }

    override fun onDisable() {
        if (::cuboidVisualizer.isInitialized) {
            cuboidVisualizer.stopAllVisualizations()
            logger.info("All visualizations cleaned up")
        }

        logger.info("Disasters plugin disabled successfully!")
    }

    private fun initHooks() {
        val worldEditPlugin = server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin?
        if (worldEditPlugin == null) {
            logger.severe("WorldEdit plugin not found! Disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }

        arenaManager = ArenaManager(worldEditPlugin)
        arenaSetupManager = ArenaSetupManager()

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook(arenaManager).register()
            logger.info("PlaceholderAPI hook registered")
        }
    }

    private fun initStorage() {
        Filer.createFolders()
        FileManager.initialize()
        Lobby.setLocation()
        Data.load()
    }

    private fun initVisualizers() {
        cuboidVisualizer = CuboidVisualizer(this)
        logger.info("Visualizers initialized")
    }

    private fun initSidebar() {
        sidebarService = SidebarService(arenaManager)
    }

    private fun registerCommands() {
        val lamp: Lamp<BukkitCommandActor> = BukkitLamp.builder(this).build()

        lamp.register(
                ArenaCommand(arenaManager),
                DisastersCommand(this, arenaManager, sidebarService),
                ArenaSetupCommand(arenaManager, arenaSetupManager, cuboidVisualizer),
        )

        logger.info("Commands registered")
    }

    private fun registerListeners() {
        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerSuspendingEvents(PlayerDamageListener(arenaManager), this)
        server.pluginManager.registerSuspendingEvents(PlayerDeathListener(arenaManager), this)
        server.pluginManager.registerEvents(ExplosionListener(arenaManager), this)
        server.pluginManager.registerEvents(ItemPickupListener(arenaManager), this)
        server.pluginManager.registerEvents(PlayerLeaveListener(arenaManager), this)
        server.pluginManager.registerEvents(BlockBreakListener(arenaManager), this)
        server.pluginManager.registerEvents(BlockPlaceListener(arenaManager), this)
        server.pluginManager.registerEvents(HealthRegenListener(arenaManager), this)
        server.pluginManager.registerEvents(PlayerMoveListener(arenaManager), this)
        server.pluginManager.registerEvents(PlayerJumpListener(arenaManager), this)
        server.pluginManager.registerEvents(ChatInputListener(), this)
        server.pluginManager.registerEvents(ArenaEditGUIListener(), this)

        server.pluginManager.registerEvents(
                SelectionListener(arenaSetupManager, cuboidVisualizer),
                this
        )

        logger.info("Listeners registered")
    }

    private fun initDisasters() {
        DisasterTask().runTaskTimer(this, 0, 20)
        logger.info("Disaster task initialized")
    }

    fun getArenaManager(): ArenaManager = arenaManager
    fun getSetupManager(): ArenaSetupManager = arenaSetupManager
    fun getSidebarService(): SidebarService = sidebarService
    fun getCuboidVisualizer(): CuboidVisualizer = cuboidVisualizer
}
