# Kinvoicing

Kotlin Multiplatform invoicing library with a sealed IR, type-safe DSL builder, and three renderers (Compose, HTML, PDF).

[![Build](https://github.com/chrisjenx/kinvoicing/actions/workflows/build.yml/badge.svg)](https://github.com/chrisjenx/kinvoicing/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-purple.svg)](https://kotlinlang.org)

## Installation

```kotlin
dependencies {
    implementation("com.chrisjenx.kinvoicing:core:<version>")

    // Pick your renderer(s):
    implementation("com.chrisjenx.kinvoicing:render-compose:<version>")
    implementation("com.chrisjenx.kinvoicing:render-html-email:<version>")  // Email-safe HTML
    implementation("com.chrisjenx.kinvoicing:render-html:<version>")        // Compose → HTML (via compose2pdf)
    implementation("com.chrisjenx.kinvoicing:render-pdf:<version>")
}
```

## Quick Start

```kotlin
val doc = invoice {
    header {
        branding {
            primary {
                name("Acme Corp")
                address("123 Main St", "Springfield, IL 62701")
                email("billing@acme.com")
            }
        }
        invoiceNumber("INV-2026-0001")
        issueDate(LocalDate(2026, 3, 1))
        dueDate(LocalDate(2026, 3, 31))
    }
    billFrom {
        name("Acme Corp")
        address("123 Main St", "Springfield, IL 62701")
        email("billing@acme.com")
        phone("+1 (555) 100-0001")
    }
    billTo {
        name("Jane Smith")
        address("456 Oak Ave", "Boulder, CO 80301")
        email("jane@example.com")
        phone("+1 (555) 200-0002")
    }
    lineItems {
        columns("Description", "Qty", "Rate", "Amount")
        item("Web Development", qty = 40, unitPrice = 150.0)
        item("Design Services", qty = 10, unitPrice = 100.0)
        item("Hosting (Monthly)", qty = 1, unitPrice = 49.99)
    }
    summary {
        currency("USD")
    }
    footer {
        notes("Thank you for your business!")
        terms("Net 30")
    }
}
```

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `:core` | `core` | Invoice IR (sealed classes), DSL builder, currency formatting |
| `:render-compose` | `render-compose` | Compose Multiplatform UI renderer |
| `:render-html-email` | `render-html-email` | Email-safe HTML renderer via kotlinx.html |
| `:render-pdf` | `render-pdf` | PDF renderer via compose2pdf |
| `:render-html` | `render-html` | Compose → HTML renderer via compose2pdf |

## Architecture

The core module defines a sealed `InvoiceDocument` IR with 9 section variants (Header, BillFrom, BillTo, LineItems, Summary, PaymentInfo, Footer, Custom, MetaBlock). Renderers use exhaustive `when` blocks over the sealed hierarchy, so adding a new section type is a compile-time-checked change.

The Compose and PDF renderers share a single `InvoiceContent` composable -- PDF matches Compose by construction via [compose2pdf](https://github.com/nicbell/compose2pdf).

## License

```
Copyright 2026 Christopher Jenkins

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
