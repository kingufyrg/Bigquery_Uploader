package com.betstone.etl.enums;

import com.betstone.etl.models.JsonScorecardSchemas;

public enum BigQueryTable {
    GAME_PROFIT("EGMGameProfit", JsonScorecardSchemas.GAME_PROFIT),
    EGM("ScorecardEGM2", JsonScorecardSchemas.EGM),
    ASSETS_DAILY("AssetsDaily2", JsonScorecardSchemas.ASSETS),
    MISTERY("MysteryEGM", JsonScorecardSchemas.MISTERY),
    EGM_INVOICING("ScorecardEGM2_Invoicing", JsonScorecardSchemas.EGM),
    ASSETS_INVOICING("AssetsDaily2_Invoicing", JsonScorecardSchemas.ASSETS),
    GAME_PROFIT_INVOICING("EGMGameProfit_Invoicing", JsonScorecardSchemas.GAME_PROFIT),
    PLAYERS_EXPERIENCE("PlayersExperience", JsonScorecardSchemas.PLAYER_EXPERIENCE);


    private final String name;
    private final String schema;

    BigQueryTable(String name, String schema) {
        this.name = name;
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }
}
