package com.betstone.etl.models;

public class JsonScorecardSchemas {
    public static final String GAME_PROFIT =
            "[{\"name\":\"EGM\",\"type\":\"STRING\",\"mode\":\"REQUIRED\"}," +
                    "{\"name\":\"game\",\"type\":\"STRING\",\"mode\":\"REQUIRED\"}," +
                    "{\"name\":\"totalBet\",\"type\":\"FLOAT\",\"mode\":\"REQUIRED\"}," +
                    "{\"name\":\"totalWin\",\"type\":\"FLOAT\",\"mode\":\"REQUIRED\"}," +
                    "{\"name\":\"totalPlays\",\"type\":\"INTEGER\",\"mode\":\"REQUIRED\"}," +
                    "{\"name\":\"totalWiningPlays\",\"type\":\"INTEGER\",\"mode\":\"REQUIRED\"}," +
                    "{\"name\":\"profitDate\",\"type\":\"DATE\",\"mode\":\"REQUIRED\"}]";

    public static final String EGM =
            "[{\"mode\":\"REQUIRED\",\"name\":\"device\",\"type\":\"STRING\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"operator\",\"type\":\"STRING\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"site\",\"type\":\"STRING\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"area\",\"type\":\"STRING\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"bank\",\"type\":\"STRING\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"wagerAmount\",\"type\":\"FLOAT\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"payoutAmount\",\"type\":\"FLOAT\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"netWin\",\"type\":\"FLOAT\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"GGR\",\"type\":\"FLOAT\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"numWagers\",\"type\":\"INTEGER\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"numPayouts\",\"type\":\"INTEGER\"}, " +
                    "{\"mode\":\"REQUIRED\",\"name\":\"aggDate\",\"type\":\"DATE\"}]";

    public static final String ASSETS =
            "[{\"mode\": \"REQUIRED\",\"name\": \"aggDate\",\"type\": \"DATE\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"cabinet\",\"type\": \"STRING\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"Definition\",\"type\":\"STRING\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"EGM\",\"type\": \"STRING\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"MBVersion\",\"type\": \"STRING\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"MBType\",\"type\": \"STRING\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"MBLevels\",\"type\": \"STRING\"}," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"Type\",\"type\": \"STRING\"}]";/**," +
                    "{\"mode\": \"REQUIRED\",\"name\": \"Library\",\"type\": \"STRING\"}]";**/

    public static final String MISTERY =
            "[{\"mode\":\"REQUIRED\",\"name\":\"EGM\",\"type\":\"STRING\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Date\",\"type\":\"DATE\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Bonus_Win_Amount\",\"type\":\"FLOAT\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Game\",\"type\":\"STRING\"}]";

    public static final String PLAYER_EXPERIENCE =
            "[{\"mode\":\"REQUIRED\",\"name\":\"ID\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Hour\",\"type\":\"TIME\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Balance\",\"type\":\"FLOAT\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Count\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Event_ID\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"Session_ID\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"REQUIRED\",\"name\":\"BetCountperSession\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Num_Lines\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Bet_per_line\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Denom\",\"type\":\"FLOAT\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"total_bet\",\"type\":\"FLOAT\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Type_Game\",\"type\":\"STRING\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Result\",\"type\":\"STRING\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Spin_Time_Seconds\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Game_Name\",\"type\":\"STRING\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"LOGDate\",\"type\":\"DATE\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Win_Amount\",\"type\":\"FLOAT\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Transaction\",\"type\":\"FLOAT\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Different_Games\",\"type\":\"INTEGER\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"Account\",\"type\":\"STRING\"}," +
                    "{\"mode\":\"NULLABLE\",\"name\":\"contador\",\"type\":\"INTEGER\"}]";
}
