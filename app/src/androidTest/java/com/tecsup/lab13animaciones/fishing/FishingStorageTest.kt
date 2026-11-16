package com.tecsup.lab13animaciones.fishing

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FishingStorageTest {
    @Test fun restoresPurchasesAndDoesNotPayTwiceAcrossInstances() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="fishing_test_only"
        context.getSharedPreferences(name,0).edit().clear().commit()
        try {
            val storage=FishingStorage(context,name)
            assertTrue(storage.save(Progress(coins=500,xp=Progression.threshold(8))))
            assertTrue(storage.changeRod(2))
            val fish=species.first()
            val catch=Catch("persistent-receipt",fish,10,31)
            assertTrue(storage.capture(catch))
            val reopened=FishingStorage(context,name)
            assertEquals(2,reopened.load().equipped)
            assertEquals(setOf(0,2),reopened.load().owned)
            assertEquals(100+fish.value,reopened.load().coins)
            assertTrue(reopened.capture(catch))
            assertEquals(100+fish.value,storage.load().coins)
            assertEquals(FishRecord(1,31),storage.load().records[fish.id])
        } finally { context.getSharedPreferences(name,0).edit().clear().commit() }
    }
    @Test fun migratesActualV1PreferenceAndKeepsNonconsecutiveReceipts() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="fishing_migration_test_only"
        val prefs=context.getSharedPreferences(name,0)
        try {
            prefs.edit().putString("progress", """{"coins":300,"owned":"0,5","equipped":5,"deepest":450,"reward_token":"legacy","records":{"perca":{"count":2,"best":24}}}""").commit()
            val storage=FishingStorage(context,name)
            val legacy=storage.load()
            assertEquals(5,legacy.equipped);assertEquals(0,legacy.xp)
            assertEquals(FishRecord(2,24),legacy.records["perca"])
            val a=Catch("a",species.first(),8,26)
            val b=a.copy(token="b")
            assertTrue(storage.capture(a));assertTrue(storage.capture(b))
            val restored=FishingStorage(context,name)
            assertTrue(restored.capture(a))
            assertEquals(300+2*species.first().value,restored.load().coins)
            assertEquals(50,restored.load().xp);assertEquals(setOf(0,5),restored.load().owned)
            assertEquals(4,restored.load().records["perca"]!!.count)
        } finally { prefs.edit().clear().commit() }
    }

    @Test fun explorationAndMissionRewardsPersistExactlyOnce() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="fishing_missions_test_only"
        val prefs=context.getSharedPreferences(name,0)
        prefs.edit().clear().commit()
        try {
            val storage=FishingStorage(context,name)
            assertTrue(storage.explore(40))
            repeat(5) { assertTrue(storage.capture(Catch("new-$it",species.first(),5,20))) }
            val reopened=FishingStorage(context,name)
            val before=reopened.load()
            assertEquals(40,before.deepest)
            assertEquals(5,before.missionCatches)
            assertEquals(setOf("primeros_cinco","explorador_40"),before.completedMissions)
            assertTrue(reopened.explore(40))
            assertTrue(reopened.capture(Catch("new-0",species.first(),5,20)))
            assertEquals(before,storage.load())
        } finally { prefs.edit().clear().commit() }
    }

    @Test fun retryRecommitsReceiptEvenIfFailedWriteUpdatedMemory() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="fishing_commit_retry_test_only"
        val real=context.getSharedPreferences(name,0)
        real.edit().clear().commit()
        var attempts=0
        val wrapped=object : android.content.ContextWrapper(context) {
            override fun getApplicationContext(): android.content.Context = this
            override fun getSharedPreferences(n: String, mode: Int): android.content.SharedPreferences =
                object : android.content.SharedPreferences by real {
                    override fun edit(): android.content.SharedPreferences.Editor {
                        val delegate=real.edit()
                        return object : android.content.SharedPreferences.Editor by delegate {
                            override fun commit(): Boolean {
                                val ok=delegate.commit()
                                attempts++
                                return ok && attempts>1
                            }
                        }
                    }
                }
        }
        try {
            val storage=FishingStorage(wrapped,name)
            val caught=Catch("retry",species.first(),5,20)
            assertFalse(storage.capture(caught))
            assertTrue(storage.capture(caught))
            assertEquals(2,attempts)
            assertEquals(1,storage.load().total)
            assertEquals(species.first().value,storage.load().coins)
        } finally { real.edit().clear().commit() }
    }

}
