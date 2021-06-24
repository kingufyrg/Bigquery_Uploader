# -*- coding: utf-8 -*-
"""
Created on Mon Oct  8 12:31:24 2018

@author: Gabriel Quintanar
"""

import datetime as dt
import os
import re
from os.path import isfile, join

import pandas as pd
from babel import numbers

path = "//Volumes/shared/Files_Mystery/Sin_Procesar_Daily"
pathP = "//Volumes/shared/Files_Mystery/Procesados_Daily"
dest = "//Volumes/shared/Files_2Databases/MysteryEGM_Daily"

# path = "C:/Users/Yonathan Diaz/Documents/NewETLPython/M"
# pathP = "C:/Users/Yonathan Diaz/Documents/NewETLPython/M_P"
# dest = "C:/Users/Yonathan Diaz/Documents/NewETLPython/M_P"

dateformat = "%m/%d/%Y"

onlyfiles = [f for f in os.listdir(path) if isfile(join(path, f))]
csvfiles = [f for f in onlyfiles if f.endswith('csv')]
dataframeOuptut = pd.DataFrame(columns=['EGM', 'Date', 'Bonus Win Amount', 'Game Name'])

print("--------- \tArchivo Mistery \t---------")
try:
    for file in csvfiles:
        dataset = pd.read_csv(join(path, file), sep=";", encoding='utf-8')
        print("Archivo: " + file)
        dataset['Win Time'] = dataset['Win Time'].apply(lambda x: x[0:10])
        dataset['Win Time'] = dataset['Win Time'].apply(lambda x: dt.datetime.strptime(x, dateformat))
        dataset['EGM'] = dataset['EGM'].apply(lambda x: re.sub(r"[_]|[ ]|server|SERVER", '', x)).apply(
            lambda x: str.upper(x))
        dataset.loc[:, ['Bonus Win Amount']] = dataset.loc[:, ['Bonus Win Amount']].replace(to_replace="[%]",
                                                                                            value="",
                                                                                            regex=True).replace(
            to_replace='[nan]', value='0', regex=True)
        try:
            dataset.loc[:, ['Bonus Win Amount']] = dataset.loc[:, ['Bonus Win Amount']].applymap(
                lambda x: numbers.parse_decimal(x, 'en_US')).astype(float).round(2)
        except numbers.NumberFormatError as err:
            print('Error en parseo de número, intentando otro formato', err)
            dataset.loc[:, ['Bonus Win Amount']] = dataset.loc[:, ['Bonus Win Amount']].applymap(
                lambda x: numbers.parse_decimal(x, 'de')).astype(float).round(2)
        dataset.rename(index=str, columns={'Win Time': 'Date'}, inplace=True)
        dataframeOuptut = dataframeOuptut.append(dataset, sort=False)
        print("Archivo adjuntado.")
        dataset = pd.read_csv(join(path, file), sep=";", encoding='utf-8')
        os.remove(join(path, file))
        dataset.to_csv(join(pathP, file), index=False, header=False, encoding='utf-8')
        dataframeOuptut.reset_index(drop=True, inplace=True)
    if (len(dataframeOuptut) > 0):
        dataframeOuptut.loc[:, ['EGM', 'Game Name']] = dataframeOuptut.loc[:, ['EGM', 'Game Name']].astype(str)
        dataframeOuptut.to_csv(join(dest, "MysteryResult.csv"), header=False, index=False)
        print("Archivo final construido")
except Exception as err:
    print("Error en ejecución de ETL Mystery")
    print(err)
print("--------- \tTerminado \t---------")
