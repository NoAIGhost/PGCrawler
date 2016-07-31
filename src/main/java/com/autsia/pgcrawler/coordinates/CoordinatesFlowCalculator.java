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

package com.autsia.pgcrawler.coordinates;

import com.pokegoapi.google.common.geometry.S2LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MRN2x2 on 31.07.2016.
 */
public class CoordinatesFlowCalculator {

    public static final double NORTH = 0;
    public static final double EAST = 90;
    public static final double SOUTH = 180;
    public static final double WEST = 270;
    public static final double R = 6378.1;
    public static final double PULSE_RADIUS = 0.1;// km - radius of players heartbeat is 100m
    public static final double XDIST = Math.sqrt(3) * PULSE_RADIUS; // dist between column centers = 300м
    public static final double YDIST = 3 * (PULSE_RADIUS / 2);// dist between row centers = 150м

    private List<S2LatLng> sortedCoordinates = new ArrayList<>();

    public List<S2LatLng> generateLocationSteps(S2LatLng initialLocation, int stepCount) {
        sortedCoordinates.add(initialLocation);
        int ring = 1;
        S2LatLng loc = initialLocation;
        while (ring < stepCount) {
            loc = calculateNewCoordinates(loc, YDIST, NORTH);
            loc = calculateNewCoordinates(loc, XDIST / 2, WEST);
            for (int direction = 0; direction < 6; direction++) {
                for (int j = 0; j < ring; j++) {
                    if (direction == 0) {//RIGHT
                        loc = calculateNewCoordinates(loc, XDIST, EAST);
                    } else if (direction == 1) {//DOWN + RIGHT
                        loc = calculateNewCoordinates(loc, YDIST, SOUTH);
                        loc = calculateNewCoordinates(loc, XDIST / 2, EAST);
                    } else if (direction == 2) {//DOWN + LEFT
                        loc = calculateNewCoordinates(loc, YDIST, SOUTH);
                        loc = calculateNewCoordinates(loc, XDIST / 2, WEST);
                    } else if (direction == 3) {//LEFT
                        loc = calculateNewCoordinates(loc, XDIST, WEST);
                    } else if (direction == 4) {//UP + LEFT
                        loc = calculateNewCoordinates(loc, YDIST, NORTH);
                        loc = calculateNewCoordinates(loc, XDIST / 2, WEST);
                    } else if (direction == 5) {//UP + RIGHT
                        loc = calculateNewCoordinates(loc, YDIST, NORTH);
                        loc = calculateNewCoordinates(loc, XDIST / 2, EAST);
                    }
                    sortedCoordinates.add(loc);
                }
            }

            ring += 1;
        }
        return sortedCoordinates;
    }

    private S2LatLng calculateNewCoordinates(S2LatLng initialLocation,
                                             double distance, double bearing) {
        double newLatitude = Math.asin(Math.sin(initialLocation.latRadians()) * Math.cos(distance / R) +
                Math.cos(initialLocation.latRadians()) * Math.sin(distance / R) * Math.cos(bearing));
        double newLongtitude = initialLocation.lngRadians()
                + Math.atan2(Math.sin(bearing) * Math.sin(distance / R) * Math.cos(initialLocation.latRadians()),
                Math.cos(distance / R) - Math.sin(initialLocation.latRadians()) * Math.sin(newLatitude));

        return S2LatLng.fromRadians(newLatitude, newLongtitude);
    }

}
