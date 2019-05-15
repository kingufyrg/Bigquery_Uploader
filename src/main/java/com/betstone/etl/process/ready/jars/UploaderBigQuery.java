package com.betstone.etl.process.ready.jars;

import bigquery.BigQueryUploader;

public class UploaderBigQuery {

    public static void main(String[] args) {
        BigQueryUploader uploader = new BigQueryUploader();
        uploader.uploadBigQueryFiles();
    }
}
