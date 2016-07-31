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
import com.autsia.pgcrawler.coordinates.PokestopComparator;
import com.autsia.pgcrawler.metadata.PokestopsCache;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("farmAction")
public class FarmAction extends AbstractAction {

    @Autowired
    private PokestopsCache pokestopsCache;

    @Override
    public void perform() {
        try {
            Collection<Pokestop> pokestops = new ArrayList<>(go.getMap().getMapObjects().getPokestops());
            Optional<Pokestop> stop = pokestops.stream().sorted(new PokestopComparator(go)).filter(s -> !pokestopsCache.isInCooldown(s)).findFirst();

            stop.ifPresent(pokestop -> {
                try {
                    String pokestopId = pokestop.getId();
                    PokestopLootResult lootResult = pokestop.loot();
                    FortSearchResponseOuterClass.FortSearchResponse.Result result = lootResult.getResult();
                    switch (result) {
                        case SUCCESS:
                            log.info("Looted pokestop {}", pokestopId);
                            log.info("Experience gained: {}", lootResult.getExperience());
                            List<ItemAwardOuterClass.ItemAward> itemsAwarded = lootResult.getItemsAwarded();
                            for (ItemAwardOuterClass.ItemAward itemAward : itemsAwarded) {
                                log.info("Awarded with items: {}", itemAward.getItemId().name());
                            }
                            pokestopsCache.setToCooldown(pokestop);
                            break;
                        case INVENTORY_FULL:
                            log.info("Looted pokestop {}", pokestopId);
                            log.info("Experience gained: {}", lootResult.getExperience());
                            log.info("Awarded with no items, because inventory is full...");
                            pokestopsCache.setToCooldown(pokestop);
                            break;
                        case OUT_OF_RANGE:
                            log.info("Can't loot pokestop {} - it's too far away, let's walk closer...", pokestopId);
                            pokestopsCache.addStopToGo(pokestop);
                            break;
                        default:
                            log.info("Can't loot pokestop {}, reason: ", pokestopId, result.name());
                            pokestopsCache.setToCooldown(pokestop);
                    }
                } catch (LoginFailedException | RemoteServerException e) {
                    log.error(e.getMessage(), e);
                }

            });
        } catch (LoginFailedException | RemoteServerException e) {
            log.error(e.getMessage(), e);
        }
    }

}
