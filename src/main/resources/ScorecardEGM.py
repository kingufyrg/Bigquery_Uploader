# -*- coding: utf-8 -*-
"""

Script encargado de la transformaciÃ³n de los archivos de la tabla ScorecardEGM2,
la cual se encarga de la informaciÃ³n agregada a nivel de mÃ¡quinas. El script
define un método para tomar distintos directorios, dÃ³nde tomar los archivos originales,
dÃ³nde depositarlos como respaldo, dÃ³nde depositar los procesados de ScorecardEGM2 y 
AssetsDaily2.
      

Notas:
      Al hacer pruebas de comprobaciÃ³n con la informaciÃ³n obtenida desde el Macro de Excel
      se encontraron diferencias entre el resultado de este script y el resultado del macro.
      Se encontrÃ³ que la codificaciÃ³n no se habÃ­a estandarizado para el Macro. Por lo que
      las tablas de BigQuery se encuentran con una codificaciÃ³n 'inadecuada'.
      Se recomendarÃ­a hacer una limpieza de datos y poder implementar en el Macro una limpieza
      de caracteres a UTF-8 (codificaciÃ³n estandar comÃºn) para poder hacer no sÃ³lo uso
      de transformaciÃ³n de datos con Python, sino con cualquier otra herramienta.
      
      Hasta el momento, se solucionÃ³ este problema de compatibilidades haciendo explÃ­citamente
      la definiciÃ³n de la codificaciÃ³n en la lectura y la escritura de los archivos. Hasta el
      momento, la forma que ha permitido "empatar" las cantidades de un archivo ha sido
      leer los archivos descargados de Scorecard como UTF-8, y guardÃ¡rlos nuevamente como UTF-8.
      En teorÃ­a, Python hace uso de esta codificaciÃ³n por default para el manejo de archivos,
      pero se ha requerido de hacerlo explÃ­cito debido a los errores provocados.


@author: Gabriel Quintanar
"""

import datetime as dt
import os
from os.path import isfile, join

import pandas as pd
from babel import numbers

path = "//187.188.105.168/shared/Files_Scorecard2/Sin_Procesar_Daily"
pathProcessed = "//187.188.105.168/shared/Files_Scorecard2/Procesados_Daily"
dest = "//187.188.105.168/shared/Files_2Databases/ScorecardEGM2_Daily"

destAsset = "//187.188.105.168/shared/Files_2Databases/AssetsDaily2"

# path = "C:/Users/Yonathan Diaz/Documents/NewETLPython/EGM"
# pathProcessed = "C:/Users/Yonathan Diaz/Documents/NewETLPython/EGM_P"
# dest = "C:/Users/Yonathan Diaz/Documents/NewETLPython/EGM_P"

# destAsset = "C:/Users/Yonathan Diaz/Documents/NewETLPython/EGM_P"

pathI = "//187.188.105.168/shared/Files_Scorecard2_Invoicing/Sin_Procesar_Daily"
pathPI = "//187.188.105.168/shared/Files_Scorecard2_Invoicing/Procesados_Daily"
destI = "//187.188.105.168/shared/Files_2Databases/ScorecardEGM2_Daily_Invoicing"

destAssetI = "//187.188.105.168/shared/Files_2Databases/AssetsDaily2_Invoicing"


def egmETL(path, pathP, destE, destA):
    """
    FunciÃ³n que ejecuta toda la transformaciÃ³n de datos para ScorecardEGM2.
      
    Args:
         **path** (str): Directorio desde donde se recogen todos los archivos descargados sin procesar.
         
         **pathP** (str): Directorio en el cual se depositan todos los archivos originales acabados de procesar.
         
         **destE** (str): Directorio final donde se depositan los archivos finales, procesados, que corresponden a la tabla ScorecardEGM2.
         
         **destA** (str): Directorio final donde se depositan los archivos finales, procesados, que corresponden a la tabla AssetsDaily2.
    """
    print("--------- /tArchivo ScorecardEGM /t---------")
    try:

        dateformat = "%Y%m%d"
        onlyfiles = [f for f in os.listdir(path) if isfile(join(path, f))]
        csvfiles = [f for f in onlyfiles if f.endswith('csv')]
        dfEGM = pd.DataFrame(
            columns=['EGM', 'Casino', 'Site', 'Area', 'Bank', 'Total In', 'Total Out', 'Net Win', 'GGR', 'Games Played',
                     'Games Won', 'aggDate'])

        for file in csvfiles:
            if not file.endswith(".csv"):
                pass
            dataset = pd.read_csv(join(path, file), sep=';', dtype=str, encoding='utf-8')
            print("Archivo: " + file)
            dateFile = dt.datetime.strptime(file[0:8], dateformat)
            dataset['aggDate'] = dateFile
            dataset.loc[:, ['EGM']] = dataset.loc[:, ['EGM']].replace(to_replace="[_]|[ ]|server|SERVER", value='',
                                                                      regex=True).applymap(lambda x: str.upper(x))
            dataset = dataset[
                ['EGM', 'Casino', 'Site', 'Area', 'Bank', 'Total In', 'Total Out', 'Net Win', 'GGR', 'Games Played',
                 'Games Won', 'aggDate']]
            dataset.loc[:, ['Total In', 'Total Out', 'Net Win',
                            'GGR', 'Games Played', 'Games Won']] = dataset.loc[:,
                                                                   ['Total In', 'Total Out', 'Net Win', 'GGR',
                                                                    'Games Played',
                                                                    'Games Won']].replace(to_replace="[%]", value="",
                                                                                          regex=True).replace(
                to_replace='[nan]', value='0',
                regex=True)
            try:
                dataset.loc[:, ['Total In', 'Total Out', 'Net Win',
                                'GGR', 'Games Played', 'Games Won']] = dataset.loc[:,
                                                                       ['Total In', 'Total Out', 'Net Win', 'GGR',
                                                                        'Games Played',
                                                                        'Games Won']].applymap(lambda x:
                                                                                               numbers.parse_decimal(x,
                                                                                                                     'en_US')).astype(
                    dtype='double')
            except numbers.NumberFormatError as err:
                print('Error en parseo de nÃºmero, intentando otro formato', err)
                dataset.loc[:, ['Total In', 'Total Out', 'Net Win',
                                'GGR', 'Games Played', 'Games Won']] = dataset.loc[:,
                                                                       ['Total In', 'Total Out', 'Net Win', 'GGR',
                                                                        'Games Played',
                                                                        'Games Won']].applymap(lambda x:
                                                                                               numbers.parse_decimal(x,
                                                                                                                     'de')).astype(
                    dtype='double')
            dataset[['Net Win']] = dataset[['Net Win']].astype(float).round(2)
            dfEGM = dfEGM.append(dataset)
            print("TransformaciÃ³n exitosa y adjuntado a archivo final")
            dataset = pd.read_csv(join(path, file), sep=';', dtype=str, encoding='utf-8')
            os.remove(join(path, file))
            print("Original removido")
            dataset.to_csv(join(pathP, file), sep=';', index=False, header=False, encoding='utf-8')
            print("Guardado en " + join(pathP, file))
            dfEGM.reset_index(drop=True, inplace=True)

        if (len(dfEGM) > 0):
            dfEGM.loc[:, ['EGM', 'Casino', 'Site', 'Area', 'Bank']] = dfEGM.loc[:,
                                                                      ['EGM', 'Casino', 'Site', 'Area', 'Bank']].astype(
                str)
            dfEGM.loc[:, ['Games Played', 'Games Won']] = dfEGM.loc[:, ['Games Played', 'Games Won']].astype('int64')
            dfEGM.to_csv(join(destE, "ScorecardResult.csv"), header=False, index=False, encoding='utf-8')
            print("Archivo final terminado")
            print("Construyendo archivo Assets")
            assets = pd.read_excel('//187.188.105.168/shared/ETLProcess.xlsm', sheet_name="HDMach")
            mistery = pd.read_excel("//187.188.105.168/shared/ETLProcess.xlsm", sheet_name="MysteryDB")
            dfEGM['definition'] = ["SD"] * len(dfEGM)
            dfEGM.rename(index=str, columns={'aggDate': 'Initial Date'}, inplace=True)
            merged = pd.merge(dfEGM, assets, how='left', on='EGM')
            merged.loc[:, 'cabinet'].fillna("Others", inplace=True)
            merged.loc[:, 'Type'].fillna("Slots", inplace=True)
            merged.loc[(merged['cabinet'] != "Others") & (merged['cabinet'] != "MiraStone"), 'definition'] = "HD"
            merged.loc[merged['cabinet'] == "MiraStone", 'definition'] = "Bingo"
            merged.loc[merged['definition'] == "Bingo", 'Type'] = "Bingo"
            merged.rename(index=str, columns={"Initial Date_x": 'Initial Date'}, inplace=True)
            mistery.rename(index=str, columns={'InitialDate': 'Initial Date'}, inplace=True)
            merged['MBVersion'] = "Not-MB"
            merged['MBType'] = "Not-MB"
            merged['MBLevels'] = "Not-MB"

            EGMFound = merged.loc[merged.EGM.isin(mistery.EGM), :]

            for x in EGMFound.EGM:
                dateDownload = EGMFound.loc[EGMFound.EGM == x, 'Initial Date'].values[0]
                dateMistery = mistery.loc[mistery.EGM == x, 'Initial Date'].values[0]
                if (dateDownload >= dateMistery):
                    merged.loc[merged.EGM == x, 'MBVersion'] = mistery.loc[mistery.EGM == x, 'MBVersion'].values[0]
                    merged.loc[merged.EGM == x, 'MBType'] = mistery.loc[mistery.EGM == x, 'MBType'].values[0]
                    merged.loc[merged.EGM == x, 'MBLevels'] = mistery.loc[mistery.EGM == x, 'MBLevels'].values[0]

            merged = merged.loc[:,
                     ['Initial Date', 'cabinet', 'definition', 'EGM', 'MBVersion', 'MBType', 'MBLevels', 'Type']]
            merged.loc[:, ['cabinet', 'definition', 'EGM', 'MBVersion', 'MBType', 'MBLevels', 'Type']] = merged.loc[:,
                                                                                                         ['cabinet',
                                                                                                          'definition',
                                                                                                          'EGM',
                                                                                                          'MBVersion',
                                                                                                          'MBType',
                                                                                                          'MBLevels',
                                                                                                          'Type']].astype(
                str)
            merged.to_csv(join(destA, "AssetsResults.csv"), header=False, index=False, encoding='utf-8')
    except Exception as error:
        print("Error en escritura de archivos Scorecard y Assets")
        print(error)
    print("--------- /tTerminado /t---------")


egmETL(path, pathProcessed, dest, destAsset)
egmETL(pathI, pathPI, destI, destAssetI)
