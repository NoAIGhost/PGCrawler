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

/**
 * Created by MRN2x2 on 30.07.2016.
 */
function initMap() {
    var mapDiv = document.getElementById('map_canvas');
    var map = new google.maps.Map(mapDiv, {
        center: {lat: 50.448944, lng: 30.523042},
        zoom: 17
    });

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
            var pos = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };

            map.setCenter(pos);
            var marker = new google.maps.Marker({
                position: pos,
                map: map,
                draggable: true
            });
            getNearbyPokemons(pos);
        }, function () {
            handleLocationError(true, map, map.getCenter());
        });
    } else {
        // Browser doesn't support Geolocation
        handleLocationError(false, map, map.getCenter());
    }
    function handleLocationError(browserHasGeolocation, map, pos) {
        var infoWindow = new google.maps.InfoWindow({map: map});
        infoWindow.setPosition(pos);
        infoWindow.setContent(browserHasGeolocation ?
            'Error: The Geolocation service failed.' :
            'Error: Your browser doesn\'t support geolocation.');
    }

    function getNearbyPokemons(pos) {
        var prefix = '/nearbyPokemons';
        // Origins, anchor positions and coordinates of the marker increase in the X
        // direction to the right and in the Y direction down.

        // Shapes define the clickable region of the icon. The type defines an HTML
        // <area> element 'poly' which traces out a polygon as a series of X,Y points.
        // The final coordinate closes the poly by connecting to the first coordinate.
        var shape = {
            coords: [1, 1, 1, 20, 18, 20, 18, 1],
            type: 'poly'
        };
        $.ajax({
            type: 'GET',
            //url: prefix + "?lat=" + 50.417437 + "&lng=" + 30.543307,
            url: prefix + "?lat=" + pos.lat + "&lng=" + pos.lng,
            dataType: 'json',
            async: true,
            success: function (result) {
                for (var i = 0; i < result.length; i++) {
                    var pokemon = result[i];
                    var latitude = pokemon.latitude;
                    var longitude = pokemon.longitude;
                    var pokemonName = pokemon.name;
                    var pokemonId = pokemon.id;
                    var pokemonExpiration = pokemon.expirationTimestampMs;
                    var image = {
                        url: 'static/images/pokemons/' + pokemonId + ".png",
                        // This marker is 20 pixels wide by 32 pixels high.
                        size: new google.maps.Size(20, 32),
                        // The origin for this image is (0, 0).
                        origin: new google.maps.Point(0, 0),
                        // The anchor for this image is the base of the flagpole at (0, 32).
                        anchor: new google.maps.Point(0, 32)
                    };
                    var marker = new google.maps.Marker({
                        position: {lat: latitude, lng: longitude},
                        map: map,
                        icon: image,
                        shape: shape,
                        title: pokemonName + +" " + pokemonId + " " + pokemonExpiration
                    });
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(jqXHR.status + ' ' + jqXHR.responseText);
            }
        });
    }
}

