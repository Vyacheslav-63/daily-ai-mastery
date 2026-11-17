---
name: Cyber Luminescence
colors:
  surface: '#0f131c'
  surface-dim: '#0f131c'
  surface-bright: '#353943'
  surface-container-lowest: '#0a0e17'
  surface-container-low: '#181b25'
  surface-container: '#1c1f29'
  surface-container-high: '#262a34'
  surface-container-highest: '#31353f'
  on-surface: '#dfe2ef'
  on-surface-variant: '#b9cacb'
  inverse-surface: '#dfe2ef'
  inverse-on-surface: '#2c303a'
  outline: '#849495'
  outline-variant: '#3a494b'
  surface-tint: '#00dce6'
  primary: '#e0fdff'
  on-primary: '#00373a'
  primary-container: '#00f2fe'
  on-primary-container: '#006a70'
  inverse-primary: '#00696f'
  secondary: '#d0bcff'
  on-secondary: '#3c0091'
  secondary-container: '#571bc1'
  on-secondary-container: '#c4abff'
  tertiary: '#ecfaff'
  on-tertiary: '#003640'
  tertiary-container: '#91e8ff'
  on-tertiary-container: '#00697c'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ff6ff'
  primary-fixed-dim: '#00dce6'
  on-primary-fixed: '#002022'
  on-primary-fixed-variant: '#004f53'
  secondary-fixed: '#e9ddff'
  secondary-fixed-dim: '#d0bcff'
  on-secondary-fixed: '#23005c'
  on-secondary-fixed-variant: '#5516be'
  tertiary-fixed: '#acedff'
  tertiary-fixed-dim: '#4cd7f6'
  on-tertiary-fixed: '#001f26'
  on-tertiary-fixed-variant: '#004e5c'
  background: '#0f131c'
  on-background: '#dfe2ef'
  surface-variant: '#31353f'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.015em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.01em
  code-md:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: -0.01em
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-caps:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.08em
  tab-label:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 12px
    letterSpacing: 0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.25rem
---

## Brand & Style

This design system embodies high-velocity digital learning and synthetic intelligence mastery. Designed for developers, engineers, and product builders seeking daily iterative compounding of AI skills, the UI projects an advanced, focused, and futuristic terminal-like environment. 

The aesthetic is an evolved synthesis of **Glassmorphism** and **Cyber Minimalist High-Tech**:
- **Atmosphere:** Deep void space, precision telemetry, focused calm, and ambient bioluminescence.
- **Surfaces:** Translucent frosted materials floating over deep space backdrops, creating atmospheric layering without distracting skeuomorphism.
- **Lighting:** Focused neon kinetic glows that signify active synthesis, progress, mastery milestones, and real-time computation states.
- **Emotional Response:** Sharp cognitive focus, agency, modern craft, and technological immersion.

## Colors

The color palette is built strictly for dark-adapted vision and high-focus contrast ratios, using deep obsidian foundations balanced with electrical gas discharge accents.

### Palette Architecture
- **Base Background Canvas:** `#090D16` (Deep Void Slate) provides an absolute resting surface.
- **Surface Elevation Foundation:** `#0D111F` (Elevated Cyber Deep Slate) serves as the ground plane for frosted layers.
- **Primary Energy:** `#00F2FE` (Neon Cyan) drives key interaction indicators, streaks, completion states, and primary active states.
- **Secondary Luminescence:** `#8B5CF6` (Cyber Violet) and `#A855F7` signify neural computations, prompt pipelines, and milestone thresholds.
- **Tertiary Accent:** `#06B6D4` (Deep Electric Teal) anchors informational metadata, code highlights, and intermediate progress states.
- **Neutrals & Text:**
  - High Contrast Pure: `#FFFFFF` (Headers, titles, critical values)
  - Mid Contrast Slate: `#94A3B8` (Body prose, default labels)
  - Low Contrast Dim: `#475569` (Inactive tabs, disabled states, structural rules)

### Translucency & Glass Tokens
- **Glass Panel Surface:** `rgba(13, 17, 31, 0.65)` layered over canvas with `backdrop-filter: blur(16px)`.
- **Glass Border / Sheen:** `rgba(255, 255, 255, 0.08)` along standard borders; top-lit borders use `rgba(255, 255, 255, 0.16)`.
- **Glow Halos:** Low-opacity, broad-spread radial glows using `rgba(0, 242, 254, 0.15)` and `rgba(139, 92, 246, 0.18)`.

## Typography

Typography balances clean structural legibility with technical precision. 

- **Primary Interface (Inter):** Applied across display headlines, instructional steps, challenge narratives, and standard controls. Tracking is tuned tighter on large display types for a modern, compact, dense feel.
- **Technical & Machine Surface (JetBrains Mono):** Anchors code snippets, telemetry statistics, time-stamps, AI token counts, difficulty markers, and status indicators.
- **Hierarchy Rules:**
  - Headers must strictly use pure white (`#FFFFFF`) to ensure uncompromised contrast over translucent glass surfaces.
  - Body text uses neutral slates to preserve typographic calm.
  - Uppercase `label-caps` must always be paired with `JetBrains Mono` and positive tracking for instantaneous telemetry scanning.

## Layout & Spacing

The layout follows a mobile-first progressive expansion architecture, calibrated specifically for touch ergonomics, one-thumb interaction zones, and edge-to-edge PWA viewports.

### Rhythm & Grid
- **Mobile Grid (< 640px):** Single-column flex flow constrained within `100vw`, utilizing `1rem` outer canvas padding (`margin-mobile`) and a strict 4-pixel base rhythm.
- **Tablet / Expanded Viewport (641px - 1024px):** Dual-column layout with fixed right-hand diagnostic telemetry and lesson sidebars; max container width 768px centered.
- **Desktop (> 1024px):** Max container width constrained to 960px to maintain focused app-like ergonomics rather than a dispersed dashboard.

### Safe Areas & Vertical Rhythm
- **Top Bar:** Fixed `56px` translucent top navigation containing streak counters, profile ring, and network sync badges, cushioned with `env(safe-area-inset-top)`.
- **Bottom Navigation Dock:** Fixed `64px` floating glass bar with `env(safe-area-inset-bottom)` buffer.
- **Scroll Margin:** All scrollable content containers must have bottom padding equal to `calc(64px + env(safe-area-inset-bottom) + 1.5rem)` to guarantee zero occlusion by the persistent bottom bar.

## Elevation & Depth

Visual hierarchy is established strictly through **optical transmittance, back-surface blurs, and localized neon edge-glows** rather than muddy black drop-shadows.

### Depth Layers
1. **Layer 0 (Canvas Void):** `#090D16` static base.
2. **Layer 1 (Sub-surface Deck):** Background gradients with slow, subtle cyber-violet glow wells (`ellipse at 50% 0%, rgba(139,92,246,0.12) 0%, transparent 60%`).
3. **Layer 2 (Interactive Floating Glass):**
   - Background: `rgba(13, 17, 31, 0.70)`
   - Backdrop Filter: `blur(16px) saturate(180%)`
   - Border: `1px solid rgba(255, 255, 255, 0.08)`
   - Top-edge highlight: `box-shadow: inset 0 1px 0 0 rgba(255, 255, 255, 0.12)`
4. **Layer 3 (Modals, Action Sheets, Floating Bars):**
   - Background: `rgba(18, 24, 43, 0.85)`
   - Backdrop Filter: `blur(24px)`
   - Border: `1px solid rgba(0, 242, 254, 0.25)`
   - Ambient Aura: `0 8px 32px -4px rgba(0, 0, 0, 0.5), 0 0 24px 0 rgba(0, 242, 254, 0.1)`

### Active & Glowing States
When an element achieves an active, selected, or completion state, its perimeter illuminates. Use double box-shadow rings: an inner crisp cyan line (`0 0 0 1px #00F2FE`) and an outer diffuse kinetic glow (`0 0 16px rgba(0, 242, 254, 0.35)`).

## Shapes

The geometric framework balances human tactile curvature with aerodynamic cyber minimalism.

- **Standard Cards & Containers:** Medium curvature (`0.5rem` / `8px` or `rounded-lg` / `16px`) provides soft containment without sacrificing content density.
- **Pill Badges & Status Tokens:** Fully rounded capsules (`9999px`) to emphasize their modular, collectible metadata identity.
- **Bottom Navigation Dock:** Contoured pill housing with rounded-top edges (`24px` radius) or fully floating stadium-capsule geometry (`32px` radius) offset from screen perimeters.
- **Interactive Buttons:** Slightly softened corners (`10px` to `12px`) matching native mobile hardware silhouettes.

## Components

### Buttons
- **Primary Cyber Action:** High-saturation background gradient (`linear-gradient(135deg, #00F2FE 0%, #06B6D4 100%)`), text in black obsidian (`#090D16`, bold), subtle external aura (`box-shadow: 0 0 14px rgba(0, 242, 254, 0.4)`). Active state applies `scale(0.97)` micro-compression.
- **Secondary Glass Button:** `rgba(255, 255, 255, 0.05)` fill, `1px solid rgba(255, 255, 255, 0.15)` border, text in `#FFFFFF`. Hover/Active state tints border to `#8B5CF6` with subtle purple specular highlight.
- **Ghost Utility Button:** Zero fill, text in `#94A3B8`, active text transitions to `#00F2FE`.

### Pill Badges & Chips
- **Category / Difficulty Badges:** Height `22px`, pill radius (`9999px`), padding `0 8px`. JetBrains Mono `code-sm`. 
  - *Beginner / Prompt:* Border `rgba(6, 182, 212, 0.3)`, background `rgba(6, 182, 212, 0.1)`, text `#00F2FE`.
  - *Advanced / Architecture:* Border `rgba(139, 92, 246, 0.3)`, background `rgba(139, 92, 246, 0.12)`, text `#A855F7`.
- **Interactive Chips (Filter/Topic):** Frosted glass pills with active border transition into electric cyan glow.

### Cards & Lesson Modules
- Frosted base surface with `12px` to `16px` padding.
- Displays lesson title, estimated runtime (e.g., `5 MIN`), XP payload, and completion status indicator.
- Bottom card edge includes a 2px micro-progress track: background `rgba(255,255,255,0.06)`, active fill in glowing cyan gradient.

### Input Fields & Code Playgrounds
- **Inputs:** Height `48px`, background `rgba(9, 13, 22, 0.6)`, border `1px solid rgba(255, 255, 255, 0.1)`. Font `body-md`. Focus state shifts border to `#00F2FE` with `box-shadow: 0 0 12px rgba(0, 242, 254, 0.2)`.
- **Code Terminal Playground:** Dark obsidian container (`#06090F`), `1px solid rgba(255, 255, 255, 0.08)`, header utility strip housing run button, language tag (`JetBrains Mono`), and execution latency indicator.

### Status Indicators & Streaks
- **Streak Flame / AI Synapse:** Micro-icon wrapped in glowing halo container with pulsing cyber-violet border.
- **Live Status Dot:** 6px circular dot with concentric animated ping ring (`rgba(0, 242, 254, 0.75)`).

### Mobile Bottom Navigation Tab Bar
- Anchored to bottom screen boundary with integrated touch feedback.
- Frosted glass substrate: `rgba(9, 13, 22, 0.85)` with `backdrop-filter: blur(20px)` and top hair-line border `rgba(255, 255, 255, 0.08)`.
- 4 to 5 tab slots: *Daily*, *Curriculum*, *Sandbox*, *Profile*.
- Inactive tabs: `#475569` icon and text.
- Active tab: `#00F2FE` icon with a persistent micro neon dot (3px) positioned directly beneath the icon, surrounded by an ambient soft blue radial flare.