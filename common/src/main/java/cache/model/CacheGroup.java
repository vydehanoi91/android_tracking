package cache.model;

import com.google.gson.annotations.SerializedName;

public class CacheGroup {
    @SerializedName("expire")
    public long expire;
    @SerializedName("last")
    public long last;
    @SerializedName("key")
    public String key;
    @SerializedName("content")
    public String content;

    public CacheGroup(String key) {
        this.key = key;
    }
}
