package com.auroramc.punish.model;

import com.auroramc.punish.types.PunishmentType;
import com.auroramc.punish.utils.TimeUtils;

public enum PunishmentReason {
    TESTE("Apenas testando", "Este é um motivo para poder testar.",
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("10s")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("20s"))),
    AMEACA("Ameaça", "Caso um jogador tenha sido ameaçado, efetuar essa punição no ameaçador.",
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("7d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("14d"))),
    ANTI_JOGO_JOGO("Anti-jogo (Jogo)", "Utilizar de aplicativos externos para ganho de vantagem.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("30d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("0"))),
    ANTI_JOGO_CHAT("Atitude de discriminação", "Atitude racista, homofóbica, xenofóbica ou discriminatória para com outro jogador.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("1d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("3d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("7d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("15d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("0"))),
    COMERCIO("Comércio", "Negociar contas, produtos ou serviços dentro de nosso servidor através de vendas em dinheiro real ou trocas.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("1d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("3d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("7d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("15d"))),
    DESINFORMACAO("Desinformação", "Divulgação de informações incorretas ou enganosas.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("3d"))),
    DIVULGACAO_SIMPLES("Divulgação", "Divulgar sites, canais ou vídeo no youtube que possuam ou não relação indireta com o servidor. Não incluindo links do fórum ou site da Aurora.",
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("12h")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("1d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("3d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("7d"))),
    DIVULGACAO_GRAVE("Divulgação de servidores", "Divulgação de informações graves ou sensíveis.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("0"))),
    FLOOD("Flood ou Spam", "Enviar mensagens repetidas ou desnecessárias no chat do lobby, minigames ou /tell.",
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("2h")),
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("5h")),
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("1d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("3d"))),
    HACK("Hack", "Utilizar de aplicativos externos para ganho de vantagem.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("90d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("0"))),
    NICKNAMEINADEQUADO("Nickname inapropriado", "Uso de nickname inapropriado ou ofensivo.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("0"))),
    OFENSA_JOGADOR("Ofensa a jogador", "Ofender outro jogador.",
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("5h")),
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("12h")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("1d")),
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("3d"))),
    OFENSA_STAFF("Ofensa à equipe ou ao servidor", "Ofensas direcionadas à equipe ou ao servidor.",
            new PunishmentType(PunishmentType.Type.MUTE, TimeUtils.durationToMillis("5d"))),
    CROSS_TEAMING("Time ou aliança", "Formar time ou aliança não permitida.",
            new PunishmentType(PunishmentType.Type.BAN, TimeUtils.durationToMillis("7d")));

    private final String name;
    private final String description;
    private final PunishmentType[] types;

    PunishmentReason(String name, String description, PunishmentType... types) {
        this.name = name;
        this.description = description;
        this.types = types;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public PunishmentType[] getTypes() {
        return types;
    }
}
