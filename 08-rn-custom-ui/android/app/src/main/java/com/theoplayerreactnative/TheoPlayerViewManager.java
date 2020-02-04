package com.theoplayerreactnative;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.THEOplayerConfig;
import com.theoplayer.android.api.event.EventListener;
import com.theoplayer.android.api.event.player.PauseEvent;
import com.theoplayer.android.api.event.player.PlayEvent;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.event.player.SeekedEvent;
import com.theoplayer.android.api.source.SourceDescription;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class TheoPlayerViewManager extends SimpleViewManager<THEOplayerView> implements LifecycleEventListener {

    //static
    private static final String TAG = TheoPlayerViewManager.class.getSimpleName();
    private static final String RCT_MODULE_NAME = "THEOplayerView";

    private enum InternalAndGlobalEventPair {
        onSeek("onSeekEventInternal", "onSeek"),
        onPlay("onPlayEventInternal", "onPlay"),
        onPause("onPauseEventInternal", "onPause");

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
        /*
          If you want to use Google Ima set googleIma in theoplayer config and add `integration: "google-ima"`
          in js ads source declaration.
          You can declarate in THEOplayer configuration builder default js and css paths by using cssPaths() and jsPaths()
        */
        THEOplayerConfig playerConfig = new THEOplayerConfig.Builder()
                // .googleIma(true)
                .build();

        playerView = new THEOplayerView(reactContext.getCurrentActivity(), playerConfig);
        playerView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        addPropertyChangeListeners(reactContext);
        reactContext.addLifecycleEventListener(this);

        return playerView;
    }

    private void addPropertyChangeListeners(final ThemedReactContext reactContext) {
        playerView.getPlayer().addEventListener(PlayerEventTypes.SEEKED, new EventListener<SeekedEvent>() {
            @Override
            public void handleEvent(final SeekedEvent seekedEvent) {
                Log.d(TAG, "seeked native: " + seekedEvent);
                WritableMap event = Arguments.createMap();
                event.putDouble("currentTime", seekedEvent.getCurrentTime());
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        playerView.getId(),
                        InternalAndGlobalEventPair.onSeek.internalEvent,
                        event);
            }
        });

        playerView.getPlayer().addEventListener(PlayerEventTypes.PLAY, new EventListener<PlayEvent>() {
            @Override
            public void handleEvent(final PlayEvent playEvent) {
                Log.d(TAG, "play native");
                WritableMap event = Arguments.createMap();

                //local property change
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        playerView.getId(),
                        InternalAndGlobalEventPair.onPlay.internalEvent,
                        event);
            }
        });

        playerView.getPlayer().addEventListener(PlayerEventTypes.PAUSE, new EventListener<PauseEvent>() {
            @Override
            public void handleEvent(final PauseEvent pauseEvent) {
                Log.d(TAG, "pause native");
                WritableMap event = Arguments.createMap();
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        playerView.getId(),
                        InternalAndGlobalEventPair.onPause.internalEvent,
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

    @ReactProp(name = "defaultCssPaths")
    public void setDefaultCssPaths(View view, @Nullable ReadableArray defaultCssPaths) {
        try {
            Field declaredField = playerView.getClass().getDeclaredField("stateWrapper");
            declaredField.setAccessible(true);
            THEOplayerView.StateWrapper wrapper = (THEOplayerView.StateWrapper) declaredField.get(playerView);
            THEOplayerConfig config = wrapper.config;
            Field cssPathsField = config.getClass().getDeclaredField("cssPaths");
            cssPathsField.setAccessible(true);
            List<String> cssPaths = (List<String>) cssPathsField.get(config);

            for (Object o : defaultCssPaths.toArrayList()) {
                cssPaths.add((String) o);
            }

        } catch (Exception exception) {
            Log.e("CSS PATHS", "Error: " + exception);
        }
    }


    @ReactProp(name = "defaultJsPaths")
    public void setDefaultJsPaths(View view, @Nullable ReadableArray defaultJsPaths) {
        try {
            Field declaredField = playerView.getClass().getDeclaredField("stateWrapper");
            declaredField.setAccessible(true);
            THEOplayerView.StateWrapper wrapper = (THEOplayerView.StateWrapper) declaredField.get(playerView);
            THEOplayerConfig config = wrapper.config;
            Field jsPathsField = config.getClass().getDeclaredField("jsPaths");
            jsPathsField.setAccessible(true);
            List<String> jsPaths = (List<String>) jsPathsField.get(config);

            for (Object o : defaultJsPaths.toArrayList()) {
               jsPaths.add((String) o);
            }

            /*
                Evaluate main script function declarated in theoplayer.js(custom js)
                You can init pure js code without file by evaluateJavaScript.
             */
            playerView.evaluateJavaScript("init({player: player})", null);

        } catch (Exception exception) {
            Log.e("JS PATHS", "Error: " + exception);
        }
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put(
                        InternalAndGlobalEventPair.onSeek.internalEvent,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", InternalAndGlobalEventPair.onSeek.globalEvent)))
                .put(
                        InternalAndGlobalEventPair.onPlay.internalEvent,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", InternalAndGlobalEventPair.onPlay.globalEvent)))
                .put(
                        InternalAndGlobalEventPair.onPause.internalEvent,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", InternalAndGlobalEventPair.onPause.globalEvent)))
                .build();
    }

    //lifecycle events
    @Override
    public void onHostResume() {
        playerView.onResume();
    }

    @Override
    public void onHostPause() {
        playerView.onPause();
    }

    @Override
    public void onHostDestroy() {
        playerView.onDestroy();
    }

}
