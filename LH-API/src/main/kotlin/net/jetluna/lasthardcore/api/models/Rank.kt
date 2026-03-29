package net.jetluna.lasthardcore.api.models

enum class Rank(val prefix: String, val level: Int) {
    DEFAULT("", 0),
    HELPER("§a§lHELPER ", 1),
    MODER("§9§lMODER ", 2),
    ADMIN("§c§lADMIN ", 3),
    CREATOR("§4§lCREATOR ", 4);

    fun isStaff(): Boolean {
        return this.level > 0
    }

    fun isAtLeast(requiredRank: Rank): Boolean {
        return this.level >= requiredRank.level
    }
}