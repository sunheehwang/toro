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

package toro.pixabay.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.squareup.moshi.Json;

@Entity public class VideoSize {

  @PrimaryKey @NonNull @Json(name = "url") private String url = "";
  @Json(name = "width") private Integer width;
  @Json(name = "size") private Integer size;
  @Json(name = "height") private Integer height;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VideoSize videoSize = (VideoSize) o;

    if (!url.equals(videoSize.url)) return false;
    if (width != null ? !width.equals(videoSize.width) : videoSize.width != null) return false;
    if (size != null ? !size.equals(videoSize.size) : videoSize.size != null) return false;
    return height != null ? height.equals(videoSize.height) : videoSize.height == null;
  }

  @Override public int hashCode() {
    int result = url.hashCode();
    result = 31 * result + (width != null ? width.hashCode() : 0);
    result = 31 * result + (size != null ? size.hashCode() : 0);
    result = 31 * result + (height != null ? height.hashCode() : 0);
    return result;
  }
}
