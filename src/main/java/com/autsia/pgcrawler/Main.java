/*
 *     This program is free software: you can redistribute it and/or modify
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

package com.autsia.pgcrawler;

import com.autsia.pgcrawler.config.AppConfig;
import com.autsia.pgcrawler.config.AuthType;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;
import org.springframework.util.Assert;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class Main {

    private static final Gson gson = new Gson();

    private static final String APPCONFIG_JSON = "appconfig.json";

    private static final String COORDINATES_REGEXP = "^(\\-?\\d+(\\.\\d+)?),\\s*(\\-?\\d+(\\.\\d+)?)$";
    private static final String COORDINATES_DELIMITER = ", ";

    public static void main(String... args) throws Exception {
        try (Reader reader = new InputStreamReader(Main.class.getResourceAsStream("/" + APPCONFIG_JSON))) {
            AppConfig appConfig = gson.fromJson(reader, AppConfig.class);
            OkHttpClient httpClient = new OkHttpClient();
            CredentialProvider provider = getCredentialProvider(appConfig, httpClient);
            PokemonGo go = new PokemonGo(provider, httpClient);

            printGreeting(go.getPlayerProfile());

            LatLng coordinates = parseLocation(appConfig);
            go.setLatitude(coordinates.lat);
            go.setLongitude(coordinates.lng);

            List<CatchablePokemon> catchablePokemon = go.getMap().getCatchablePokemon();
            for (CatchablePokemon pokemon : catchablePokemon) {
                System.out.println("pokemon.getPokemonId() = " + pokemon.getPokemonId());
            }
        }
    }

    private static void printGreeting(PlayerProfile playerProfile) {
        System.out.println("Name: " + playerProfile.getUsername());
        System.out.println("Team: " + playerProfile.getTeam().name());
        System.out.println("Level: " + playerProfile.getStats().getLevel());
        System.out.println("Experience to next level: " + (playerProfile.getStats().getNextLevelXp() - playerProfile.getStats().getExperience()));
        System.out.println("Pokecoin: " + playerProfile.getCurrencies().get(PlayerProfile.Currency.POKECOIN));
        System.out.println("Stardust: " + playerProfile.getCurrencies().get(PlayerProfile.Currency.STARDUST));
    }

    private static CredentialProvider getCredentialProvider(AppConfig appConfig, OkHttpClient httpClient) throws LoginFailedException, RemoteServerException {
        String auth = appConfig.getAuth();
        CredentialProvider provider;
        if (AuthType.PTC.name().equals(auth)) {
            provider = new PtcCredentialProvider(httpClient, appConfig.getUsername(), appConfig.getPassword());
        } else {
            provider = new GoogleAutoCredentialProvider(httpClient, appConfig.getUsername(), appConfig.getPassword());
        }
        return provider;
    }

    private static LatLng parseLocation(AppConfig appConfig) throws Exception {
        String location = appConfig.getLocation();
        String gmapkey = appConfig.getGmapkey();
        Assert.notNull(location);
        Assert.notNull(gmapkey);
        GeoApiContext context = new GeoApiContext().setApiKey(gmapkey);
        LatLng coordinates;
        if (location.matches(COORDINATES_REGEXP)) {
            String[] stringCoordinates = location.split(COORDINATES_DELIMITER);
            coordinates = new LatLng(Double.parseDouble(stringCoordinates[0]), Double.parseDouble(stringCoordinates[1]));
        } else {
            GeocodingResult[] results = GeocodingApi.geocode(context, location).await();
            coordinates = results[0].geometry.location;
        }
        GeocodingResult[] results = GeocodingApi.reverseGeocode(context, coordinates).await();
        System.out.println("Location: " + results[0].formattedAddress);
        return coordinates;
    }

}
