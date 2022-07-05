# -*- coding: utf-8 -*-

import datetime
import logging
import os
from itertools import islice
from os.path import isfile, join
import pandas as pd
from babel import numbers
from openpyxl import load_workbook
from pandas import DataFrame

path = "/Users/yairrobledo/Desktop/ETL/Mystery/Sin_Procesar"
pathP = "/Users/yairrobledo/Desktop/ETL/Mystery/Procesados"
dest = "/Users/yairrobledo/Desktop/Copia de ETL/Mystery"

onlyfiles = [f for f in os.listdir(path) if isfile(join(path, f))]
excelfiles = [f for f in onlyfiles if f.endswith('xlsx') and f[0] != "~"]
dataframeOuptut = pd.DataFrame(columns=['EGM', 'Date', 'Mystery Bonus Win', 'Game'])

print("--------- \tArchivo Mistery \t---------")
try:
    for file in excelfiles:
        if not file.endswith("xlsx"):
            pass
        dateformat = "%Y%m%d"
        # Carga los libros de Excel con el que trabajará
        dataset = load_workbook(filename="" + join(path, file) + "")
        # Copia el contenido del libro de la hoja actual
        data = dataset.active.values
        # Define el renglón [0:] (primer renglón) como nombre para las columnas
        cols = next(data)[0:]
        # Define al dataset de forma ordenada sin importar si es del mismo tipo, toma toda la hoja como conjunto
        # ordenado de datos
        data = list(data)
        "idx = [r[0] for r in data]"
        data = (islice(r, 0, None) for r in data)
        df = DataFrame(data, columns=cols)
        print("Archivo: " + file)
        dateFile = datetime.datetime.strptime(file[0:8], dateformat).date()
        df['Win Time'] = dateFile
        df['Win Time'] = pd.to_datetime(df['Win Time'], format="%Y-%m-%d")

        df.loc[:, 'EGM'] = df.loc[:, 'EGM'].replace(to_replace="[_]|[ ]|server|SERVER", value='',
                                                    regex=True)
        df['EGM'] = df['EGM'].apply(lambda x: str.upper(x))

        try:
            df.loc[:, ['Mystery Bonus Win']] = df.loc[:, ['Mystery Bonus Win']].applymap(
                lambda x: round(x, 2))
        except numbers.NumberFormatError as err:
            print('Error en parseo de número, intentando otro formato', err)
            df.loc[:, ['Mystery Bonus Win']] = df.loc[:, ['Mystery Bonus Win']].applymap(
                lambda x: round(x, 2))
        df.rename(index=str, columns={'Win Time': 'Date'}, inplace=True)
        df = df.loc[:, ['EGM', 'Date', 'Mystery Bonus Win', 'Game']]
        dataframeOuptut = dataframeOuptut.append(df, sort=False)
        print(df.info())
        print("Transformación exitosa y adjuntado a archivo final")
        print("Archivo adjuntado")
        dataset.save(join(pathP, file))
        print("Archivo original copiado a procesados")
        os.remove(join(path, file))

        dataframeOuptut.reset_index(drop=True, inplace=True)

        dataframeOuptut.loc[:, ['EGM']] = dataframeOuptut.loc[:, ['EGM']].astype(str)

        dataframeOuptut.loc[:, ['Game']] = dataframeOuptut.loc[:, ['Game']].astype(str)

    if (len(dataframeOuptut)) > 0:
        dataframeOuptut.to_csv(join(dest, "Mystery.csv"), header=False, index=False, encoding='utf-8')
        print("Archivo final terminado")
except Exception as err:
    print("Error en ejecución de ETL Mystery")
    print(err.__cause__)
    logging.exception(str(err))
    print(err.with_traceback(err.__traceback__))
print("--------- \tTerminado \t---------")
