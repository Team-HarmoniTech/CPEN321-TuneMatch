package com.cpen321.tunematch;

import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;

public class BackendClient extends ApiClient<BackendInterface> {
    @Override
    protected String getBaseUrl() {
        return "https://zphy19my7b.execute-api.us-west-2.amazonaws.com/v1";
    }
    private @Nullable String currentUserId;

    public BackendClient() {
        super(BackendInterface.class);
    }
    public BackendClient(String currentUserId) {
        super(BackendInterface.class);
        this.currentUserId = currentUserId;
    }

    public User getUser(String userId, boolean fullProfile) throws ApiException {
        Call<String> call = api.getUser(userId, fullProfile);
        JsonObject response = call(call).getAsJsonObject();
        User user;
        if (fullProfile) {
            user = new User(
                    response.get("userId").toString(),
                    response.get("username").toString(),
                    response.get("profilePic").toString()
            );
        } else {
            user = new User(
                    response.get("userId").toString(),
                    response.get("username").toString(),
                    response.get("profilePic").toString(),
                    response.get("bio").toString(),
                    getAsStringList(response.getAsJsonArray("topArtists")),
                    getAsStringList(response.getAsJsonArray("topGenres"))
            );
        }
        return user;
    }

    private static final int MAX_PROFILE_URL = 500;
    public User createUser(SpotifyClient spotifyClient) throws ApiException, JSONException {
        Gson gson = new Gson();
        JsonObject me = spotifyClient.getMe();
        SpotifyClient.SpotifyTopResult topResult = spotifyClient.getMeTopArtistsAndGenres();

        JsonArray spotifyImage = me.get("images").getAsJsonArray();
        JsonElement profilePicture = spotifyImage.get(0).getAsJsonObject().get("url");

        JsonObject createUserBody = new JsonObject();
        JsonObject userInfo = new JsonObject();
        userInfo.add("spotify_id", me.get("id"));
        userInfo.add("username", me.get("display_name"));
        userInfo.add("top_artists", gson.toJsonTree(topResult.topArtists));
        userInfo.add("top_genres", gson.toJsonTree(topResult.topGenres));
        if (profilePicture.toString().length() < MAX_PROFILE_URL) {
            userInfo.add("pfp_url", profilePicture);
        }
        createUserBody.add("userData", userInfo);

        Call<String> call = api.createUser(createUserBody);
        JsonObject response = call(call).getAsJsonObject();
        return new User(
                response.get("userId").toString(),
                response.get("username").toString(),
                response.get("profilePic").toString(),
                response.get("bio").toString(),
                getAsStringList(response.getAsJsonArray("topArtists")),
                getAsStringList(response.getAsJsonArray("topGenres"))
        );
    }

    public List<SearchUser> searchUser(String searchTerm) throws ApiException {
        if (this.currentUserId == null) {
            throw new ApiException(400, "userId is not set");
        }
        Call<String> call = api.searchUser(searchTerm, this.currentUserId);
        JsonArray response = call(call).getAsJsonArray();
        List<SearchUser> searchedUser = new ArrayList<>();

        for (int i = 0; i < response.size(); i++) {
            JsonObject jsonObject = response.get(i).getAsJsonObject();

            String name = jsonObject.get("username").toString();
            String id = jsonObject.get("userId").toString();
            String match_score = jsonObject.get("match_percent").toString();
            String profilePic = jsonObject.get("profilePic").toString();

            SearchUser user = new SearchUser(name, id, profilePic);
            user.setMatchPercent(match_score);
            searchedUser.add(user);
        }

        return searchedUser;
    }

    public User getMe(boolean fullProfile) throws ApiException {
        if (this.currentUserId == null) {
            throw new ApiException(400, "userId is not set");
        }
        Call<String> call = api.getMe(this.currentUserId, fullProfile);
        JsonObject response = call(call).getAsJsonObject();
        User user;
        if (fullProfile) {
            user = new User(
                    response.get("userId").toString(),
                    response.get("username").toString(),
                    response.get("profilePic").toString()
            );
        } else {
            user = new User(
                    response.get("userId").toString(),
                    response.get("username").toString(),
                    response.get("profilePic").toString(),
                    response.get("bio").toString(),
                    getAsStringList(response.getAsJsonArray("topArtists")),
                    getAsStringList(response.getAsJsonArray("topGenres"))
            );
        }
        return user;
    }

    public List<SearchUser> getMatches() throws ApiException {
        if (this.currentUserId == null) {
            throw new ApiException(400, "userId is not set");
        }
        Call<String> call = api.getMatches(this.currentUserId);
        JsonArray response = call(call).getAsJsonArray();
        List<SearchUser> searchedUser = new ArrayList<>();

        for (int i = 0; i < response.size(); i++) {
            JsonObject jsonObject = response.get(i).getAsJsonObject();

            String name = jsonObject.get("username").toString();
            String id = jsonObject.get("userId").toString();
            String match_score = jsonObject.get("match_percent").toString();
            String profilePic = jsonObject.get("profilePic").toString();

            SearchUser user = new SearchUser(name, id, profilePic);
            user.setMatchPercent(match_score);
            searchedUser.add(user);
        }

        return searchedUser;
    }

//    public void updateMe(JsonObject body, final ApiResponseCallback callback) throws ApiException {
//        if (this.currentUserId == null) {
//            callback.onError(new ApiException(400, "userId is not set"));
//        }
//        Call<String> call = api.updateMe(body, this.currentUserId);
//        call(call, callback);
//    }
}