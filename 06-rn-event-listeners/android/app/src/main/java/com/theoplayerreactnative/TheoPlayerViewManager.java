package com.theoplayerreactnative;

import android.view.View;
import android.widget.LinearLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.event.EventListener;
import com.theoplayer.android.api.event.player.PlayEvent;
import com.theoplayer.android.api.event.player.PlayerEventTypes;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class TheoPlayerViewManager extends SimpleViewManager<THEOplayerView> implements LifecycleEventListener { // Implement lifecycle listener
    private static final String TAG = TheoPlayerViewManager.class.getSimpleName();
    private static final String RCT_MODULE_NAME = "THEOplayerView";

    private enum InternalAndGlobalEventPair {
        onPlay("onPlayEventInternal", "onPlay");

        String internalEvent;
        String globalEvent;

        InternalAndGlobalEventPair(String internalEvent, String globalEvent) {
            this.internalEvent = internalEvent;
            this.globalEvent = globalEvent;
        }
    }

    THEOplayerView playerView;

    @Override
    public String getName() {
        return RCT_MODULE_NAME;
    }

    @Override
    protected THEOplayerView createViewInstance(final ThemedReactContext reactContext) {
        THEOplayerConfig playerConfig = new THEOplayerConfig.Builder()
              .license("your_license_string")
              .build();

        playerView = new THEOplayerView(reactContext.getCurrentActivity(), playerConfig);
        playerView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // Add change listeners
        addPropertyChangeListeners(reactContext);

        // Add lifecycle event listener to react context
        reactContext.addLifecycleEventListener(this);

        return playerView;
    }

    // Change listeners
    private void addPropertyChangeListeners(final ThemedReactContext reactContext) {
        // Add listener on video play
        playerView.getPlayer().addEventListener(PlayerEventTypes.PLAY, new EventListener<PlayEvent>() {
            @Override
            public void handleEvent(final PlayEvent playEvent) {
                WritableMap event = Arguments.createMap();

                // Local property change
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    playerView.getId(),
                    InternalAndGlobalEventPair.onPlay.internalEvent,
                    event);
            }
        });

    }

    @ReactProp(name = "autoplay", defaultBoolean = false)
    public void setAutoplay(View view, boolean autoplay) {
        playerView.getPlayer().setAutoplay(autoplay);
    }

    @ReactProp(name = "fullscreenOrientationCoupling", defaultBoolean = false)
    public void setFullscreenOrientationCoupling(View view, boolean fullscreenOrientationCoupling) {
        playerView.getSettings().setFullScreenOrientationCoupled(fullscreenOrientationCoupling);
    }

    @ReactProp(name = "source")
    public void setSource(View view, ReadableMap source) {
        SourceDescription sourceDescription = SourceHelper.parseSourceFromJS(source);
        if (sourceDescription != null) {
            playerView.getPlayer().setSource(sourceDescription);
        }
    }

    @Override
    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
            .put(
                InternalAndGlobalEventPair.onPlay.internalEvent,
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", InternalAndGlobalEventPair.onPlay.globalEvent)))
            .build();
    }

    // Lifecycle events
    @Override
    // Called either when the host activity receives a resume event or when native module that implements
    // this is initialized while the host activity is already resumed
    public void onHostResume() {
        playerView.onResume();
    }

    @Override
    // Called when host activity receives pause event
    public void onHostPause() {
        playerView.onPause();
    }

    @Override
    // Called when host activity receives destroy event
    public void onHostDestroy() {
        playerView.onDestroy();
    }
}
