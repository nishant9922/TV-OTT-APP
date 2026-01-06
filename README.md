Overview

This Android TV application implements a Netflix-style browsing experience with vertical content rails, a Mini Player, and TV-optimized focus navigation using the D-Pad.

The app is designed to follow Android TV focus rules, ensuring smooth navigation across rails, menu drawer, and Mini Player without breaking existing playback or navigation flows.

Key Features
Mini Player

Supports background video playback.

Appears as a Mini Player overlay when enabled.

Can be focused independently without interrupting playback.

Transitions smoothly to full-screen player when required.

Focus & Navigation Behavior

The app follows these focus rules on Android TV:

DPAD navigation is fully supported across banners, rails, cards, menu, and Mini Player.

Focus can move from any rail item to the Mini Player by long-pressing DPAD_CENTER (≥ 3 seconds).

Short press on DPAD_CENTER continues to open the video (existing behavior is preserved).

Pressing UP from the Mini Player returns focus to the previously focused rail item.

Pressing BACK closes the Mini Player.

Left DPAD from the first card of a rail opens the side menu.

Right DPAD from the last card of a rail moves focus to the Mini Player (if visible).

Menu drawer behavior remains unchanged across all fragments.

DPAD_CENTER Long-Press Logic

Holding DPAD_CENTER for 3 seconds or more on any card:

Moves focus to the Mini Player

Does not launch full-screen playback

Releasing DPAD_CENTER before 3 seconds:

Triggers the normal click action (video opens)

This ensures:

No conflict with existing click behavior

No accidental full-screen launches

TV-friendly interaction model

Architecture Highlights:

Single player instance is reused across:

Full-screen Player

Mini Player

Player attachment/detachment is handled safely to avoid:

Multiple playback instances

Memory leaks

Focus logic is handled at:

Card level (for long-press detection)

Activity level (for Mini Player focus control)

Supported Components:

Android TV (Leanback compatible)

RecyclerView-based vertical rails

Horizontal card rails

Mini Player overlay

Side navigation drawer

DPAD-only navigation (no touch dependency)

Design Goals:

Maintain existing playback & navigation flows

Add new features without regressions

Follow Android TV UX best practices

Ensure predictable focus behavior

Avoid accidental player launches

Notes:

Exact seek/resume behavior depends on player SDK limitations.

Only one video playback is supported at a time.

Mini Player behavior follows standard Android TV lifecycle rules.

Status:

Stable
Tested with long-press DPAD behavior
No impact on existing click/navigation logic
