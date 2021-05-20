package com.theoplayerreactnative;

import android.view.View;
import android.widget.LinearLayout;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.THEOplayerConfig;
import com.theoplayer.android.api.source.SourceDescription;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class TheoPlayerViewManager extends SimpleViewManager<THEOplayerView> implements LifecycleEventListener {

    //static
    private static final String TAG = TheoPlayerViewManager.class.getSimpleName();
    private static final String RCT_MODULE_NAME = "THEOplayerView";

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

        reactContext.addLifecycleEventListener(this);

        return playerView;
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
