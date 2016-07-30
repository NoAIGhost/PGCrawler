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

package com.autsia.pgcrawler.actions.impl;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;
import com.autsia.pgcrawler.metadata.PokestopsCache;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2LatLng;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

@Slf4j
public class FarmAction extends AbstractAction {

    private PokestopsCache pokestopsCache;

    @Override
    public void perform() {
        try {
            Collection<Pokestop> sortedStops = new TreeSet<>(go.getMap().getMapObjects().getPokestops());
            Optional<Pokestop> stop = sortedStops.stream().sorted((stop1, stop2) -> {
                S2LatLng locationA = S2LatLng.fromDegrees(stop1.getLatitude(), stop1.getLongitude());
                S2LatLng locationB = S2LatLng.fromDegrees(stop2.getLatitude(), stop2.getLongitude());
                S2LatLng currentLocation = S2LatLng.fromDegrees(go.getLatitude(), go.getLongitude());
                Double distanceA = currentLocation.getEarthDistance(locationA);
                Double distanceB = currentLocation.getEarthDistance(locationB);
                return distanceA.compareTo(distanceB);
            }).filter(pokestopsCache::isInCooldown).findFirst();

            stop.ifPresent(pokestop -> {
                PokestopLootResult lootResult = null;
                try {
                    String pokestopId = pokestop.getId();
                    lootResult = pokestop.loot();
                    FortSearchResponseOuterClass.FortSearchResponse.Result result = lootResult.getResult();
                    switch (result) {
                        case SUCCESS:
                            log.info("Looted pokestop {}", pokestopId);
                            log.info("Experience gained: {}", lootResult.getExperience());
                            List<ItemAwardOuterClass.ItemAward> itemsAwarded = lootResult.getItemsAwarded();
                            for (ItemAwardOuterClass.ItemAward itemAward : itemsAwarded) {
                                log.info("Awarded with items: {}", itemAward.getItemId().name());
                            }
                            break;
                        case INVENTORY_FULL:
                            log.info("Looted pokestop {}", pokestopId);
                            log.info("Experience gained: {}", lootResult.getExperience());
                            log.info("Awarded with no items, because inventory is full...");
                            break;
                        default:
                            log.info("Can't loot pokestop {}, reason: ", pokestopId, result.name());
                    }
                    pokestopsCache.addPokestop(pokestop);
                } catch (LoginFailedException | RemoteServerException e) {
                    log.error(e.getMessage(), e);
                }

            });
        } catch (LoginFailedException | RemoteServerException e) {
            log.error(e.getMessage(), e);
        }
    }

}
