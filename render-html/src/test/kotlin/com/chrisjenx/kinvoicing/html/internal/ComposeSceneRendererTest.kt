package com.chrisjenx.kinvoicing.html.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 * Proves the reflective [ComposeSceneRenderer] can drive the internal CanvasLayersComposeScene
 * API and produce vector SVG output for both text and shapes, independent of the CMP version's
 * internal scene shape (LegacyDriver on <= 1.11, NextDriver on >= 1.12).
 */
class ComposeSceneRendererTest {

    @Test
    fun `render produces non-empty SVG for text content`() {
        val svg = ComposeToSvg.render(400, 200, Density(1f)) {
            Text("Reflective driver")
        }
        assertTrue(svg.isNotEmpty(), "SVG output should be non-empty")
        assertContains(svg, "<svg")
    }

    @Test
    fun `render produces SVG for shapes`() {
        val svg = ComposeToSvg.render(400, 200, Density(1f)) {
            Box(Modifier.fillMaxWidth().height(50.dp).background(Color.Red))
        }
        assertTrue(svg.isNotEmpty(), "SVG output should be non-empty")
        assertContains(svg, "<svg")
    }
}
