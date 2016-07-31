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

package com.autsia.pgcrawler.metadata;

import com.autsia.pgcrawler.geo.GeoService;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.google.common.geometry.S2LatLng;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PokestopsCache {

    @Autowired
    private PokemonGo pokemonGo;

    @Autowired
    private GeoService geoService;

    private Map<String, Object> lootMap = ExpiringMap.builder()
            .expiration(5, TimeUnit.MINUTES)
            .build();

    private LinkedHashMap<String, Collection<S2LatLng>> stopsToGo = new LinkedHashMap<>();


    public void setToCooldown(Pokestop pokestop) {
        stopsToGo.remove(pokestop.getId());
        lootMap.put(pokestop.getId(), null);
    }

    public boolean isInCooldown(Pokestop pokestop) {
        return lootMap.containsKey(pokestop.getId());
    }

    public void addStopToGo(Pokestop pokestop) {
        Collection<S2LatLng> routeCoordinates = geoService.getRouteCoordinates(pokemonGo.getLatitude(), pokemonGo.getLongitude(), pokestop.getLatitude(), pokestop.getLongitude());
        stopsToGo.put(pokestop.getId(), routeCoordinates);
    }

    public Optional<Collection<S2LatLng>> getStopToGo() {
        Iterator<String> iterator = stopsToGo.keySet().iterator();
        if (iterator.hasNext()) {
            return Optional.of(stopsToGo.get(iterator.next()));
        }
        return Optional.empty();
    }

}
