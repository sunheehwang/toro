
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
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.squareup.moshi.Json;

@Entity public class VideoItem {

  @Json(name = "picture_id") private String pictureId;
  @Embedded @Json(name = "videos") private VideoSizes videos;
  @Json(name = "tags") private String tags;
  @Json(name = "downloads") private Integer downloads;
  @Json(name = "likes") private Integer likes;
  @Json(name = "favorites") private Integer favorites;
  @Json(name = "duration") private Integer duration;
  @Json(name = "id") private Integer id;
  @Json(name = "user_id") private Integer userId;
  @Json(name = "views") private Integer views;
  @Json(name = "comments") private Integer comments;
  @Json(name = "userImageURL") private String userImageURL;
  @PrimaryKey @NonNull @Json(name = "pageURL") private String pageURL = "";
  @Json(name = "type") private String type;
  @Json(name = "user") private String user;

  public String getPictureId() {
    return pictureId;
  }

  public void setPictureId(String pictureId) {
    this.pictureId = pictureId;
  }

  public VideoSizes getVideos() {
    return videos;
  }

  public void setVideos(VideoSizes videos) {
    this.videos = videos;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public Integer getDownloads() {
    return downloads;
  }

  public void setDownloads(Integer downloads) {
    this.downloads = downloads;
  }

  public Integer getLikes() {
    return likes;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
  }

  public Integer getFavorites() {
    return favorites;
  }

  public void setFavorites(Integer favorites) {
    this.favorites = favorites;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getViews() {
    return views;
  }

  public void setViews(Integer views) {
    this.views = views;
  }

  public Integer getComments() {
    return comments;
  }

  public void setComments(Integer comments) {
    this.comments = comments;
  }

  public String getUserImageURL() {
    return userImageURL;
  }

  public void setUserImageURL(String userImageURL) {
    this.userImageURL = userImageURL;
  }

  public String getPageURL() {
    return pageURL;
  }

  public void setPageURL(String pageURL) {
    this.pageURL = pageURL;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VideoItem videoItem = (VideoItem) o;

    if (pictureId != null ? !pictureId.equals(videoItem.pictureId) : videoItem.pictureId != null) {
      return false;
    }
    if (videos != null ? !videos.equals(videoItem.videos) : videoItem.videos != null) return false;
    if (tags != null ? !tags.equals(videoItem.tags) : videoItem.tags != null) return false;
    if (downloads != null ? !downloads.equals(videoItem.downloads) : videoItem.downloads != null) {
      return false;
    }
    if (likes != null ? !likes.equals(videoItem.likes) : videoItem.likes != null) return false;
    if (favorites != null ? !favorites.equals(videoItem.favorites) : videoItem.favorites != null) {
      return false;
    }
    if (duration != null ? !duration.equals(videoItem.duration) : videoItem.duration != null) {
      return false;
    }
    if (id != null ? !id.equals(videoItem.id) : videoItem.id != null) return false;
    if (userId != null ? !userId.equals(videoItem.userId) : videoItem.userId != null) return false;
    if (views != null ? !views.equals(videoItem.views) : videoItem.views != null) return false;
    if (comments != null ? !comments.equals(videoItem.comments) : videoItem.comments != null) {
      return false;
    }
    if (userImageURL != null ? !userImageURL.equals(videoItem.userImageURL)
        : videoItem.userImageURL != null) {
      return false;
    }
    if (!pageURL.equals(videoItem.pageURL)) return false;
    if (type != null ? !type.equals(videoItem.type) : videoItem.type != null) return false;
    return user != null ? user.equals(videoItem.user) : videoItem.user == null;
  }

  @Override public int hashCode() {
    int result = pictureId != null ? pictureId.hashCode() : 0;
    result = 31 * result + (videos != null ? videos.hashCode() : 0);
    result = 31 * result + (tags != null ? tags.hashCode() : 0);
    result = 31 * result + (downloads != null ? downloads.hashCode() : 0);
    result = 31 * result + (likes != null ? likes.hashCode() : 0);
    result = 31 * result + (favorites != null ? favorites.hashCode() : 0);
    result = 31 * result + (duration != null ? duration.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (views != null ? views.hashCode() : 0);
    result = 31 * result + (comments != null ? comments.hashCode() : 0);
    result = 31 * result + (userImageURL != null ? userImageURL.hashCode() : 0);
    result = 31 * result + pageURL.hashCode();
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    return result;
  }
}
