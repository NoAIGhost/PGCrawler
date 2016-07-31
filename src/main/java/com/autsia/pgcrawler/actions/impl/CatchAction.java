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

import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass;
import POGOProtos.Networking.Responses.EncounterResponseOuterClass;
import com.autsia.pgcrawler.metadata.PokemonsCache;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CatchAction extends AbstractAction {

    private PokemonsCache pokemonsCache;

    @Override
    public void perform() {
        try {
            List<CatchablePokemon> catchablePokemons = go.getMap().getCatchablePokemon().stream().filter(pokemonsCache::isBanned).collect(Collectors.toList());
            log.info("Pokemon in area: {}", catchablePokemons.size());
            if (!hasBalls()) {
                log.info("No pokeballs available...");
                return;
            }
            for (CatchablePokemon catchablePokemon : catchablePokemons) {
                log.info("Trying to catch {}", catchablePokemon.getPokemonId());
                EncounterResult encounterResult = catchablePokemon.encounterPokemon();
                if (encounterResult.wasSuccessful()) {
                    log.info("Encountered {} successfully!", encounterResult.getWildPokemon().getPokemonData().getNickname());
                    int cp = encounterResult.getWildPokemon().getPokemonData().getCp();
                    int stamina = encounterResult.getWildPokemon().getPokemonData().getStamina();
                    int attack = encounterResult.getWildPokemon().getPokemonData().getIndividualAttack();
                    int defence = encounterResult.getWildPokemon().getPokemonData().getIndividualDefense();
                    log.info("CP: {}, stamina: {}, attack: {}, defence: {}", cp, stamina, attack, defence);
                    CatchResult catchResult = catchablePokemon.catchPokemonWithRazzBerry();
                    if (catchResult == null) { // result may be null somehow, so we blacklist this pokemon
                        pokemonsCache.banPokemon(catchablePokemon);
                        continue;
                    }
                    if (catchResult.getStatus() == CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS) {
                        log.info("Gotcha!");
                        log.info("XP: {}, Candies: {}, Stardust: {}",
                                catchResult.getXpList().stream().mapToInt(i -> i).sum(),
                                catchResult.getCandyList().stream().mapToInt(i -> i).sum(),
                                catchResult.getStardustList().stream().mapToInt(i -> i).sum());
                    } else {
                        log.info("Capture failed, reason: {}", catchResult.getStatus().name());
                        pokemonsCache.banPokemon(catchablePokemon);
                    }
                } else {
                    log.info("Can't encounter the pokemon, reason: {]", encounterResult.getStatus().name());
                    if (encounterResult.getStatus() == EncounterResponseOuterClass.EncounterResponse.Status.POKEMON_INVENTORY_FULL) {
                        // TODO: disable pokemon catching here
                    }
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            log.error(e.getMessage(), e);
        }
    }

    // right, I know )))
    private boolean hasBalls() {
        Collection<Item> balls = new ArrayList<>();
        for (Pokeball pokeball : Pokeball.values()) {
            balls.add(go.getInventories().getItemBag().getItem(pokeball.getBallType()));
        }
        return !CollectionUtils.isEmpty(balls);
    }

}
