# -*- coding: utf-8 -*-"""@author: Yair Robledo"""import loggingimport osfrom itertools import isliceimport pandas as pdfrom os.path import isfile, joinfrom babel import numbersfrom openpyxl import load_workbook, Workbookfrom pandas import DataFramepath = "/Users/yairrobledo/Desktop/ETL/EGMGameProfit/Sin_Procesar"pathProcessed = "/Users/yairrobledo/Desktop/ETL/EGMGameProfit/Procesados"dest = "/Users/yairrobledo/Desktop/Copia de ETL/EGMGameProfit"# path = "C:/Users/Yonathan Diaz/Documents/NewETLPython/GP"# pathProcessed = "C:/Users/Yonathan Diaz/Documents/NewETLPython/GP_P"# dest = "C:/Users/Yonathan Diaz/Documents/NewETLPython/GP_P"pathInv = "/Volumes/shared/Files_GamesProfit_Invoicing/Sin_Procesar_Daily"pathPInv = "/Volumes/shared/Files_GamesProfit_Invoicing/Procesados_Daily"destInv = "/Volumes/shared/Files_2Databases/EGMGameProfit_Daily_Invoicing"def gameProfitETL(path, pathP, dest):    print("--------- \tArchivo GameProfit \t---------")    try:        onlyfiles = [f for f in os.listdir(path) if isfile(join(path, f))]        excelfiles = [f for f in onlyfiles if f.endswith('.xlsx') and f[0] != "~"]        dataframeOuptut = pd.DataFrame(            columns=['EGM', 'Game', 'Total In', 'Total Out', 'Games Played', 'Games Won', 'Fecha'])        for file in excelfiles:            dataset = load_workbook(filename="" + join(path, file) + "")            data = dataset.active.values            cols = next(data)[0:]            data = list(data)            "idx = [r[0] for r in data]"            data = (islice(r, 0, None) for r in data)            df = DataFrame(data, columns=cols)            print(df)            print(join(path, file))            print("Archivo: " + file)            print(df.info())            print(dataset)            df.loc[:, 'EGM'] = df.loc[:, 'EGM'].replace(to_replace="[_]|[ ]|server|SERVER", value='',                                                        regex=True)            df['EGM'] = df['EGM'].apply(lambda x: str.upper(x))            print(df.loc[:, 'EGM'])            """df.loc[:, ['Total In', 'Total Out', 'Games Played', 'Games Won']] = df.loc[:, ['Total In',                                                                                           'Total Out',                                                                                           'Games Played',                                                                                           'Games Won']]. \                replace(to_replace="[%]", value="", regex=True).replace(to_replace='[nan]', value='0', regex=True)"""            try:                df.loc[:, ['Total In', 'Total Out', 'Games Played',                           'Games Won']] = df.loc[:, ['Total In', 'Total Out', 'Games Played',                                                      'Games Won']].applymap(                    lambda x: round(x, 2))            except numbers.NumberFormatError as err:                print('Error en parseo de número, intentando otro formato', err)                df.loc[:, ['Total In', 'Total Out', 'Games Played',                           'Games Won']] = df.loc[:, ['Total In', 'Total Out',                                                      'Games Played', 'Games Won']].applymap(                    lambda x: round(x, 2))            df = df.loc[:, ['EGM', 'Game', 'Total In', 'Total Out', 'Games Played', 'Games Won', 'Fecha']]            dataframeOuptut = dataframeOuptut.append(df, sort=False)            print("Archivo adjuntado")            dataset.save(join(pathP, file))            print("Archivo original copiado a procesados")            os.remove(join(path, file))            print("Archivo original removido")            dataframeOuptut.reset_index(drop=True, inplace=True)        dataframeOuptut.loc[:, ['EGM', 'Game']] = dataframeOuptut.loc[:, ['EGM', 'Game']].astype(str)        dataframeOuptut.loc[:, ['Games Played', 'Games Won']] = dataframeOuptut.loc[:,                                                                ['Games Played', 'Games Won']].astype('int64')        if len(dataframeOuptut) > 0:            dataframeOuptut.to_csv(join(dest, "GamesProfitResult.csv"), header=False, index=False, encoding='utf-8')            print("Archivo final terminado")    except Exception as error:        print("Error en ejecución de ETL Game Profit")        print(error.__cause__)        logging.exception(str(error))        print(error.with_traceback(error.__traceback__))    print("--------- \tTerminado \t---------")gameProfitETL(path, pathProcessed, dest)gameProfitETL(pathInv, pathPInv, destInv)