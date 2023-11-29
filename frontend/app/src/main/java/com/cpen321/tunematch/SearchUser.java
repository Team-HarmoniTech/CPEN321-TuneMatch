package com.cpen321.tunematch;

import java.util.List;

public class SearchUser {
    private final String name;
    private final String id;
    private final String profilePic;
    private String matchPercent;
    private List<String> topArtist;
    private List<String> topGenres;

    // ChatGPT Usage: No
    public SearchUser(String name, String id, String profilePic) {
        this.name = name;
        this.id = id;
        this.profilePic = profilePic;
    }

    // ChatGPT Usage: No
    public String getName() {
        return name;
    }

    // ChatGPT Usage: No
    public String getId() {
        return id;
    }

    // ChatGPT Usage: No
    public String getProfilePic() {
        return profilePic;
    }

    // ChatGPT Usage: No
    public String getMatchPercent() {
        return matchPercent;
    }

    // ChatGPT Usage: No
    public void setMatchPercent(String matchPercent) {
        this.matchPercent = matchPercent;
    }

    // ChatGPT Usage: No
    public List<String> getTopArtist() {
        return topArtist;
    }

    // ChatGPT Usage: No
    public void setTopArtist(List<String> topArtist) {
        this.topArtist = topArtist;
    }

    // ChatGPT Usage: No
    public List<String> getTopGenres() {
        return topGenres;
    }

    // ChatGPT Usage: No
    public void setTopGenres(List<String> topGenres) {
        this.topGenres = topGenres;
    }
}
