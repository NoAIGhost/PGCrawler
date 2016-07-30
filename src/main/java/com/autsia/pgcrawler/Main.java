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
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Main {

    private static final Gson gson = new Gson();
    private static final String APPCONFIG_JSON = "appconfig.json";

    public static void main(String... args) throws LoginFailedException, RemoteServerException, IOException {
        try (Reader reader = new InputStreamReader(Main.class.getResourceAsStream("/" + APPCONFIG_JSON))) {
            AppConfig appConfig = gson.fromJson(reader, AppConfig.class);
            OkHttpClient httpClient = new OkHttpClient();
            CredentialProvider provider = getCredentialProvider(appConfig, httpClient);
            PokemonGo go = new PokemonGo(provider, httpClient);
            PlayerProfile playerProfile = go.getPlayerProfile();
            System.out.println("User logged in: " + playerProfile.getUsername());
            System.out.println("Experience: " + playerProfile.getStats().getExperience());
        }
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

}
