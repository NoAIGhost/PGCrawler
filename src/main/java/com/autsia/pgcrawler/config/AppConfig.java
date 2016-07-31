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

package com.autsia.pgcrawler.config;

import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Configuration
public class AppConfig {

    private static final String APPCONFIG_JSON = "appconfig.json";
    private static final String MAPCONFIG_JSON = "mapconfig.json";
    private static final String COORDINATES_REGEXP = "^(\\-?\\d+(\\.\\d+)?),\\s*(\\-?\\d+(\\.\\d+)?)$";
    private static final String COORDINATES_DELIMITER = ", ";

    @Autowired
    @Qualifier(value = "playerProperties")
    private Properties playerProperties;
    @Autowired
    @Qualifier(value = "mapProperties")
    private Properties mapProperties;

    @Bean(name = "playerProperties")
    public Properties playerProperties() throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(AppConfig.class.getResourceAsStream("/" + APPCONFIG_JSON))) {
            return gson.fromJson(reader, Properties.class);
        }
    }

    @Bean(name = "mapProperties")
    public Properties mapProperties() throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(AppConfig.class.getResourceAsStream("/" + MAPCONFIG_JSON))) {
            return gson.fromJson(reader, Properties.class);
        }
    }

    @Bean(name = "pokemonGo")
    public PokemonGo pokemonGo() throws Exception {
        if (playerProperties == null){
            System.out.println("Player properties is not initialized");
            return null;
        }
        OkHttpClient httpClient = new OkHttpClient();
        CredentialProvider provider = getCredentialProvider(playerProperties, httpClient);
        PokemonGo go = new PokemonGo(provider, httpClient);

        printGreeting(go);

        LatLng coordinates = parseLocation(playerProperties);
        go.setLatitude(coordinates.lat);
        go.setLongitude(coordinates.lng);

        return go;
    }

    @Bean(name = "mapGo")
    public PokemonGo mapGo() throws Exception {
        if (mapProperties == null){
            System.out.println("Map properties is not initialized");
            return null;
        }
        OkHttpClient httpClient = new OkHttpClient();
        CredentialProvider provider = getCredentialProvider(mapProperties, httpClient);
        PokemonGo go = new PokemonGo(provider, httpClient);

        printGreeting(go);

        LatLng coordinates = parseLocation(mapProperties);
        go.setLatitude(coordinates.lat);
        go.setLongitude(coordinates.lng);

        return go;
    }

    private static void printGreeting(PokemonGo go) {
        PlayerProfile playerProfile = go.getPlayerProfile();
        System.out.println("Name: " + playerProfile.getUsername());
        System.out.println("Team: " + playerProfile.getTeam().name());
        System.out.println("Level: " + playerProfile.getStats().getLevel());
        System.out.println("Experience to next level: " + (playerProfile.getStats().getNextLevelXp() - playerProfile.getStats().getExperience()));
        System.out.println("Pokemons: " + go.getInventories().getPokebank().getPokemons().size());
        System.out.println("Pokecoin: " + playerProfile.getCurrencies().get(PlayerProfile.Currency.POKECOIN));
        System.out.println("Stardust: " + playerProfile.getCurrencies().get(PlayerProfile.Currency.STARDUST));
    }

    private CredentialProvider getCredentialProvider(Properties properties, OkHttpClient httpClient) throws LoginFailedException, RemoteServerException {
        String auth = properties.getAuth();
        CredentialProvider provider;
        if (AuthType.PTC.name().equals(auth)) {
            provider = new PtcCredentialProvider(httpClient, properties.getUsername(), properties.getPassword());
        } else {
            provider = new GoogleAutoCredentialProvider(httpClient, properties.getUsername(), properties.getPassword());
        }
        return provider;
    }

    private LatLng parseLocation(Properties properties) throws Exception {
        String location = properties.getLocation();
        String gmapkey = properties.getGmapkey();
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
