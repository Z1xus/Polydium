package org.polyfrost.polydium.client;

import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public class PolydiumToggles {
    //? if >= 1.21.10
    @Switch(title = "Deferred Sky", description = "Draws the sky after terrain so hidden sky pixels are skipped.")
    public static boolean deferredSky = true;
    //? if >= 1.21.8
    @Switch(title = "GUI Item Baking", description = "Redraws animated GUI items only when their animation actually moves.")
    public static boolean guiItemBake = true;
    //? if > 1.8.9
    @Switch(title = "Particle Section Culling", description = "Skips particles in chunk sections Sodium has culled.")
    public static boolean particleSectionCull = true;
    //? if > 1.8.9
    @Switch(title = "Larger Section Copy Cache", description = "Keeps more copied chunk sections for meshing. Uses more memory.")
    public static boolean sectionCacheSize = true;
    //? if >= 1.21.10
    @Switch(title = "Debug Entry Cache", description = "Refreshes F3 debug entries on an interval instead of every frame.")
    public static boolean debugEntryCache = true;
    @Switch(title = "Sound Listener Throttle", description = "Stops sending unchanged listener position updates to the sound thread.")
    public static boolean soundListenerThrottle = true;
    //? if = 1.8.9
    //@Switch(title = "Plain Translation Fast Path", description = "Skips string formatting for translations that have nothing to format.")
    public static boolean plainTranslation = true;
    //? if > 1.8.9 && < 26.1
    //@Switch(title = "Precise Frame Limiter", description = "Replaces the vanilla FPS limiter with a more accurate one implemented in MC 26.1+.")
    public static boolean preciseFrameLimiter = true;
    //? if > 1.8.9
    @Switch(title = "Hide Level Loading Screen", description = "Stops drawing the level loading screen once the world is loaded.")
    public static boolean hideLevelLoadingScreen = true;
    //? if > 1.8.9
    @Switch(title = "Hide Reload Overlay")
    public static boolean hideReloadOverlay = true;
    //? if > 1.8.9
    @Switch(title = "Hide Title Fade")
    public static boolean hideTitleFade = true;
    //? if > 1.8.9
    @Switch(title = "Free Sprite Mipmaps", description = "Frees generated sprite mipmaps from the CPU once atlas is uploaded. Applies on next resource reload.")
    public static boolean spriteMipRelease = true;
    //? if > 1.8.9
    @Switch(title = "Merge Text Decorations", description = "Draws each underlined or strikethrough run of text as one quad instead of one per character.")
    public static boolean textEffectMerge = true;
    //? if > 1.8.9 && < 1.21.4
    //@Switch(title = "Single-Pass Text Shadows", description = "Draws text shadows in the same pass as the text, as MC 1.21.4+ does.")
    public static boolean singlePassTextShadow = true;
}
