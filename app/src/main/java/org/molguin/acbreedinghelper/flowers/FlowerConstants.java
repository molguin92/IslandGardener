package org.molguin.acbreedinghelper.flowers;

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

