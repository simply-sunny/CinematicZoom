package mix.cinematiczoom.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import mix.cinematiczoom.ZoomConfig;
import mix.cinematiczoom.ZoomCurve;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ZoomConfig cfg = ZoomConfig.INSTANCE;

            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("cinematiczoom.config.title"))
                .setSavingRunnable(cfg::save);

            ConfigEntryBuilder eb = builder.entryBuilder();

            // 1. Cinematic Zoom Category
            ConfigCategory cinematicCat = builder.getOrCreateCategory(Text.translatable("cinematiczoom.config.category.cinematic"));

            cinematicCat.addEntry(
                eb.startFloatField(Text.translatable("cinematiczoom.option.cinematic_starting_zoom"), cfg.cinematicStartingZoom)
                  .setMin(1f).setMax(30f)
                  .setTooltip(Text.translatable("cinematiczoom.option.cinematic_starting_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicStartingZoom = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startFloatField(Text.translatable("cinematiczoom.option.bars_percent"), cfg.cinematicBarsPercent)
                  .setMin(0f).setMax(50f)
                  .setTooltip(Text.translatable("cinematiczoom.option.bars_percent.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicBarsPercent = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startIntField(Text.translatable("cinematiczoom.option.zoom_in_ms"), cfg.cinematicZoomInMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_in_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomInMs = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startIntField(Text.translatable("cinematiczoom.option.zoom_out_ms"), cfg.cinematicZoomOutMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_out_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomOutMs = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startEnumSelector(Text.translatable("cinematiczoom.option.zoom_in_curve"), ZoomCurve.class, cfg.cinematicZoomInCurve)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_in_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomInCurve = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startEnumSelector(Text.translatable("cinematiczoom.option.zoom_out_curve"), ZoomCurve.class, cfg.cinematicZoomOutCurve)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_out_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomOutCurve = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.toggle_mode"), cfg.cinematicToggle)
                  .setTooltip(Text.translatable("cinematiczoom.option.toggle_mode.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicToggle = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.hide_hud"), cfg.cinematicHideHud)
                  .setTooltip(Text.translatable("cinematiczoom.option.hide_hud.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicHideHud = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.cinematic_cam"), cfg.cinematicCamera)
                  .setTooltip(Text.translatable("cinematiczoom.option.cinematic_cam.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicCamera = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.scale_sensitivity"), cfg.cinematicScaleSensitivity)
                  .setTooltip(Text.translatable("cinematiczoom.option.scale_sensitivity.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicScaleSensitivity = v)
                  .build()
            );

            // 2. Regular Zoom Category
            ConfigCategory regularCat = builder.getOrCreateCategory(Text.translatable("cinematiczoom.config.category.regular"));

            regularCat.addEntry(
                eb.startFloatField(Text.translatable("cinematiczoom.option.regular_starting_zoom"), cfg.regularStartingZoom)
                  .setMin(1f).setMax(30f)
                  .setTooltip(Text.translatable("cinematiczoom.option.regular_starting_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.regularStartingZoom = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startIntField(Text.translatable("cinematiczoom.option.zoom_in_ms"), cfg.regularZoomInMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_in_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomInMs = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startIntField(Text.translatable("cinematiczoom.option.zoom_out_ms"), cfg.regularZoomOutMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_out_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomOutMs = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startEnumSelector(Text.translatable("cinematiczoom.option.zoom_in_curve"), ZoomCurve.class, cfg.regularZoomInCurve)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_in_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomInCurve = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startEnumSelector(Text.translatable("cinematiczoom.option.zoom_out_curve"), ZoomCurve.class, cfg.regularZoomOutCurve)
                  .setTooltip(Text.translatable("cinematiczoom.option.zoom_out_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomOutCurve = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.toggle_mode"), cfg.regularToggle)
                  .setTooltip(Text.translatable("cinematiczoom.option.toggle_mode.tooltip"))
                  .setSaveConsumer(v -> cfg.regularToggle = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.hide_hud"), cfg.regularHideHud)
                  .setTooltip(Text.translatable("cinematiczoom.option.hide_hud.tooltip"))
                  .setSaveConsumer(v -> cfg.regularHideHud = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.cinematic_cam"), cfg.regularCinematicCamera)
                  .setTooltip(Text.translatable("cinematiczoom.option.cinematic_cam.tooltip"))
                  .setSaveConsumer(v -> cfg.regularCinematicCamera = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.scale_sensitivity"), cfg.regularScaleSensitivity)
                  .setTooltip(Text.translatable("cinematiczoom.option.scale_sensitivity.tooltip"))
                  .setSaveConsumer(v -> cfg.regularScaleSensitivity = v)
                  .build()
            );

            // 3. General & Wheel Category
            ConfigCategory generalCat = builder.getOrCreateCategory(Text.translatable("cinematiczoom.config.category.general"));

            generalCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.mouse_wheel_enabled"), cfg.mouseWheelEnabled)
                  .setTooltip(Text.translatable("cinematiczoom.option.mouse_wheel_enabled.tooltip"))
                  .setSaveConsumer(v -> cfg.mouseWheelEnabled = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Text.translatable("cinematiczoom.option.discrete_scroll"), cfg.discreteScroll)
                  .setTooltip(Text.translatable("cinematiczoom.option.discrete_scroll.tooltip"))
                  .setSaveConsumer(v -> cfg.discreteScroll = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startFloatField(Text.translatable("cinematiczoom.option.discrete_scroll_step"), cfg.discreteScrollStep)
                  .setMin(0.1f).setMax(5.0f)
                  .setTooltip(Text.translatable("cinematiczoom.option.discrete_scroll_step.tooltip"))
                  .setSaveConsumer(v -> cfg.discreteScrollStep = v)
                  .build()
            );

            return builder.build();
        };
    }
}
