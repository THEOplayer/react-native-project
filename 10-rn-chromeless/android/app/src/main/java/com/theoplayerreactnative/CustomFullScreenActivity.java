package com.theoplayerreactnative;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.theoplayer.android.api.event.player.DurationChangeEvent;
import com.theoplayer.android.api.event.player.LoadedDataEvent;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.event.player.ProgressEvent;
import com.theoplayer.android.api.event.player.TimeUpdateEvent;
import com.theoplayer.android.api.fullscreen.FullScreenActivity;
import com.theoplayer.android.api.fullscreen.FullScreenManager;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.internal.THEOplayerViewHolder;
import com.theoplayer.android.internal.fullscreen.FullScreenSharedContext;
import com.theoplayerreactnative.databinding.ActivityFullscreenBinding;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class CustomFullScreenActivity extends FullScreenActivity implements SeekBar.OnSeekBarChangeListener {
    private final int rewindStepLengthInSeconds = 10;
    private final int progressMaxValue = 100;

    private AppCompatDelegate appCompatDelegate;
    private ActivityFullscreenBinding viewBinding;
    private Player theoPlayer;
    private FullScreenManager theoFullScreenManager;
    private boolean isTrackingSeekBarTouch = false;
    private boolean controlsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Adding support for extended AppCompat features.
        // It allows to use styles and themes defined for material components.
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        // THEOplayerViewHolder theoPlayerView = FullScreenSharedContext.fullScreenSharedContext().mostVisibleTpv();
        // getIntent().putExtra("tpvID", theoPlayerView.getTHEOid());
        super.onCreate(savedInstanceState);

        // Inflating custom view and obtaining an instance of the binding class.
        viewBinding = ActivityFullscreenBinding.inflate(LayoutInflater.from(this), null, false);
        getDelegate().addContentView(viewBinding.getRoot(), new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // Gathering THEO objects references.
        theoPlayer = getTHEOplayerView().getPlayer();
        theoFullScreenManager = getTHEOplayerView().getFullScreenManager();

        // Configuring UI behavior.
        adjustPlayPauseButtonIcon();
        viewBinding.playPauseButton.setOnClickListener((button) -> onPlayPauseClick());
        viewBinding.exitFullScreenButton.setOnClickListener((button) -> onFullScreenExit());
        viewBinding.controlsOverlay.setOnClickListener((button) -> toggleControlsVisibility());
        viewBinding.controlsContainer.setOnClickListener((button) -> toggleControlsVisibility());
        viewBinding.replayButton.setOnClickListener((button) -> onReplayClicked());
        viewBinding.forwardButton.setOnClickListener((button) -> onForwardClicked());
        viewBinding.seekBar.setOnSeekBarChangeListener(this);

        // Configuring THEOplayer.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, (event) -> adjustPlayPauseButtonIcon());
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, (event) -> adjustPlayPauseButtonIcon());
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE, this::onTimeUpdate);
        theoPlayer.addEventListener(PlayerEventTypes.DURATIONCHANGE, this::onDurationChanged);
        theoPlayer.addEventListener(PlayerEventTypes.LOADEDDATA, this::onDataLoaded);
        theoPlayer.addEventListener(PlayerEventTypes.PROGRESS, this::onProgressEvent);
        theoPlayer.setAutoplay(true);

        viewBinding.totalTime.setText(formatTime(theoPlayer.getDuration()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    @NonNull
    public AppCompatDelegate getDelegate() {
        if (appCompatDelegate == null) {
            appCompatDelegate = AppCompatDelegate.create(this, null);
        }
        return appCompatDelegate;
    }

    private void onFullScreenExit() {
        theoFullScreenManager.exitFullScreen();
        finish();
    }

    private void onPlayPauseClick() {
        if (theoPlayer.isPaused()) {
            theoPlayer.play();
        } else {
            theoPlayer.pause();
        }
    }

    private void adjustPlayPauseButtonIcon() {
        if (theoPlayer.isPaused()) {
            viewBinding.playPauseButton.setIconResource(R.drawable.ic_play);
        } else {
            viewBinding.playPauseButton.setIconResource(R.drawable.ic_pause);
        }
    }

    private void onTimeUpdate(TimeUpdateEvent event) {
        if (!isTrackingSeekBarTouch) {
            viewBinding.progressText.setText(formatTime(event.getCurrentTime()));
            viewBinding.seekBar.setProgress((int) (event.getCurrentTime() * progressMaxValue / theoPlayer.getDuration()));
        }
    }

    private void onDurationChanged(DurationChangeEvent event) {
        viewBinding.totalTime.setText(formatTime(event.getDuration()));
    }

    private void onDataLoaded(LoadedDataEvent event) {
        viewBinding.totalTime.setText(formatTime(theoPlayer.getDuration()));
    }

    private void onProgressEvent(ProgressEvent event) {
        if (!isTrackingSeekBarTouch) {
            viewBinding.progressText.setText(formatTime(event.getCurrentTime()));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            viewBinding.progressText.setText(formatTime(getCurrentTimeByProgress(seekBar.getProgress())));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTrackingSeekBarTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        theoPlayer.setCurrentTime(getCurrentTimeByProgress(seekBar.getProgress()), () -> isTrackingSeekBarTouch = false);
    }

    private String formatTime(double doubleTimeInSeconds) {
        int totalTimeInSeconds = (int) doubleTimeInSeconds;
        int seconds = totalTimeInSeconds % 60;
        int minutes = totalTimeInSeconds / 60;
        int hours = totalTimeInSeconds / 3600;
        String result;
        if (hours > 0) {
            result = String.format("%d:%02d:%02d", hours, minutes, seconds);

        } else {
            result = String.format("%02d:%02d", minutes, seconds);
        }
        return result;
    }

    private double getCurrentTimeByProgress(double progress) {
        return theoPlayer.getDuration() * progress / progressMaxValue;
    }

    private void toggleControlsVisibility() {
        controlsVisible = !controlsVisible;
        viewBinding.controlsOverlay.setVisibility(controlsVisible ? View.VISIBLE : View.INVISIBLE);
    }

    private void onReplayClicked() {
        theoPlayer.requestCurrentTime(aDouble -> theoPlayer.setCurrentTime(aDouble - rewindStepLengthInSeconds));
    }

    private void onForwardClicked() {
        theoPlayer.requestCurrentTime(aDouble -> theoPlayer.setCurrentTime(aDouble + rewindStepLengthInSeconds));
    }
}
