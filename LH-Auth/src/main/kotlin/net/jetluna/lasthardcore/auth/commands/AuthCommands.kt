package net.jetluna.lasthardcore.auth.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.auth.LHAuthPlugin
import org.mindrot.jbcrypt.BCrypt

class AuthCommands(private val plugin: LHAuthPlugin) : CommandExecutor {
    private fun connectToServer(player: Player, serverName: String) {
        val out = com.google.common.io.ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(serverName)

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Команда только для игроков!")
            return true
        }

        val uuid = sender.uniqueId

        if (plugin.authManager.isLoggedIn(uuid)) {
            sender.sendMessage(Component.text("§aВы уже успешно авторизованы!"))
            return true
        }

        when (command.name.lowercase()) {
            "reg", "register" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("Использование: /reg <пароль> <повтор пароля>", NamedTextColor.RED))
                    return true
                }

                val pass1 = args[0]
                val pass2 = args[1]

                if (pass1 != pass2) {
                    sender.sendMessage(Component.text("Пароли не совпадают!", NamedTextColor.RED))
                    return true
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    if (plugin.authManager.isRegistered(uuid)) {
                        sender.sendMessage(Component.text("Вы уже зарегистрированы! Используйте /login <пароль>", NamedTextColor.RED))
                        return@Runnable
                    }

                    val hash = BCrypt.hashpw(pass1, BCrypt.gensalt())
                    val currentTime = System.currentTimeMillis()

                    val conn = LastHardcoreAPI.instance.databaseManager.getConnection()
                    if (conn != null) {
                        try {
                            val stmt = conn.prepareStatement("INSERT INTO auth_data (uuid, password_hash, reg_date, last_login) VALUES (?, ?, ?, ?)")
                            stmt.setString(1, uuid.toString())
                            stmt.setString(2, hash)
                            stmt.setLong(3, currentTime)
                            stmt.setLong(4, currentTime)
                            stmt.executeUpdate()
                            stmt.close()

                            Bukkit.getScheduler().runTask(plugin, Runnable {
                                plugin.authManager.login(uuid)
                                sender.clearTitle()
                                sender.sendMessage(Component.text("§aВы успешно зарегистрировались! Отправляем на сервер..."))
                                connectToServer(sender, "hardcore")
                            })
                        } catch (e: Exception) {
                            sender.sendMessage(Component.text("Произошла ошибка базы данных. Обратитесь к администрации.", NamedTextColor.RED))
                            plugin.logger.severe("Ошибка при регистрации: ${e.message}")
                        }
                    }
                })
            }

            "login", "l" -> {
                if (args.isEmpty()) {
                    sender.sendMessage(Component.text("Использование: /login <пароль>", NamedTextColor.RED))
                    return true
                }

                val password = args[0]

                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    val conn = LastHardcoreAPI.instance.databaseManager.getConnection()
                    if (conn != null) {
                        try {
                            val stmt = conn.prepareStatement("SELECT password_hash FROM auth_data WHERE uuid = ?")
                            stmt.setString(1, uuid.toString())
                            val rs = stmt.executeQuery()

                            if (rs.next()) {
                                val hash = rs.getString("password_hash")

                                if (BCrypt.checkpw(password, hash)) {

                                    val updateStmt = conn.prepareStatement("UPDATE auth_data SET last_login = ? WHERE uuid = ?")
                                    updateStmt.setLong(1, System.currentTimeMillis())
                                    updateStmt.setString(2, uuid.toString())
                                    updateStmt.executeUpdate()
                                    updateStmt.close()

                                    Bukkit.getScheduler().runTask(plugin, Runnable {
                                        plugin.authManager.login(uuid)
                                        sender.clearTitle()
                                        sender.sendMessage(Component.text("§aВы успешно авторизовались! Отправляем на сервер..."))
                                        connectToServer(sender, "hardcore")
                                    })
                                } else {
                                    sender.sendMessage(Component.text("Неверный пароль!", NamedTextColor.RED))
                                }
                            } else {
                                sender.sendMessage(Component.text("Вы не зарегистрированы! Используйте /reg <пароль> <пароль>", NamedTextColor.RED))
                            }
                            rs.close()
                            stmt.close()
                        } catch (e: Exception) {
                            sender.sendMessage(Component.text("Произошла ошибка базы данных.", NamedTextColor.RED))
                            plugin.logger.severe("Ошибка при входе: ${e.message}")
                        }
                    }
                })
            }
        }
        return true
    }
}