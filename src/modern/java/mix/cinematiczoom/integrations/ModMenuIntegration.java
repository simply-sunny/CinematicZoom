package mix.cinematiczoom.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import mix.cinematiczoom.ZoomConfig;
import mix.cinematiczoom.ZoomCurve;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ZoomConfig cfg = ZoomConfig.INSTANCE;

            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("cinematiczoom.config.title"))
                .setSavingRunnable(cfg::save);

            ConfigEntryBuilder eb = builder.entryBuilder();

            // 1. Cinematic Zoom Category
            ConfigCategory cinematicCat = builder.getOrCreateCategory(Component.translatable("cinematiczoom.config.category.cinematic"));

            cinematicCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.cinematic_starting_zoom"), cfg.cinematicStartingZoom)
                  .setMin(1f).setMax(20f)
                  .setTooltip(Component.translatable("cinematiczoom.option.cinematic_starting_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicStartingZoom = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.bars_percent"), cfg.cinematicBarsPercent)
                  .setMin(0f).setMax(50f)
                  .setTooltip(Component.translatable("cinematiczoom.option.bars_percent.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicBarsPercent = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_in_ms"), cfg.cinematicZoomInMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomInMs = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_out_ms"), cfg.cinematicZoomOutMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomOutMs = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_in_curve"), ZoomCurve.class, cfg.cinematicZoomInCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomInCurve = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_out_curve"), ZoomCurve.class, cfg.cinematicZoomOutCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomOutCurve = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.toggle_mode"), cfg.cinematicToggle)
                  .setTooltip(Component.translatable("cinematiczoom.option.toggle_mode.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicToggle = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_hud"), cfg.cinematicHideHud)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_hud.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicHideHud = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.cinematic_cam"), cfg.cinematicCamera)
                  .setTooltip(Component.translatable("cinematiczoom.option.cinematic_cam.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicCamera = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.scale_sensitivity"), cfg.cinematicScaleSensitivity)
                  .setTooltip(Component.translatable("cinematiczoom.option.scale_sensitivity.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicScaleSensitivity = v)
                  .build()
            );

            // 2. Regular Zoom Category
            ConfigCategory regularCat = builder.getOrCreateCategory(Component.translatable("cinematiczoom.config.category.regular"));

            regularCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.regular_starting_zoom"), cfg.regularStartingZoom)
                  .setMin(1f).setMax(20f)
                  .setTooltip(Component.translatable("cinematiczoom.option.regular_starting_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.regularStartingZoom = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_in_ms"), cfg.regularZoomInMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomInMs = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_out_ms"), cfg.regularZoomOutMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomOutMs = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_in_curve"), ZoomCurve.class, cfg.regularZoomInCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomInCurve = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_out_curve"), ZoomCurve.class, cfg.regularZoomOutCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomOutCurve = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.toggle_mode"), cfg.regularToggle)
                  .setTooltip(Component.translatable("cinematiczoom.option.toggle_mode.tooltip"))
                  .setSaveConsumer(v -> cfg.regularToggle = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_hud"), cfg.regularHideHud)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_hud.tooltip"))
                  .setSaveConsumer(v -> cfg.regularHideHud = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.cinematic_cam"), cfg.regularCinematicCamera)
                  .setTooltip(Component.translatable("cinematiczoom.option.cinematic_cam.tooltip"))
                  .setSaveConsumer(v -> cfg.regularCinematicCamera = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.scale_sensitivity"), cfg.regularScaleSensitivity)
                  .setTooltip(Component.translatable("cinematiczoom.option.scale_sensitivity.tooltip"))
                  .setSaveConsumer(v -> cfg.regularScaleSensitivity = v)
                  .build()
            );

            // 3. General & Wheel Category
            ConfigCategory generalCat = builder.getOrCreateCategory(Component.translatable("cinematiczoom.config.category.general"));

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.mouse_wheel_enabled"), cfg.mouseWheelEnabled)
                  .setTooltip(Component.translatable("cinematiczoom.option.mouse_wheel_enabled.tooltip"))
                  .setSaveConsumer(v -> cfg.mouseWheelEnabled = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.discrete_scroll"), cfg.discreteScroll)
                  .setTooltip(Component.translatable("cinematiczoom.option.discrete_scroll.tooltip"))
                  .setSaveConsumer(v -> cfg.discreteScroll = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.discrete_scroll_step"), cfg.discreteScrollStep)
                  .setMin(0.1f).setMax(5.0f)
                  .setTooltip(Component.translatable("cinematiczoom.option.discrete_scroll_step.tooltip"))
                  .setSaveConsumer(v -> cfg.discreteScrollStep = v)
                  .build()
            );

            return builder.build();
        };
    }
}
