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

import java.util.List;

/**
 * Created by MRN2x2 on 31.07.2016.
 */
public class RadarStepCalculatorTest {

    public static void main(String[] args) {
        S2LatLng initialLocation = S2LatLng.fromDegrees(50.417437, 30.543307);
        RadarStepsCalculator calculator = new RadarStepsCalculator();
        List<S2LatLng> result = calculator.generateSteps(initialLocation, 2);
        int i = 0;
        for (S2LatLng s2LatLng : result) {
            i++;
            System.out.println("MRN ============================" + i);
            System.out.println("MRN latDegrees() " + s2LatLng.latDegrees());
            System.out.println("MRN lngDegrees() " + s2LatLng.lngDegrees());
        }
    }
}
