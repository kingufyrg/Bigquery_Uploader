package com.betstone.etl.process.ready.jars;

import com.betstone.etl.io.PythonTransformation;

public class PythonExecuter {
    public static void main(String[] args) {
        PythonTransformation py = new PythonTransformation();
        py.execute();
    }
}
