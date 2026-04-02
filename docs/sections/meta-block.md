---
title: MetaBlock
parent: Sections
nav_order: 7
---

# MetaBlock

The meta block section displays key-value metadata like PO numbers, project names, and cost centers.

```kotlin
metaBlock {
    entry("PO Number", "PO-2026-0042")
    entry("Project", "Website Redesign")
    entry("Contract Ref", "C-2026-001")
    entry("Department", "Engineering")
    entry("Cost Center", "CC-4200")
    entry("Approver", "Sarah Johnson")
}
```

{% include example-preview.html name="meta-block" height="500px" %}

## Builder Reference

### MetaBlockBuilder

| Method | Description |
|--------|-------------|
| `entry(label, value)` | Add a key-value pair |
