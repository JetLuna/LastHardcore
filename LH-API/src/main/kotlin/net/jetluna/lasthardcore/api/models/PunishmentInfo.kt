package net.jetluna.lasthardcore.api.models

import java.util.UUID

data class PunishmentInfo(
    val reason: String,
    val expireTime: Long,
    val staffUuid: UUID
)