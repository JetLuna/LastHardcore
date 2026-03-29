package net.jetluna.lasthardcore.game.managers

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Team
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.Rank
import net.jetluna.lasthardcore.game.LHGamePlugin

class TabManager(private val plugin: LHGamePlugin) {

    private val timeColor = "§e"
    private val bracketColor = "§8"

    private var ticksCounter = 0

    init {
        startUpdateTask()
    }

    private fun startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            ticksCounter++
            val shouldAddMinute = ticksCounter >= 60

            if (shouldAddMinute) {
                ticksCounter = 0
            }

            val board = Bukkit.getScoreboardManager().mainScoreboard

            for (player in Bukkit.getOnlinePlayers()) {
                val uuid = player.uniqueId
                val api = LastHardcoreAPI.instance

                if (shouldAddMinute) {
                    api.playtimeManager.addMinute(uuid)
                }

                val lives = api.livesManager.getLives(uuid)

                val header = Component.text("\n§4§lLAST §c§lHARDCORE\n \n")
                val footer = Component.text("\n \n§cОсталось жизней: §e$lives\n \n§fНаш телеграм: §bt.me/lasthardcore\n")
                player.sendPlayerListHeaderAndFooter(header, footer)

                val totalMinutes = api.playtimeManager.getMinutes(uuid)
                val hoursPlayed = totalMinutes / 60

                val timeString = if (hoursPlayed > 0) {
                    "${hoursPlayed}h"
                } else {
                    "${totalMinutes}m"
                }

                val rank = api.rankManager.getRank(uuid)
                val rankName = rank.name

                val (prefixStr, nameColor) = when (rank) {
                    Rank.CREATOR -> Pair("§4§l$rankName ", "§4")
                    Rank.ADMIN -> Pair("§c§l$rankName ", "§c")
                    Rank.MODER -> Pair("§9§l$rankName ", "§9")
                    Rank.HELPER -> Pair("§a§l$rankName ", "§a")
                    else -> Pair("", "§7")
                }

                val tabName = Component.text("$prefixStr$nameColor${player.name} $bracketColor[$timeColor$timeString$bracketColor]")
                player.playerListName(tabName)

                val sortWeight = 5 - rank.level
                val teamName = "0${sortWeight}_${rank.name}"

                var team: Team? = board.getTeam(teamName)
                if (team == null) {
                    team = board.registerNewTeam(teamName)
                }

                if (!team.hasEntry(player.name)) {
                    team.addEntry(player.name)
                }
            }
        }, 0L, 20L)
    }
}