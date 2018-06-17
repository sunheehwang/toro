
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

@Entity(tableName = "photo_item")
public class PhotoItem {

  @Json(name = "largeImageURL") private String largeImageURL;
  @Json(name = "webformatHeight") private Integer webformatHeight;
  @Json(name = "webformatWidth") private Integer webformatWidth;
  @Json(name = "likes") private Integer likes;
  @Json(name = "imageWidth") private Integer imageWidth;
  @Json(name = "id") private Integer id;
  @Json(name = "user_id") private Integer userId;
  @Json(name = "views") private Integer views;
  @Json(name = "comments") private Integer comments;
  @PrimaryKey @NonNull @Json(name = "pageURL") private String pageURL;
  @Json(name = "imageHeight") private Integer imageHeight;
  @Json(name = "webformatURL") private String webformatURL;
  @Json(name = "type") private String type;
  @Json(name = "previewHeight") private Integer previewHeight;
  @Json(name = "tags") private String tags;
  @Json(name = "downloads") private Integer downloads;
  @Json(name = "user") private String user;
  @Json(name = "favorites") private Integer favorites;
  @Json(name = "imageSize") private Integer imageSize;
  @Json(name = "previewWidth") private Integer previewWidth;
  @Json(name = "userImageURL") private String userImageURL;
  @Json(name = "previewURL") private String previewURL;

  public String getLargeImageURL() {
    return largeImageURL;
  }

  public void setLargeImageURL(String largeImageURL) {
    this.largeImageURL = largeImageURL;
  }

  public Integer getWebformatHeight() {
    return webformatHeight;
  }

  public void setWebformatHeight(Integer webformatHeight) {
    this.webformatHeight = webformatHeight;
  }

  public Integer getWebformatWidth() {
    return webformatWidth;
  }

  public void setWebformatWidth(Integer webformatWidth) {
    this.webformatWidth = webformatWidth;
  }

  public Integer getLikes() {
    return likes;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
  }

  public Integer getImageWidth() {
    return imageWidth;
  }

  public void setImageWidth(Integer imageWidth) {
    this.imageWidth = imageWidth;
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

  public String getPageURL() {
    return pageURL;
  }

  public void setPageURL(String pageURL) {
    this.pageURL = pageURL;
  }

  public Integer getImageHeight() {
    return imageHeight;
  }

  public void setImageHeight(Integer imageHeight) {
    this.imageHeight = imageHeight;
  }

  public String getWebformatURL() {
    return webformatURL;
  }

  public void setWebformatURL(String webformatURL) {
    this.webformatURL = webformatURL;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getPreviewHeight() {
    return previewHeight;
  }

  public void setPreviewHeight(Integer previewHeight) {
    this.previewHeight = previewHeight;
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

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Integer getFavorites() {
    return favorites;
  }

  public void setFavorites(Integer favorites) {
    this.favorites = favorites;
  }

  public Integer getImageSize() {
    return imageSize;
  }

  public void setImageSize(Integer imageSize) {
    this.imageSize = imageSize;
  }

  public Integer getPreviewWidth() {
    return previewWidth;
  }

  public void setPreviewWidth(Integer previewWidth) {
    this.previewWidth = previewWidth;
  }

  public String getUserImageURL() {
    return userImageURL;
  }

  public void setUserImageURL(String userImageURL) {
    this.userImageURL = userImageURL;
  }

  public String getPreviewURL() {
    return previewURL;
  }

  public void setPreviewURL(String previewURL) {
    this.previewURL = previewURL;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PhotoItem photoItem = (PhotoItem) o;

    if (largeImageURL != null ? !largeImageURL.equals(photoItem.largeImageURL)
        : photoItem.largeImageURL != null) {
      return false;
    }
    if (webformatHeight != null ? !webformatHeight.equals(photoItem.webformatHeight)
        : photoItem.webformatHeight != null) {
      return false;
    }
    if (webformatWidth != null ? !webformatWidth.equals(photoItem.webformatWidth)
        : photoItem.webformatWidth != null) {
      return false;
    }
    if (likes != null ? !likes.equals(photoItem.likes) : photoItem.likes != null) return false;
    if (imageWidth != null ? !imageWidth.equals(photoItem.imageWidth)
        : photoItem.imageWidth != null) {
      return false;
    }
    if (id != null ? !id.equals(photoItem.id) : photoItem.id != null) return false;
    if (userId != null ? !userId.equals(photoItem.userId) : photoItem.userId != null) return false;
    if (views != null ? !views.equals(photoItem.views) : photoItem.views != null) return false;
    if (comments != null ? !comments.equals(photoItem.comments) : photoItem.comments != null) {
      return false;
    }
    if (!pageURL.equals(photoItem.pageURL)) return false;
    if (imageHeight != null ? !imageHeight.equals(photoItem.imageHeight)
        : photoItem.imageHeight != null) {
      return false;
    }
    if (webformatURL != null ? !webformatURL.equals(photoItem.webformatURL)
        : photoItem.webformatURL != null) {
      return false;
    }
    if (type != null ? !type.equals(photoItem.type) : photoItem.type != null) return false;
    if (previewHeight != null ? !previewHeight.equals(photoItem.previewHeight)
        : photoItem.previewHeight != null) {
      return false;
    }
    if (tags != null ? !tags.equals(photoItem.tags) : photoItem.tags != null) return false;
    if (downloads != null ? !downloads.equals(photoItem.downloads) : photoItem.downloads != null) {
      return false;
    }
    if (user != null ? !user.equals(photoItem.user) : photoItem.user != null) return false;
    if (favorites != null ? !favorites.equals(photoItem.favorites) : photoItem.favorites != null) {
      return false;
    }
    if (imageSize != null ? !imageSize.equals(photoItem.imageSize) : photoItem.imageSize != null) {
      return false;
    }
    if (previewWidth != null ? !previewWidth.equals(photoItem.previewWidth)
        : photoItem.previewWidth != null) {
      return false;
    }
    if (userImageURL != null ? !userImageURL.equals(photoItem.userImageURL)
        : photoItem.userImageURL != null) {
      return false;
    }
    return previewURL != null ? previewURL.equals(photoItem.previewURL)
        : photoItem.previewURL == null;
  }

  @Override public int hashCode() {
    int result = largeImageURL != null ? largeImageURL.hashCode() : 0;
    result = 31 * result + (webformatHeight != null ? webformatHeight.hashCode() : 0);
    result = 31 * result + (webformatWidth != null ? webformatWidth.hashCode() : 0);
    result = 31 * result + (likes != null ? likes.hashCode() : 0);
    result = 31 * result + (imageWidth != null ? imageWidth.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (views != null ? views.hashCode() : 0);
    result = 31 * result + (comments != null ? comments.hashCode() : 0);
    result = 31 * result + pageURL.hashCode();
    result = 31 * result + (imageHeight != null ? imageHeight.hashCode() : 0);
    result = 31 * result + (webformatURL != null ? webformatURL.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (previewHeight != null ? previewHeight.hashCode() : 0);
    result = 31 * result + (tags != null ? tags.hashCode() : 0);
    result = 31 * result + (downloads != null ? downloads.hashCode() : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (favorites != null ? favorites.hashCode() : 0);
    result = 31 * result + (imageSize != null ? imageSize.hashCode() : 0);
    result = 31 * result + (previewWidth != null ? previewWidth.hashCode() : 0);
    result = 31 * result + (userImageURL != null ? userImageURL.hashCode() : 0);
    result = 31 * result + (previewURL != null ? previewURL.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "PhotoItem{"
        + "largeImageURL='"
        + largeImageURL
        + '\''
        + ", likes="
        + likes
        + ", id="
        + id
        + ", views="
        + views
        + ", comments="
        + comments
        + ", type='"
        + type
        + '\''
        + ", downloads="
        + downloads
        + '}';
  }
}
