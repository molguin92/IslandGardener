/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     FlowerConstants.java is part of Island Gardener
 *
 *     Island Gardener is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Island Gardener is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Island Gardener.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.molguin.islandgardener.flowers;

public class FlowerConstants {
    public enum Species {
        COSMO,
        HYACINTH,
        LILY,
        MUM,
        PANSY,
        ROSE,
        TULIP,
        WINDFLOWER;

        public String namePlural() {
            String name = this.name();
            if (name.endsWith("Y")) {
                name = name.substring(0, name.length() - 1) + "IE";
            }
            return name + "S";
        }
    }

    public enum Color {
        RED,
        YELLOW,
        WHITE,
        ORANGE,
        PINK,
        PURPLE,
        BLUE,
        BLACK,
        GREEN
    }

    public enum Origin {
        SEED, BREEDING, ISLAND
    }
}

