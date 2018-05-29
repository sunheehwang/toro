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
import android.util.Log;
import android.view.View;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import toro.v4.Toro.GlobalScrollChangeListener;

/**
 * A Manager can be created for one {@link Activity} to manage the {@link Playable}s in it.
 *
 * @author eneim (2018/05/25).
 */
@SuppressWarnings("WeakerAccess") //
final class Manager {

  private static String TAG = "Toro:Manager";

  @NonNull final Toro toro;
  @NonNull final View decorView;
  // Indicate if the Container is scrolling/flinging or not.
  final AtomicBoolean scrolling = new AtomicBoolean(false);
  final AtomicBoolean attachFlag;
  final int maxConcurrentPlayers = 1;

  // Map of all Playback whose key is a WeakReference of the Target.
  // (Since Target can be a View, it should be kept as WeakReference).
  @NonNull private final WeakHashMap<Object, Playback> mapTargetToPlayback;

  // Manage playback state of Playback that has valid tag
  @NonNull private final HashMap<Object, PlaybackInfo> mapPlayableTagToInfo;

  // Manage playbacks whose Targets are attached to Window
  @NonNull private final HashMap<Playback, Long> mapAttachedPlaybackToTime;

  // Manage playbacks whose Targets are detached from Window, but may be reattached later.
  @NonNull private final HashMap<Playback, Long> mapDetachedPlaybackToTime;

  // Candidates to hold playbacks for a refresh call.
  final Map<Playback.LocToken, Playback> candidates = new TreeMap<>(Utils.CENTER_Y);

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
    TAG = TAG + "@" + hashCode();
  }

  /* BEGIN Manager lifecycle */

  // Once created, the Activity bound to this Manager may have some saved state and want to provide.
  void providePlayableInfoBundle(@NonNull Bundle cache) {
    Object state = cache.getSerializable(Toro.KEY_MANAGER_STATES);
    if (state instanceof HashMap) {
      //noinspection unchecked
      mapPlayableTagToInfo.putAll((Map<?, ? extends PlaybackInfo>) state);
    }
  }

  @NonNull Bundle fetchPlayableInfoBundle() {
    Bundle bundle = new Bundle();
    for (Playback playback : mapTargetToPlayback.values()) {
      if (playback.validTag()) {
        mapPlayableTagToInfo.put(playback.getTag(), playback.getPlaybackInfo());
      }
    }

    bundle.putSerializable(Toro.KEY_MANAGER_STATES, mapPlayableTagToInfo);
    return bundle;
  }

  // Get called when the DecorView is attached to Window
  void onAttached() {
    Log.d(TAG, "onAttached() called");
    if (attachFlag.compareAndSet(false, true)) {
      // Do something on the first time this Manager is attached.
      if (this.scrollChangeListener == null) {
        this.scrollChangeListener = new GlobalScrollChangeListener(this);
        this.decorView.getViewTreeObserver().addOnScrollChangedListener(scrollChangeListener);
      }

      if (mapAttachedPlaybackToTime.size() > 0 && !isScrolling()) this.performRefreshAll();
    }
  }

  // Get called when the DecorView is detached from Window
  void onDetached() {
    Log.d(TAG, "onDetached() called");
    if (attachFlag.compareAndSet(true, false)) {
      // Do something on the first time this Manager is detached.
      if (this.scrollChangeListener != null) {
        this.decorView.getViewTreeObserver().removeOnScrollChangedListener(scrollChangeListener);
        this.scrollChangeListener = null;
      }

      Iterator<Map.Entry<Object, Playback>> iterator = mapTargetToPlayback.entrySet().iterator();
      while (iterator.hasNext()) {
        Playback playback = iterator.next().getValue();
        cleanUpPlayback(playback);
        iterator.remove();
      }
    }
  }

  // Called when the Activity bound to this Manager is started.
  void onStart() {
    Log.d(TAG, "onStart() called");
    if (mapAttachedPlaybackToTime.size() > 0) {
      for (Playback playback : mapAttachedPlaybackToTime.keySet()) {
        // playback.onActive();
        restorePlayableState(playback);
      }
      performRefreshAll();
    }
  }

  // Called when the Activity bound to this Manager is stopped.
  void onStop() {
    Log.d(TAG, "onStop() called");
    for (Playback playback : mapAttachedPlaybackToTime.keySet()) {
      playback.pause();
      savePlayableState(playback);
    }
  }

  void onDestroy() {
    Log.d(TAG, "onDestroy() called");
    // TODO may need to do something more.
    this.onDetached();
  }

  /* END Manager lifecycle */

  void setScrolling(boolean scrolling) {
    Log.d(TAG, "setScrolling() called with: scrolling = [" + scrolling + "]");
    this.scrolling.set(scrolling);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")  //
  boolean isScrolling() {
    Log.d(TAG, "isScrolling() called");
    return this.scrolling.get();
  }

  void restorePlayableState(@NonNull Playback playback) {
    Log.d(TAG, "restorePlayableState() called with: playback = [" + playback + "]");
    if (playback.validTag()) {
      PlaybackInfo playbackInfo = mapPlayableTagToInfo.get(playback.getTag());
      if (playbackInfo != null) {
        playback.setPlaybackInfo(playbackInfo);
      }
    }
  }

  void savePlayableState(@NonNull Playback playback) {
    Log.d(TAG, "savePlayableState() called with: playback = [" + playback + "]");
    if (playback.validTag()) {
      mapPlayableTagToInfo.put(playback.getTag(), playback.getPlaybackInfo());
    }
  }

  // Important. Do the refresh stuff. Change the playback items, etc.
  void performRefreshAll() {
    Log.d(TAG, "performRefreshAll() called");
    candidates.clear();
    // List of all possible candidates.
    ArrayList<Playback> playbacks = new ArrayList<>(mapAttachedPlaybackToTime.keySet());
    for (Playback playback : playbacks) {
      Playback.LocToken token = playback.getLocToken();
      // Doing this will sort the playback using LocToken's center Y value.
      if (token != null) candidates.put(token, playback);
    }

    LimitedArrayList<Playback> toPlay = new LimitedArrayList<>(candidates.values());
    int count = toPlay.size();
    if (maxConcurrentPlayers < count) toPlay.removeRange(maxConcurrentPlayers, count);

    // Make sure the size of playback items.
    Utils.checkSize(toPlay.size(), maxConcurrentPlayers);

    playbacks.removeAll(toPlay);  // Keep only non-play-candidate ones, and pause them.
    for (Playback playback : playbacks) if (playback.isPlaying()) playback.pause();
    // Now kick play the play-candidate
    if (!isScrolling()) {
      for (Playback playback : toPlay) {
        if (!playback.isPlaying()) playback.play();
      }
    }
    // Clean up cache
    candidates.clear();
  }

  // Cancel the behavior of a specific target. Called by GC thread.
  void cancel(Object target) {
    Log.d(TAG, "cancel() called with: target = [" + target + "]");
    Playback playback = mapTargetToPlayback.remove(target);
    if (playback != null) {
      removePlayback(playback);
    }
  }

  /**
   * Once {@link Playable#into(PlayerView)} is called, it will create a new {@link Playback} object
   * response to that Target. {@link Manager} will then add that {@link Playback} to cache for management.
   * Old {@link Playback} will be cleaned up and removed.
   */
  <T> Playback<T> addPlayback(@NonNull Playback<T> playback) {
    Log.d(TAG, "addPlayback() called with: playback = [" + playback + "]");
    T target = ToroUtil.checkNotNull(playback).getTarget();
    boolean shouldQueue = target != null; // playback must have a valid target.
    if (shouldQueue) {
      // Not-null target may be already a target of another Playback before.
      // Here we also make sure if we need to torn that old Playback down first or not.
      Playback cache = this.mapTargetToPlayback.get(target);
      shouldQueue = cache == null || cache != playback;
    }

    if (shouldQueue) {
      Playback oldPlayback = this.mapTargetToPlayback.put(target, playback);
      playback.onAdded();
      restorePlayableState(playback);
      if (oldPlayback != null) {  // if null --> we must clean this up, it must no longer exist
        if (mapAttachedPlaybackToTime.remove(oldPlayback) != null) {
          throw new IllegalStateException("Unstable state: attached playback: " //
              + oldPlayback + " is replaced by " + playback);
        } else {
          if (mapDetachedPlaybackToTime.remove(oldPlayback) == null) {
            // Old playback is in detached cache, we clean its resource.
            oldPlayback.onInActive();
          }
          oldPlayback.onRemoved();
        }
      }
    }

    if (mapAttachedPlaybackToTime.containsKey(playback)) {
      // Check this newly added playback for immediate refresh.
      if (shouldQueue && playback.getLocToken() != null) performRefreshAll();
    }

    return playback;
  }

  private void cleanUpPlayback(Playback playback) {
    Log.d(TAG, "cleanUpPlayback() called with: playback = [" + playback + "]");
    savePlayableState(playback);
    playback.pause();
    playback.onInActive();
    playback.onRemoved();
  }

  // Permanently remove the Playback from any cache.
  // Notice: never call this inside an iteration of the maps below:
  // - mapTargetToPlayback
  // - mapAttachedPlaybackToTime
  // - mapDetachedPlaybackToTime
  // - mapPlayableTagToInfo
  void removePlayback(Playback playback) {
    Log.d(TAG, "removePlayback() called with: playback = [" + playback + "]");
    mapAttachedPlaybackToTime.remove(playback);
    mapDetachedPlaybackToTime.remove(playback);
    mapTargetToPlayback.remove(playback.getTarget());
    cleanUpPlayback(playback);
    // mapPlayableTagToInfo.remove(playback.getTag());
  }

  // Called when a Playback's target is attached. Eg: PlayerView is attached to window.
  <T> void onTargetActive(T target) {
    Log.d(TAG, "onTargetActive() called with: target = [" + target + "]");
    long now = System.nanoTime();
    Playback playback = mapTargetToPlayback.get(target);
    if (playback != null) {
      mapAttachedPlaybackToTime.put(playback, now);
      mapDetachedPlaybackToTime.remove(playback);
      playback.onActive();
      performRefreshAll();
    } else {
      throw new IllegalStateException("No Playback found for target.");
    }
  }

  // Called when a Playback's target is detached. Eg: PlayerView is detached from window.
  <T> void onTargetInActive(T target) {
    Log.d(TAG, "onTargetInActive() called with: target = [" + target + "]");
    long now = System.nanoTime();
    Playback playback = mapTargetToPlayback.get(target);
    if (playback != null) {
      mapDetachedPlaybackToTime.put(playback, now);
      mapAttachedPlaybackToTime.remove(playback);
      performRefreshAll();
      playback.onInActive();
    }
  }

  // Called when something has changed about the Playback. Eg: playback's target has layout change.
  <T> void onPlaybackInternalChanged(Playback<T> playback) {
    Log.d(TAG, "onPlaybackInternalChanged() called with: playback = [" + playback + "]");
    if (playback.getLocToken() != null) performRefreshAll();
  }
}
