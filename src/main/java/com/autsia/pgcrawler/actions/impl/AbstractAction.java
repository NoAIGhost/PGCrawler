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
import com.autsia.pgcrawler.config.AppConfig;
import com.pokegoapi.api.PokemonGo;

public abstract class AbstractAction implements PGAction {

    protected PokemonGo go;
    protected AppConfig appConfig;

}