package com.tecsup.lab13animaciones.fishing

/** Missions count only new catches after introduction; legacy collections do not auto-pay. */
data class FishingMission(val id: String, val title: String, val target: Int, val coins: Int, val xp: Int,
                          val metric: (Progress) -> Int)
object FishingMissions {
    val all = listOf(
        FishingMission("primeros_cinco", "Captura 5 peces", 5, 100, 75) { it.missionCatches },
        FishingMission("tres_especies", "Descubre 3 especies en nuevas capturas", 3, 150, 125) { it.missionSpecies.size },
        FishingMission("explorador_40", "Alcanza 40 m de profundidad", 40, 200, 150) { it.missionDeepest }
    )
    fun settle(progress: Progress): Progress {
        var next = progress
        for (mission in all) {
            if (mission.id !in next.completedMissions && mission.metric(next) >= mission.target) {
                next = next.copy(
                    coins = (next.coins.toLong() + mission.coins).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    xp = (next.xp.toLong() + mission.xp).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    completedMissions = next.completedMissions + mission.id
                )
            }
        }
        return next
    }
}
