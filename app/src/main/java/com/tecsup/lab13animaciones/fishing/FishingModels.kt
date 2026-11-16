package com.tecsup.lab13animaciones.fishing

// Stable IDs are persisted; adding species does not invalidate existing saves.
enum class Rarity(val title: String, val weight: Int) {
    COMMON("Común", 65), UNCOMMON("Poco común", 25), RARE("Raro", 9), LEGENDARY("Legendario", 1)
}
data class Rod(val id: Int, val name: String, val price: Int, val depth: Int, val color: Long)
data class Zone(val id: Int, val name: String, val min: Int, val max: Int, val sky: Long, val water: Long)
data class Fish(val id: String, val name: String, val zone: Int, val rarity: Rarity,
                val min: Int, val max: Int, val value: Int, val sizeCm: Int, val difficulty: Float, val color: Long, val shape: Int)
data class FishRecord(val count: Int = 0, val bestCm: Int = 0)
data class Progress(val coins: Int = 0, val owned: Set<Int> = setOf(0), val equipped: Int = 0,
                    val records: Map<String, FishRecord> = emptyMap(), val deepest: Int = 0, val lastRewardToken: String = "") {
    val rod: Rod get() = rods.first { it.id == equipped }
    val total: Int get() = records.values.sumOf { it.count }
}
data class Catch(val token: String, val fish: Fish, val depth: Int, val sizeCm: Int)
enum class FishingPhase { IDLE, CASTING, SINKING, BITE, REELING, SUCCESS, ESCAPED }
enum class GamePage { HOME, WATER, SHOP, COLLECTION, ZONES }
val rods = listOf(
    Rod(0,"Caña de madera",0,10,0xFFB57945), Rod(1,"Caña básica",150,25,0xFF5BB89B),
    Rod(2,"Caña profesional",400,50,0xFFF2B84D), Rod(3,"Caña de titanio",900,100,0xFFAFC5CF),
    Rod(4,"Caña eléctrica",1800,200,0xFF9D8DFF), Rod(5,"Caña legendaria",3500,500,0xFFFFD86A))
val zones = listOf(
    Zone(0,"Lago tranquilo",0,25,0xFFBCEAF0,0xFF238C9A),
    Zone(1,"Lago profundo",25,100,0xFFBDCDE4,0xFF21466C),
    Zone(2,"Océano",100,200,0xFFFFDBAA,0xFF16617C),
    Zone(3,"Abismo legendario",200,500,0xFF989BBE,0xFF121D3B))
val species = listOf(
    Fish("perca","Perca dorada",0,Rarity.COMMON,1,25,24,18,.18f,0xFFF4B548,0),
    Fish("trucha","Trucha coral",0,Rarity.UNCOMMON,4,25,48,32,.30f,0xFFED8878,1),
    Fish("carpa","Carpa de jade",0,Rarity.RARE,8,25,100,50,.45f,0xFF88C39D,2),
    Fish("bagre","Bagre de río",1,Rarity.COMMON,26,100,65,65,.25f,0xFFA4A9C9,3),
    Fish("lucio","Lucio esmeralda",1,Rarity.UNCOMMON,38,100,110,85,.38f,0xFF64BFA9,1),
    Fish("esturion","Esturión real",1,Rarity.RARE,65,100,210,120,.52f,0xFFC0CEDD,3),
    Fish("payaso","Payaso mandarín",2,Rarity.COMMON,101,200,110,24,.30f,0xFFFFA34B,0),
    Fish("cirujano","Cirujano zafiro",2,Rarity.UNCOMMON,125,200,190,38,.44f,0xFF69A7F5,2),
    Fish("vela","Pez vela rubí",2,Rarity.RARE,160,200,340,160,.60f,0xFFE980A7,1),
    Fish("linterna","Pez linterna",3,Rarity.COMMON,201,500,200,48,.42f,0xFF75DBCD,2),
    Fish("dragon","Dragón abisal",3,Rarity.RARE,280,500,600,190,.68f,0xFFB392EE,3),
    Fish("leviatan","Leviatán aurora",3,Rarity.LEGENDARY,400,500,1400,350,.80f,0xFFFFD276,1))
