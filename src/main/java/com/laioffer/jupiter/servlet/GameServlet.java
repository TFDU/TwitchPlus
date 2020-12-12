package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public class GameServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();
        response.setContentType("application/json;charset=UTF-8");
        try{
            if(gameName != null){
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGame(gameName)));
            }else{
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        }catch(TwitchException e){
            throw new ServletException(e);
        }
    }
}
