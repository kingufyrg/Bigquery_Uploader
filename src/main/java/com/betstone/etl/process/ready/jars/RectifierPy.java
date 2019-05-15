package com.betstone.etl.process.ready.jars;

import com.betstone.etl.process.ready.ProfitVerificator;

public class RectifierPy {

    public static void main(String[] args) {
        ProfitVerificator verifier = new ProfitVerificator();
        verifier.rectificationProcess(false);
    }
}
