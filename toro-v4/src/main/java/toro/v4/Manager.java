/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package toro.v4;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.media.PlaybackInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import toro.v4.Toro.GlobalScrollChangeListener;

import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * A Manager can be created for one {@link Activity} to manage the {@link Playback}s in it.
 *
 * @author eneim (2018/05/25).
 */
@SuppressWarnings("WeakerAccess") //
final class Manager {

  static Comparator<Playback.Token> TOKEN_COMPARATOR = new Comparator<Playback.Token>() {
    @Override public int compare(Playback.Token o1, Playback.Token o2) {
      return o1.compareTo(o2);
    }
  };

  @NonNull final Toro toro;
  @NonNull final View decorView;
  // Indicate if the Container is scrolling/flinging or not.
  final AtomicBoolean scrolling = new AtomicBoolean(false);
  final AtomicBoolean attachFlag;
  final int maxConcurrentPlayers = 1;

  private Dispatcher dispatcher;

  // It is this Manager's response to clean this.
  @NonNull final HashMap<Playable, Object> mapPlayableToTarget;

  // Map WeakReference of the Target to the Playback.
  @NonNull final WeakHashMap<Object, Playback> mapTargetToPlayback;

  // Manage playback state of Playback that has valid tag
  @NonNull private final HashMap<Object, PlaybackInfo> mapPlayableTagToInfo;

  // Manage playbacks whose Targets are attached to manager
  @NonNull private final HashMap<Playback, Long> mapAttachedPlaybackToTime;

  // Manage playbacks whose Targets are detached from manager, but could be reattached later.
  @NonNull private final HashMap<Playback, Long> mapDetachedPlaybackToTime;

  final ArrayList<Playable> playablesThisActiveTo = new ArrayList<>();

  // Candidates to hold playbacks for a refresh call.
  private final TreeMap<Playback.Token, Playback> candidates = new TreeMap<>(TOKEN_COMPARATOR);

  // Observe the scroll in ViewTreeObserver.
  private GlobalScrollChangeListener scrollChangeListener;

  @SuppressWarnings("unused") //
  Manager(@NonNull Toro toro, @NonNull View decorView) {
    this.toro = toro;
    this.decorView = decorView;
    this.attachFlag = new AtomicBoolean(false);
    this.mapTargetToPlayback = new WeakHashMap<>();
    this.mapPlayableTagToInfo = new HashMap<>();
    this.mapAttachedPlaybackToTime = new HashMap<>();
    this.mapDetachedPlaybackToTime = new HashMap<>();
    this.mapPlayableToTarget = new HashMap<>();
  }

  /* [BEGIN] Manager lifecycle */

  // Once created, the Activity bound to this Manager may have some saved state and want to provide.
  void onInitialized(@Nullable Bundle cache) {
    if (cache == null) return;
    Object state = cache.getSerializable(Toro.KEY_MANAGER_STATES);
    if (state instanceof HashMap) {
      //noinspection unchecked
      mapPlayableTagToInfo.putAll((Map<?, ? extends PlaybackInfo>) state);
    }
  }

  /*
    Called when the Activity bound to this Manager is started.

    [Note, 20180622] If an Activity (say Activity A1) is started by another Activity (say Activity A0),
    the following lifecycle events will be executed:
    A0@onPause() --> A1@onCreate() --> A1@onStart() --> A1@onPostCreate() --> A1@onResume -->
    A1@onPostResume --> A0@onStop --> A0@onSaveInstanceState.
    Therefore, handling Manager activeness of Playable requires some order handling.
   */
  void onStart() {
    for (Playback playback : mapAttachedPlaybackToTime.keySet()) {
      playback.playable.mayUpdateStatus(this, true);
      playback.onActive();
    }
    this.dispatchRefreshAll();
  }

  // Get called when the DecorView is attached to Window
  void onAttached() {
    if (dispatcher == null) dispatcher = new Dispatcher(this);

    if (attachFlag.compareAndSet(false, true)) {
      // Do something on the first time this Manager is attached.
      if (this.scrollChangeListener == null) {
        this.scrollChangeListener = new GlobalScrollChangeListener(this);
        this.decorView.getViewTreeObserver().addOnScrollChangedListener(scrollChangeListener);
      }

      // Has attached playbacks, and not scrolling, then try refreshing everything.
      if (mapAttachedPlaybackToTime.size() > 0 && !isScrolling()) this.dispatchRefreshAll();
    }
  }

  // Get called when the DecorView is detached from Window
  void onDetached() {
    if (attachFlag.compareAndSet(true, false)) {
      // Do something on the first time this Manager is detached.
      if (this.scrollChangeListener != null) {
        this.decorView.getViewTreeObserver().removeOnScrollChangedListener(scrollChangeListener);
        this.scrollChangeListener = null;
      }
    }

    if (dispatcher != null) {
      dispatcher.removeCallbacksAndMessages(null);
      dispatcher = null;
    }
  }

  // Called when the Activity bound to this Manager is stopped.
  void onStop(boolean configChange) {
    for (Playback playback : mapAttachedPlaybackToTime.keySet()) {
      playback.onPause(configChange);
      playback.playable.mayUpdateStatus(this, configChange);
    }
    // Put it here for future warning.
    // [20180620] Don't call this, as it may change the reason we pause the playback.
    // performRefreshAll();
  }

  @NonNull Bundle onSavePlaybackInfo() {
    Bundle bundle = new Bundle();
    for (Playback playback : mapTargetToPlayback.values()) {
      if (playback.validTag()) {
        mapPlayableTagToInfo.put(playback.getTag(), playback.playable.getPlaybackInfo());
      }
    }

    bundle.putSerializable(Toro.KEY_MANAGER_STATES, mapPlayableTagToInfo);
    return bundle;
  }

  void onDestroy(boolean recreating) {
    for (Playback playback : mapTargetToPlayback.values()) {
      preparePlaybackDestroy(playback, recreating);
    }
    mapTargetToPlayback.clear();
    mapPlayableToTarget.clear();
  }

  /* [END] Manager lifecycle */

  void setScrolling(boolean scrolling) {
    this.scrolling.set(scrolling);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")  //
  boolean isScrolling() {
    return this.scrolling.get();
  }

  // called when a Playback becomes active, to restore last playback information.
  void restorePlayableState(@NonNull Playback playback) {
    if (playback.validTag()) {
      PlaybackInfo playbackInfo = mapPlayableTagToInfo.get(playback.getTag());
      if (playbackInfo != null) {
        playback.playable.setPlaybackInfo(playbackInfo);
      }
    }
  }

  void savePlayableState(@NonNull Playback playback) {
    if (playback.validTag()) {
      mapPlayableTagToInfo.put(playback.getTag(), playback.playable.getPlaybackInfo());
    }
  }

  // Important. Do the refresh stuff. Change the playback items, etc.
  void performRefreshAll() {
    candidates.clear();
    // List of all possible candidates.
    ArrayList<Playback> playbacks = new ArrayList<>(mapAttachedPlaybackToTime.keySet());
    for (Playback playback : playbacks) {
      Playback.Token token = playback.getToken();
      // Doing this will sort the playback using LocToken's center Y value.
      if (token != null) candidates.put(token, playback);
    }

    LimitedArrayList<Playback> toPlay = new LimitedArrayList<>(candidates.values());
    int count = toPlay.size();
    if (maxConcurrentPlayers < count) toPlay.removeRange(maxConcurrentPlayers, count);

    // Make sure the size of playback items.
    Utils.checkSize(toPlay.size(), maxConcurrentPlayers);

    playbacks.removeAll(toPlay);
    playbacks.addAll(mapDetachedPlaybackToTime.keySet());
    // Keep only non-play-candidate ones, and pause them.
    for (Playback playback : playbacks) {
      playback.onPause(false);
    }
    // Now kick play the play-candidate
    if (!isScrolling()) {
      for (Playback playback : toPlay) {
        playback.onPlay();
      }
    }
    // Clean up cache
    candidates.clear();
  }

  // Cancel the behavior of a specific target. Called by GC thread.
  void cancel(Object target) {
    Playback playback = mapTargetToPlayback.remove(target);
    if (playback != null) {
      removePlayback(playback);
    }
  }

  /**
   * Once {@link Playable#bind(PlayerView)} is called, it will create a new {@link Playback} object
   * to manage the Target. {@link Manager} will then add that {@link Playback} to cache for management.
   * Old {@link Playback} will be cleaned up and removed.
   */
  @SuppressWarnings("UnusedReturnValue")  //
  <T> Playback<T> addPlayback(@NonNull Playback<T> playback) {
    T target = checkNotNull(playback).getTarget();
    boolean shouldQueue = target != null; // playback must have a valid target.
    if (shouldQueue) {
      // Not-null target may be already a target of another Playback before.
      // Here we also make sure if we need to torn that old Playback down first or not.
      Playback cache = this.mapTargetToPlayback.get(target);
      shouldQueue = cache == null || cache != playback;
    }

    if (shouldQueue) {
      Playback oldPlayback = this.mapTargetToPlayback.put(target, playback);
      if (oldPlayback != null) {  // if not null --> we must clean this up, it must no longer exist
        if (mapAttachedPlaybackToTime.remove(oldPlayback) != null) {
          throw new RuntimeException("Old playback is still attached. This should not happen ...");
        } else {
          if (mapDetachedPlaybackToTime.remove(oldPlayback) != null) {
            // Old playback is in detached cache, we clean its resource.
            oldPlayback.onInActive();
          }
          oldPlayback.onRemoved(false);
        }
      }
      playback.playable.mayUpdateStatus(this, true);
      playback.onAdded();
    }

    // In case we are adding nothing new, and the playback is already there.
    if (mapAttachedPlaybackToTime.containsKey(playback)) {
      // shouldQueue is true when the target is not null and no pre-exist playback.
      if (shouldQueue && playback.getToken() != null) this.dispatchRefreshAll();
    }

    return playback;
  }

  private void preparePlaybackDestroy(Playback playback, boolean recreating) {
    // savePlayableState(playback);
    playback.onPause(recreating);
    playback.onInActive();
    playback.onRemoved(recreating);
  }

  // Permanently remove the Playback from any cache.
  // Notice: never call this inside an iteration of the maps below:
  // - mapTargetToPlayback
  // - mapAttachedPlaybackToTime
  // - mapDetachedPlaybackToTime
  // - mapPlayableTagToInfo
  void removePlayback(Playback playback) {
    mapAttachedPlaybackToTime.remove(playback);
    mapDetachedPlaybackToTime.remove(playback);
    mapTargetToPlayback.remove(playback.getTarget());
    preparePlaybackDestroy(playback, false);
    // mapPlayableTagToInfo.remove(playback.getTag());
  }

  // Called when a Playback's target is attached. Eg: PlayerView is attached to window.
  <T> void onTargetActive(T target) {
    long now = System.nanoTime();
    Playback playback = mapTargetToPlayback.get(target);
    if (playback != null) {
      // TODO [20180620] double check if we should restore state of this Playback or not.
      // restorePlayableState(playback);
      mapAttachedPlaybackToTime.put(playback, now);
      mapDetachedPlaybackToTime.remove(playback);
      playback.onActive();
      this.dispatchRefreshAll();
    } else {
      throw new IllegalStateException("No Playback found for target.");
    }
  }

  // Called when a Playback's target is detached. Eg: PlayerView is detached from window.
  <T> void onTargetInActive(T target) {
    long now = System.nanoTime();
    Playback playback = mapTargetToPlayback.get(target);
    if (playback != null) {
      // TODO [20180620] double check if we should save state of this Playback or not.
      // savePlayableState(playback);
      mapDetachedPlaybackToTime.put(playback, now);
      mapAttachedPlaybackToTime.remove(playback);
      playback.onPause(false);
      this.dispatchRefreshAll();
      playback.onInActive();
    }
  }

  // Called when something has changed about the Playback. Eg: playback's target has layout change.
  <T> void onPlaybackInternalChanged(Playback<T> playback) {
    if (playback.getToken() != null) this.dispatchRefreshAll();
  }

  //// Using Dispatcher

  void dispatchRefreshAll() {
    if (dispatcher != null) dispatcher.dispatchRefreshAll();
  }
}
