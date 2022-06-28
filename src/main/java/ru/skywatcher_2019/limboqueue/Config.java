package ru.skywatcher_2019.limboqueue;

import net.elytrium.java.commons.config.YamlConfig;

public class Config extends YamlConfig {
    @Ignore
    public static final Config IMP = new Config();

    @Create
    public MAIN MAIN;

    public static class MAIN {
        @Comment(
                "Serializers: LEGACY_AMPERSAND, LEGACY_SECTION, MINIMESSAGE"
        )
        public String SERIALIZER = "MINIMESSAGE";
        public String SERVER = "survival";
        @Comment(
                "Server checking interval in seconds"
        )
        public int CHECK_INTERVAL = 2;

        @Create
        public Config.MAIN.WORLD WORLD;

        public static class WORLD {
            @Comment(
                    "Dimensions: OVERWORLD, NETHER, THE_END"
            )
            public String DIMENSION = "OVERWORLD";
        }

    }

    @Create
    public MESSAGES MESSAGES;

    public static class MESSAGES {
        public String QUEUE_MESSAGE = "Your position in queue: {0}";
        public String SERVER_OFFLINE = "<red>Server is offline.";
    }
}
