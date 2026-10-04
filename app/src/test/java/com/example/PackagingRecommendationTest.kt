package com.example

import com.example.data.local.DefaultPackagingData
import com.example.data.model.FoodCommodity
import com.example.data.model.OptimizationGoal
import com.example.data.model.PackagingMaterial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackagingRecommendationTest {

    @Test
    fun `default commodities and materials are populated with valid properties`() {
        val commodities = DefaultPackagingData.commodities
        val materials = DefaultPackagingData.materials

        assertTrue("Should have at least 8 commodities", commodities.size >= 8)
        assertTrue("Should have at least 8 materials", materials.size >= 8)

        commodities.forEach { c ->
            assertTrue("Baseline shelf life must be positive for ${c.name}", c.baselineShelfLifeDays > 0)
            assertTrue("Target OTR min must be positive for ${c.name}", c.targetOtrMin > 0)
            assertTrue("Target WVTR min must be positive for ${c.name}", c.targetWvtrMin > 0)
        }

        materials.forEach { m ->
            assertTrue("Carbon footprint must be positive for ${m.name}", m.carbonFootprintKgCo2 > 0)
            assertTrue("Circularity score must be between 0 and 100 for ${m.name}", m.circularityScore in 0..100)
            assertTrue("Tensile strength must be positive for ${m.name}", m.tensileStrengthMpa > 0)
        }
    }

    @Test
    fun `sample specifications have consistent metrics`() {
        val specs = DefaultPackagingData.sampleSpecs
        assertTrue("Should have initial approved specs", specs.isNotEmpty())

        specs.forEach { s ->
            assertTrue("Predicted shelf life should exceed baseline for ${s.projectName}", s.predictedShelfLifeDays >= s.baselineShelfLifeDays)
            assertTrue("Eco score must be between 10 and 100 for ${s.projectName}", s.ecoScore in 10..100)
        }
    }
}
