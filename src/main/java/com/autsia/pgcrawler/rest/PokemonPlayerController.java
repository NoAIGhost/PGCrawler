/*
 *   This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.autsia.pgcrawler.rest;

import com.autsia.pgcrawler.rest.model.MapPlayer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokegoapi.api.PokemonGo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by MRN2x2 on 31.07.2016.
 */
@RestController
public class PokemonPlayerController {
    @Autowired
    @Qualifier(value = "pokemonGo")
    private PokemonGo pokemonGo;

    //for test purposes
    @RequestMapping(value = "/changePokemonPlayerLocation")
    @ResponseBody
    public void changePlayerLocation(@RequestParam(value = "lat") String lat,
                                     @RequestParam(value = "lng") String lng) {
        if (pokemonGo == null) {
            System.out.println("Player account is not initialized");
            return;
        }
        double initialLatitude = Double.parseDouble(lat);
        double initialLongitude = Double.parseDouble(lng);
        pokemonGo.setLatitude(initialLatitude);
        pokemonGo.setLongitude(initialLongitude);
    }

    @RequestMapping(value = "/getPokemonPlayer", produces = "text/event-stream")
    @ResponseBody
    public String getPlayer() {
        if (pokemonGo == null) {
            System.out.println("Player account is not initialized");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        MapPlayer mapPlayer = new MapPlayer();
        mapPlayer.setLatitude(pokemonGo.getLatitude());
        mapPlayer.setLongitude(pokemonGo.getLongitude());
        String player = null;
        try {
            player = mapper.writeValueAsString(mapPlayer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "retry: 200\ndata: " + player + "\n\n";
    }
}
