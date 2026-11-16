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
            assertTrue(storage.save(Progress(coins=500)))
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
}
