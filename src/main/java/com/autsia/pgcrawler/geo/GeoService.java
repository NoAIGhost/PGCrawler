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

package com.autsia.pgcrawler.geo;

import com.pokegoapi.google.common.geometry.S1Angle;
import com.pokegoapi.google.common.geometry.S2LatLng;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeoService {

    public static final String GEO_SERVICE_URL = "http://yournavigation.org/api/dev/route.php";

    public Collection<S2LatLng> getRouteCoordinates(S2LatLng start, S2LatLng end) {
        return getRouteCoordinates(start.latDegrees(), start.lngDegrees(), end.latDegrees(), end.lngDegrees());
    }

    private Collection<S2LatLng> getRouteCoordinates(double flat, double flon, double tlat, double tlon) {
        String routeParsed = getRouteFile(flat, flon, tlat, tlon);
        if (!routeParsed.contains("<distance>0</distance>")) {
            routeParsed = routeParsed.split("<coordinates>")[1];
            Matcher matcher = Pattern.compile("(|-)\\d+.(|-)\\d+,(|-)\\d+.(|-)\\d+").matcher(routeParsed);
            Collection<String> coordinatesList = new ArrayList<>();
            while (matcher.find()) {
                coordinatesList.add(matcher.group());
            }
            return coordinatesList.stream().map(c -> new S2LatLng(S1Angle.degrees(Double.parseDouble(c.split(",")[1])), S1Angle.degrees(Double.parseDouble(c.split(",")[0])))).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private String getRouteFile(double flat, double flon, double tlat, double tlon) {
        final String[] routeFile = {""};
        try {
            URL url = new URL(createURLString(flat, flon, tlat, tlon));
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            urlConnection.setRequestProperty("Accept-Language", "en");
            urlConnection.setRequestProperty("Cache-Control", "max=0");
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setRequestProperty("DNT", "1");
            urlConnection.setRequestProperty("Host", "router.project-osrm.org");
            urlConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");

            try (InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream())) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    bufferedReader.lines().forEach(l -> routeFile[0] += l);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return routeFile[0];
    }

    private String createURLString(double flat, double flon, double tlat, double tlon) {
        return String.format("%s?flat=%f&flon=%f&tlat=%f&tlon=%f&v=foot&fast=1", GEO_SERVICE_URL, flat, flon, tlat, tlon);
    }

}
