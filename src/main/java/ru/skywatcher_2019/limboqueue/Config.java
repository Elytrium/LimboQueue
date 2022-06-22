package ru.skywatcher_2019.limboqueue;

import net.elytrium.java.commons.config.YamlConfig;

public class Config extends YamlConfig {
    @Ignore
    public static final Config IMP = new Config();

    @Create
    public MAIN MAIN;

    public static class MAIN {
        public String SERIALIZER = "MINIMESSAGE";
        public String SERVER = "survival";
    }

    @Create
    public MESSAGES MESSAGES;

    public static class MESSAGES {
        public String QUEUEMESSAGE = "Игроков в очереди: {0}";
        public String OFFLINESERVER = "На данный момент сервер выключен.";
    }
}
