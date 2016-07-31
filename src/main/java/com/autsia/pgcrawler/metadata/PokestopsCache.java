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

import com.pokegoapi.api.map.fort.Pokestop;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PokestopsCache {

    private Map<String, Object> lootMap = ExpiringMap.builder()
            .expiration(5, TimeUnit.MINUTES)
            .build();

    public void addPokestop(Pokestop pokestop) {
        lootMap.put(pokestop.getId(), null);
    }

    public boolean isInCooldown(Pokestop pokestop) {
        return lootMap.containsKey(pokestop.getId());
    }

}
