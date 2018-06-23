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

package toro.demo.exoplayer.creator;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.ExoCreator;
import toro.demo.exoplayer.R;
import toro.v4.Toro;

/**
 * @author eneim (2018/02/07).
 *
 * Demo for {@link ExoCreator}, written in Java.
 */

public class CreatorDemoActivity extends AppCompatActivity {

  static final Uri videoUri =
      // Uri.parse("https://storage.googleapis.com/material-design/publish/material_v_12/assets/0B14F_FSUCc01SWc0N29QR3pZT2s/materialmotionhero-spec-0505.mp4");
      Uri.parse("file:///android_asset/bbb/video.mp4");

  PlayerView playerView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo_creator);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    playerView = findViewById(R.id.playerView);
    Toro.with(this).setUp(videoUri).tag(videoUri).asPlayable().bind(playerView);
  }

}