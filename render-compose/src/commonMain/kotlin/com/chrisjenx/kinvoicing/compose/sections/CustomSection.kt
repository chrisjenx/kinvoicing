package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chrisjenx.kinvoicing.InvoiceSection

@Composable
internal fun CustomSection(custom: InvoiceSection.Custom) {
    Column(modifier = Modifier.fillMaxWidth()) {
        custom.content.forEach { ElementContent(it) }
    }
}
