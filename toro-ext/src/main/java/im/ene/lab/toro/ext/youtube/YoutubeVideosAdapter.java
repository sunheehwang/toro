/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.lab.toro.ext.youtube;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eneim on 4/8/16.
 *
 * Youtube Video Manager + Video Adapter. This is the core of Youtube Support part.
 */
public abstract class YoutubeVideosAdapter extends ToroAdapter<YoutubeViewHolder>
    implements VideoPlayerManager //
    /* , YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener */ {

  final FragmentManager mFragmentManager;
  // private final VideoPlayerManager delegate;
  YouTubePlayer mYoutubePlayer;

  private static final int MESSAGE_PLAYBACK_PROGRESS = 1;

  private final Map<String, Long> mVideoStates = new HashMap<>();

  private ToroPlayer mPlayer;
  // This Handler will send Message to Main Thread
  private Handler mUiHandler;
  private Handler.Callback mCallback = new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_PLAYBACK_PROGRESS:
          if (mPlayer != null) {
            mPlayer.onPlaybackProgress(mPlayer.getCurrentPosition(), mPlayer.getDuration());
          }
          mUiHandler.removeMessages(MESSAGE_PLAYBACK_PROGRESS);
          mUiHandler.sendEmptyMessageDelayed(MESSAGE_PLAYBACK_PROGRESS, 250);
          return true;
        default:
          return false;
      }
    }
  };

  public YoutubeVideosAdapter(FragmentManager fragmentManager) {
    super();
    this.mFragmentManager = fragmentManager;
    // this.delegate = new VideoPlayerManagerImpl();
  }

  /**
   * @return latest Video Player
   */
  @Override public ToroPlayer getPlayer() {
    return mPlayer;
  }

  /**
   * Set current video player. There would be at most one Video player at a time.
   *
   * @param player the current Video Player of this manager
   */
  @Override public void setPlayer(ToroPlayer player) {
    mPlayer = player;
  }

  @Override public void onRegistered() {
    mUiHandler = new Handler(Looper.getMainLooper(), mCallback);
  }

  @Override public void onUnregistered() {
    mUiHandler.removeCallbacksAndMessages(null);
    mUiHandler = null;
  }

  /**
   * Start playing current video
   */
  @Override public void startPlayback() {
    if (mPlayer != null) {
      mPlayer.start();
    }
  }

  /**
   * Pause current video
   */
  @Override public void pausePlayback() {
    if (mPlayer != null) {
      mPlayer.pause();
    }
  }

  /**
   * Save current video state
   */
  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    if (videoId != null) {
      mVideoStates.put(videoId, position == null ? Long.valueOf(0) : position);
    }
  }

  /**
   * Restore and setup state of a Video to current video player
   */
  @Override public void restoreVideoState(String videoId) {
    if (mPlayer == null) {
      return;
    }

    Long position = mVideoStates.get(videoId);
    if (position == null) {
      position = 0L;
    }

    // See {@link android.media.MediaPlayer#seekTo(int)}
    try {
      mPlayer.seekTo(position);
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Nullable @Override public Long getSavedPosition(String videoId) {
    if (getPlayer() != null && videoId.equals(getPlayer().getVideoId())) {
      return getPlayer().getCurrentPosition();
    }
    return mVideoStates.get(videoId);
  }

  @Override public void startPlayback(long position) {
    if (mPlayer != null) {
      mPlayer.start(position);
    }
  }

  @Override public void stopPlayback() {
    if (mPlayer != null) {
      mPlayer.stop();
    }
  }

  // Adapt from YouTubePlayer.PlaybackEventListener

  public void onPlaying() {
    // video starts playing
    if (mUiHandler != null) {
      // Remove old callback if exist
      mUiHandler.removeMessages(MESSAGE_PLAYBACK_PROGRESS);
      mUiHandler.sendEmptyMessageDelayed(MESSAGE_PLAYBACK_PROGRESS, 250);
    }
  }

  public void onPaused() {
    if (mUiHandler != null) {
      mUiHandler.removeMessages(MESSAGE_PLAYBACK_PROGRESS);
    }
  }

  public void onError(YouTubePlayer.ErrorReason errorReason) {
    if (mUiHandler != null) {
      mUiHandler.removeMessages(MESSAGE_PLAYBACK_PROGRESS);
    }
  }

  //public void onStopped() {
  //}

  //@Override public void onBuffering(boolean b) {
  //}

  //@Override public void onSeekTo(int i) {
  //}
}
