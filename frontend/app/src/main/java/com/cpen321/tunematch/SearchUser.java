package com.cpen321.tunematch;

import java.util.List;

public class SearchUser {
    private String name;
    private String id;
    private String profilePic;
    private String matchPercent;
    private List<String> topArtist;
    private List<String> topGenres;
    public SearchUser(String name, String id, String profilePic) {
        this.name = name;
        this.id = id;
        this.profilePic = profilePic;
    }

    public String getName() {return name;}
    public String getId() {return id;}
    public String getProfilePic() {return profilePic;}
    public String getMatchPercent() {return matchPercent;}
    public List<String> getTopArtist() {return topArtist;}
    public List<String> getTopGenres() {return topGenres;}
    public void setTopArtist(List<String> topArtist) {
        this.topArtist = topArtist;
    }
    public void setTopGenres(List<String> topGenres) {
        this.topGenres = topGenres;
    }

    public void setMatchPercent(String matchPercent) {
        this.matchPercent = matchPercent;
    }
}
