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

package com.autsia.pgcrawler.model;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Pokeball {

    POKEBALL(ItemIdOuterClass.ItemId.ITEM_POKE_BALL),
    GREATBALL(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL),
    ULTRABALL(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL),
    MASTERBALL(ItemIdOuterClass.ItemId.ITEM_MASTER_BALL);

    @Getter
    private final ItemIdOuterClass.ItemId ballType;

}
