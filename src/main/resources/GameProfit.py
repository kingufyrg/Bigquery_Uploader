# -*- coding: utf-8 -*-
"""

@author: Gabriel Quintanar
"""
import logging
import os
import pandas as pd
from babel import numbers
from os.path import isfile, join



path = "//187.188.105.168/shared/Files_GamesProfit/Sin_Procesar_Daily"
pathProcessed = "//187.188.105.168/shared/Files_GamesProfit/Procesados_Daily"
dest = "//187.188.105.168/shared/Files_2Databases/EGMGameProfit_Daily"

# path = "C:/Users/Yonathan Diaz/Documents/NewETLPython/GP"
# pathProcessed = "C:/Users/Yonathan Diaz/Documents/NewETLPython/GP_P"
# dest = "C:/Users/Yonathan Diaz/Documents/NewETLPython/GP_P"

pathInv = "//187.188.105.168/shared/Files_GamesProfit_Invoicing/Sin_Procesar_Daily"
pathPInv = "//187.188.105.168/shared/Files_GamesProfit_Invoicing/Procesados_Daily"
destInv = "//187.188.105.168/shared/Files_2Databases/EGMGameProfit_Daily_Invoicing"


def gameProfitETL(path, pathP, dest):
    print("--------- \tArchivo GameProfit \t---------")
    try:
        onlyfiles = [f for f in os.listdir(path) if isfile(join(path, f))]
        excelfiles = [f for f in onlyfiles if f.endswith('xlsx')]
        dataframeOuptut = pd.DataFrame(
            columns=['Date', 'Day of Week', 'EGM', 'Game', 'Total In', 'Total Out', 'Games Played', 'Games Won',
                     'Largest Win', 'Max Bet'])
        for file in excelfiles:
            dataset = pd.read_excel(join(path, file), header=0, encoding='utf-8')
            print("Archivo: " + file)
            print(dataset.info())
            dataset.loc[:, 'EGM'] = dataset.loc[:, 'EGM'].replace(to_replace="[_]|[ ]|server|SERVER", value='',
                                                                  regex=True)
            dataset['EGM'] = dataset['EGM'].apply(lambda x: str.lower(x))

            print(dataset.loc[:, 'EGM'])
            """dataset.loc[:, ['Total In', 'Total Out', 'Games Played', 'Games Won']] = dataset.loc[:, ['Total In',
                                                                                                     'Total Out',
                                                                                                     'Games Played',
                                                                                                     'Games Won']].\
                replace(to_replace="[%]", value="", regex=True).replace(to_replace='[nan]', value='0', regex=True)
            try:
                dataset.loc[:, ['Total In', 'Total Out', 'Games Played',
                                'Games Won']] = dataset.loc[:, ['Total In', 'Total Out',
                                                                'Games Played', 'Games Won']].applymap(
                    lambda x: numbers.parse_decimal(x, 'en_US')).astype(float).round(2)
            except numbers.NumberFormatError as err:
                print('Error en parseo de número, intentando otro formato', err)
                dataset.loc[:, ['Total In', 'Total Out', 'Games Played',
                                'Games Won']] = dataset.loc[:, ['Total In', 'Total Out',
                                                                'Games Played', 'Games Won']].applymap(
                    lambda x: numbers.parse_decimal(x, 'de')).astype(float).round(2)
            dataset = dataset.loc[:, ['EGM', 'Game', 'Total In', 'Total Out', 'Games Played', 'Games Won', 'Date']]
            dataframeOuptut = dataframeOuptut.append(dataset, sort=False)
            print("Archivo adjuntado")
            dataset = pd.read_csv(join(path, file), sep=';', dtype=str, encoding='utf-8')
            os.remove(join(path, file))
            print("Archivo original removido")
            dataset.to_csv(join(pathP, file), index=False, header=False, encoding='utf-8')
            print("Archivo original copiado a procesados")
            dataframeOuptut.reset_index(drop=True, inplace=True)
        dataframeOuptut.loc[:, ['EGM', 'Game']] = dataframeOuptut.loc[:, ['EGM', 'Game']].astype(str)
        dataframeOuptut.loc[:, ['Games Played', 'Games Won']] = dataframeOuptut.loc[:,
                                                                ['Games Played', 'Games Won']].astype('int64')
        if len(dataframeOuptut) > 0:
            dataframeOuptut.to_csv(join(dest, "GamesProfitResult.csv"), header=False, index=False, encoding='utf-8')
            print("Archivo final terminado")"""
    except Exception as error:
        print("Error en ejecución de ETL Game Profit")
        print(error.__cause__)
        logging.exception(str(error))
        print(error.with_traceback(error.__traceback__))
    print("--------- \tTerminado \t---------")


gameProfitETL(path, pathProcessed, dest)
gameProfitETL(pathInv, pathPInv, destInv)
