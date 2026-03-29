package net.jetluna.lasthardcore.game.managers

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.game.LHGamePlugin
import java.util.UUID

class ScoreboardManager(private val plugin: LHGamePlugin) : Listener {

    private var totalAdv = 0
    private val completedAdv = mutableMapOf<UUID, Int>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        var count = 0
        val it = Bukkit.getServer().advancementIterator()
        while (it.hasNext()) {
            val adv = it.next()
            if (!adv.key.key.startsWith("recipes/")) count++
        }
        totalAdv = count

        for (player in Bukkit.getOnlinePlayers()) {
            var pCount = 0
            val pIt = Bukkit.getServer().advancementIterator()
            while (pIt.hasNext()) {
                val adv = pIt.next()
                if (!adv.key.key.startsWith("recipes/") && player.getAdvancementProgress(adv).isDone) {
                    pCount++
                }
            }
            completedAdv[player.uniqueId] = pCount
        }

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                val board = player.scoreboard
                if (board == Bukkit.getScoreboardManager().mainScoreboard) {
                    player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
                }
                val currentBoard = player.scoreboard

                var objective = currentBoard.getObjective("lh_board")
                if (objective == null) {
                    objective = currentBoard.registerNewObjective("lh_board", Criteria.DUMMY, Component.text("§4§lLAST §c§lHARDCORE"))
                    objective.displaySlot = DisplaySlot.SIDEBAR
                }

                val api = LastHardcoreAPI.instance
                val uuid = player.uniqueId

                val lives = api.livesManager.getLives(uuid)
                val totalMinutes = api.playtimeManager.getMinutes(uuid)
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                val timeStr = if (hours > 0) "${hours}ч ${mins}м" else "${mins}м"

                val mobKills = player.getStatistic(Statistic.MOB_KILLS)
                val playerKills = player.getStatistic(Statistic.PLAYER_KILLS)
                val trades = player.getStatistic(Statistic.TRADED_WITH_VILLAGER)
                val debris = player.getStatistic(Statistic.MINE_BLOCK, Material.ANCIENT_DEBRIS)
                val advCount = completedAdv[uuid] ?: 0

                val lines = listOf(
                    "§1",
                    "§f Жизни: §c❤ $lives",
                    "§f Наиграно: §e$timeStr",
                    "§2",
                    "§7 Статистика:",
                    "§f Достижений: §d$advCount/$totalAdv",
                    "§f Убийств (М/И): §c$mobKills §8| §c$playerKills",
                    "§f Сделок: §a$trades",
                    "§f Обломков: §6$debris",
                    "§3",
                    "§b t.me/lasthardcore"
                )

                lines.reversed().forEachIndexed { index, text ->
                    val score = index + 1
                    val teamName = "line_$score"
                    var team = currentBoard.getTeam(teamName)
                    if (team == null) {
                        team = currentBoard.registerNewTeam(teamName)
                    }

                    val entry = "§" + score.toString(16) + "§r"
                    if (!team.hasEntry(entry)) {
                        team.addEntry(entry)
                    }

                    team.prefix(Component.text(text))
                    objective.getScore(entry).score = score
                }
            }
        }, 0L, 20L)
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        var count = 0
        val it = Bukkit.getServer().advancementIterator()
        while (it.hasNext()) {
            val adv = it.next()
            if (!adv.key.key.startsWith("recipes/") && e.player.getAdvancementProgress(adv).isDone) {
                count++
            }
        }
        completedAdv[e.player.uniqueId] = count
    }

    @EventHandler
    fun onAdvancement(e: PlayerAdvancementDoneEvent) {
        if (!e.advancement.key.key.startsWith("recipes/")) {
            completedAdv[e.player.uniqueId] = (completedAdv[e.player.uniqueId] ?: 0) + 1
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        completedAdv.remove(e.player.uniqueId)
    }
}