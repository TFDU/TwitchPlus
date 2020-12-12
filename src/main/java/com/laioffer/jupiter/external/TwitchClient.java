package com.laioffer.jupiter.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class TwitchClient {
    private static final String TOKEN = "Bearer kuzxvzj29apnnzza6jnvvefp9cpq6s";
    private static final String CLIENT_ID = "tvysri8aqmpncsnif5jxroifi84t9e";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;

    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            return String.format(url, limit);
        } else {
            try {
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }
    private String searchTwitch(String url) throws TwitchException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if(status != 200){
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get data from Twitch");
            }
            HttpEntity entity = response.getEntity();
            if(entity == null){
                throw new TwitchException("Failed to get data from Twitch");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();

        };
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", TOKEN);
        httpGet.setHeader("Client-Id", CLIENT_ID);
        try {
            return httpClient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get data from Twitch");
        }finally{
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private List<Game> getGameList(String data) throws TwitchException{
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }
    public List<Game> topGames(int limit) throws TwitchException{
        if(limit <= 0){
            limit = DEFAULT_GAME_LIMIT;
        }
        return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL, "", limit)));
    }
    public Game searchGame(String gameName) throws TwitchException{
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }
}
