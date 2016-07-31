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

import com.autsia.pgcrawler.Main;
import com.autsia.pgcrawler.config.AppConfig;
import com.autsia.pgcrawler.config.AuthType;
import com.autsia.pgcrawler.coordinates.CoordinatesFlowCalculator;
import com.autsia.pgcrawler.rest.model.MapPokemon;
import com.google.gson.Gson;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2LatLng;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MRN2x2 on 30.07.2016.
 */
@RestController
public class NearbyPokemonController {
    private static final Gson gson = new Gson();
    private static final String APPCONFIG_JSON = "appconfig.json";
    @Autowired
    private AppConfig appConfig;

    @RequestMapping(value = "/nearbyPokemons", method = RequestMethod.GET)
    public
    @ResponseBody
    List<MapPokemon> getNearbyPokemons(@RequestParam(value = "lat") String lat,
                                       @RequestParam(value = "lng") String lng)
            throws LoginFailedException, RemoteServerException {
        AppConfig appConfig = null;
        try (Reader reader = new InputStreamReader(Main.class.getResourceAsStream("/" + APPCONFIG_JSON))) {
            appConfig = gson.fromJson(reader, AppConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        OkHttpClient httpClient = new OkHttpClient();
        CredentialProvider provider = getCredentialProvider(appConfig, httpClient);
        PokemonGo go = new PokemonGo(provider, httpClient);
        double initialLatitude = Double.parseDouble(lat);
        double initialLongitude = Double.parseDouble(lng);

        S2LatLng initialLocation = S2LatLng.fromDegrees(initialLatitude, initialLongitude);
        CoordinatesFlowCalculator calculator = new CoordinatesFlowCalculator();
        List<S2LatLng> locationSteps = calculator.generateLocationSteps(initialLocation, 2);
        List<MapPokemon> mapPokemons = getMapPokemons(go, locationSteps);

        return mapPokemons;
    }

    private List<MapPokemon> getMapPokemons(PokemonGo go,
                                            List<S2LatLng> locationSteps) throws LoginFailedException, RemoteServerException {
        System.out.println("MRN Search Pokemons START");
        List<MapPokemon> mapPokemons = new ArrayList<>();
        java.util.Map<Long, NearbyPokemon> encounterIdToPokemon = new HashMap<>();
        boolean firstTime = true;
        for (S2LatLng s2LatLng : locationSteps) {
            go.setLatitude(s2LatLng.latDegrees());
            go.setLongitude(s2LatLng.lngDegrees());
            Map map = go.getMap();
            if (firstTime) {
                for (NearbyPokemon pokemon : map.getNearbyPokemon()) {
                    encounterIdToPokemon.put(pokemon.getEncounterId(), pokemon);
                    System.out.println("MRN found NearbyPokemon " + pokemon.getPokemonId());
                }
                firstTime = false;
            }
            if (encounterIdToPokemon.isEmpty()) {
                break;
            }
            for (CatchablePokemon pokemon : map.getCatchablePokemon()) {
                System.out.println("MRN found CatchablePokemon " + pokemon.getPokemonId());
                encounterIdToPokemon.remove(pokemon.getEncounterId());
                MapPokemon mapPokemon = new MapPokemon();
                mapPokemon.setName(pokemon.getPokemonId().name());
                mapPokemon.setId(pokemon.getPokemonId().getNumber());
                mapPokemon.setLatitude(pokemon.getLatitude());
                mapPokemon.setLongitude(pokemon.getLongitude());
                mapPokemon.setExpirationTimestampMs(pokemon.getExpirationTimestampMs());
                mapPokemons.add(mapPokemon);
            }
            if (encounterIdToPokemon.isEmpty()) {
                break;
            }

            //temp hack to avoid ban
            try {
                Thread.sleep(10000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("MRN Search Pokemons END");
        return mapPokemons;
    }


    private CredentialProvider getCredentialProvider(AppConfig appConfig, OkHttpClient httpClient) throws LoginFailedException, RemoteServerException {
        String auth = appConfig.getAuth();
        CredentialProvider provider;
        if (AuthType.PTC.name().equals(auth)) {
            provider = new PtcCredentialProvider(httpClient, appConfig.getUsername(), appConfig.getPassword());
        } else {
            provider = new GoogleAutoCredentialProvider(httpClient, appConfig.getUsername(), appConfig.getPassword());
        }
        return provider;
    }
}
