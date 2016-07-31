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

import com.autsia.pgcrawler.actions.PGAction;
import com.autsia.pgcrawler.metadata.PokestopsCache;
import com.pokegoapi.google.common.geometry.S2LatLng;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("walkAction")
public class WalkAction extends AbstractAction {

    @Autowired
    private PokestopsCache pokestopsCache;

    @Autowired
    private PGAction catchAction;

    @Override
    public void perform() {
        Optional<Collection<S2LatLng>> stopToGo = pokestopsCache.getStopToGo();
        if (stopToGo.isPresent()) {
            for (S2LatLng s2LatLng : stopToGo.get()) {
                try {
                    tryToCatchPokemon();
                    walkTo(s2LatLng);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            // TODO: implement random walk
        }

    }

    private void tryToCatchPokemon() {
        catchAction.perform();
    }

    private void walkTo(S2LatLng s2LatLng) throws InterruptedException {
        S2LatLng botLocation = S2LatLng.fromDegrees(go.getLatitude(), go.getLongitude());
        S2LatLng diff = s2LatLng.sub(botLocation);
        double distance = s2LatLng.getEarthDistance(botLocation);
        long timeout = 200L;
        double speed = properties.getWalkSpeed();
        if (speed == 0) {
            return;
        }
        double timeRequired = distance / speed;
        double stepsRequired = timeRequired / (timeout * 0.001d);
        if (stepsRequired == 0) {
            return;
        }
        double deltaLat = diff.latDegrees() / stepsRequired;
        double deltaLng = diff.lngDegrees() / stepsRequired;

        log.info("Walking to {} in {} steps...", s2LatLng.toStringDegrees(), stepsRequired);

        for (int i = 0; i < stepsRequired; i++) {
            go.setLatitude(go.getLatitude() + deltaLat);
            go.setLongitude(go.getLongitude() + deltaLng);
            Thread.sleep(timeout);
        }
    }

}
