package com.chrisjenx.kinvoicing.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SanitizeTest {

    // ── requireSafeUrl ──

    @Test
    fun acceptsHttpUrl() {
        assertEquals("http://example.com", requireSafeUrl("http://example.com", "test"))
    }

    @Test
    fun acceptsHttpsUrl() {
        assertEquals("https://example.com/pay", requireSafeUrl("https://example.com/pay", "test"))
    }

    @Test
    fun acceptsMailtoUrl() {
        assertEquals("mailto:user@example.com", requireSafeUrl("mailto:user@example.com", "test"))
    }

    @Test
    fun acceptsTelUrl() {
        assertEquals("tel:+15551234567", requireSafeUrl("tel:+15551234567", "test"))
    }

    @Test
    fun acceptsRelativeUrl() {
        assertEquals("/path/to/page", requireSafeUrl("/path/to/page", "test"))
    }

    @Test
    fun rejectsJavascriptUrl() {
        assertFailsWith<IllegalArgumentException> {
            requireSafeUrl("javascript:alert(1)", "test")
        }
    }

    @Test
    fun rejectsJavascriptUrlCaseInsensitive() {
        assertFailsWith<IllegalArgumentException> {
            requireSafeUrl("JAVASCRIPT:alert(1)", "test")
        }
    }

    @Test
    fun rejectsDataUrl() {
        assertFailsWith<IllegalArgumentException> {
            requireSafeUrl("data:text/html,<script>alert(1)</script>", "test")
        }
    }

    @Test
    fun rejectsVbscriptUrl() {
        assertFailsWith<IllegalArgumentException> {
            requireSafeUrl("vbscript:MsgBox", "test")
        }
    }

    // ── sanitizeFontFamily ──

    @Test
    fun acceptsNormalFontFamily() {
        assertEquals("Inter", sanitizeFontFamily("Inter"))
    }

    @Test
    fun acceptsFontFamilyWithHyphen() {
        assertEquals("Fira-Code", sanitizeFontFamily("Fira-Code"))
    }

    @Test
    fun acceptsFontFamilyWithSpace() {
        assertEquals("Open Sans", sanitizeFontFamily("Open Sans"))
    }

    @Test
    fun rejectsFontFamilyWithSemicolon() {
        assertFailsWith<IllegalArgumentException> {
            sanitizeFontFamily("Inter; background: url(evil)")
        }
    }

    @Test
    fun rejectsFontFamilyWithBraces() {
        assertFailsWith<IllegalArgumentException> {
            sanitizeFontFamily("Inter} body { display:none")
        }
    }

    @Test
    fun rejectsFontFamilyWithParentheses() {
        assertFailsWith<IllegalArgumentException> {
            sanitizeFontFamily("Inter, url(evil)")
        }
    }

    @Test
    fun rejectsFontFamilyWithAngleBrackets() {
        assertFailsWith<IllegalArgumentException> {
            sanitizeFontFamily("Inter<script>")
        }
    }

    // ── requireFinite ──

    @Test
    fun acceptsZero() {
        assertEquals(0.0, requireFinite(0.0, "test"))
    }

    @Test
    fun acceptsPositive() {
        assertEquals(100.0, requireFinite(100.0, "test"))
    }

    @Test
    fun acceptsNegative() {
        assertEquals(-50.0, requireFinite(-50.0, "test"))
    }

    @Test
    fun acceptsMaxValue() {
        assertEquals(Double.MAX_VALUE, requireFinite(Double.MAX_VALUE, "test"))
    }

    @Test
    fun rejectsNaN() {
        assertFailsWith<IllegalArgumentException> {
            requireFinite(Double.NaN, "test")
        }
    }

    @Test
    fun rejectsPositiveInfinity() {
        assertFailsWith<IllegalArgumentException> {
            requireFinite(Double.POSITIVE_INFINITY, "test")
        }
    }

    @Test
    fun rejectsNegativeInfinity() {
        assertFailsWith<IllegalArgumentException> {
            requireFinite(Double.NEGATIVE_INFINITY, "test")
        }
    }
}
