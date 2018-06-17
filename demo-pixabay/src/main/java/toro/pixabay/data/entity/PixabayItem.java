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

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author eneim (2018/05/11).
 */
@Entity(  //
    tableName = "pixabay_item", //
    indices = { @Index(value = "query"), @Index(value = "timeStamp") } //
) //
public class PixabayItem {

  private int type; // 1: Photo, 2: Video
  private long timeStamp;

  @PrimaryKey @NonNull private String pageUrl = "";

  @Embedded(prefix = "photo_") private PhotoItem photoItem;

  @Embedded(prefix = "video_") private VideoItem videoItem;

  private String query;

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  @NonNull public String getPageUrl() {
    return pageUrl;
  }

  public void setPageUrl(@NonNull String pageUrl) {
    this.pageUrl = pageUrl;
  }

  public PhotoItem getPhotoItem() {
    return photoItem;
  }

  public void setPhotoItem(PhotoItem photoItem) {
    this.photoItem = photoItem;
  }

  public VideoItem getVideoItem() {
    return videoItem;
  }

  public void setVideoItem(VideoItem videoItem) {
    this.videoItem = videoItem;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public static PixabayItem fromPhotoItem(PhotoItem item, long nowNano) {
    PixabayItem result = new PixabayItem();
    result.timeStamp = nowNano;
    result.type = 1;
    result.pageUrl = item.getPageURL();
    result.photoItem = item;
    result.videoItem = null;
    return result;
  }

  public static PixabayItem fromVideoItem(VideoItem item, long nowNano) {
    PixabayItem result = new PixabayItem();
    result.timeStamp = nowNano;
    result.type = 2;
    result.pageUrl = item.getPageURL();
    result.videoItem = item;
    result.photoItem = null;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PixabayItem that = (PixabayItem) o;

    if (type != that.type) return false;
    if (!pageUrl.equals(that.pageUrl)) return false;
    if (photoItem != null ? !photoItem.equals(that.photoItem) : that.photoItem != null) {
      return false;
    }
    return videoItem != null ? videoItem.equals(that.videoItem) : that.videoItem == null;
  }

  @Override public int hashCode() {
    int result = type;
    result = 31 * result + pageUrl.hashCode();
    result = 31 * result + (photoItem != null ? photoItem.hashCode() : 0);
    result = 31 * result + (videoItem != null ? videoItem.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "PixabayItem{"
        + "type="
        + type
        + ", timeStamp="
        + timeStamp
        + ", pageUrl='"
        + pageUrl
        + '\''
        + '}';
  }
}
