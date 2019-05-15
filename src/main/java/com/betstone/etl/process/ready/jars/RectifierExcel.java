package com.betstone.etl.process.ready.jars;

import com.betstone.etl.process.ready.ProfitVerificator;

public class RectifierExcel {

    public static void main(String[] args) {
        ProfitVerificator verifier = new ProfitVerificator();
        verifier.rectificationProcess(true);
    }
}
