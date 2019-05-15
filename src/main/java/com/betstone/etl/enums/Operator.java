package com.betstone.etl.enums;

public enum Operator {
    BETSTONE("Betstone");

    private final String operatorName;

    Operator(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorName() {
        return operatorName;
    }
}
