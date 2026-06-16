@echo off

set APP_NAME=red
set SRC_DIR=./
set BUILD_DIR=bin
set LIB_DIR=lib
set SERVLET_API_JAR=%LIB_DIR%\javax.servlet-api-4.0.1.jar
set TEST_LIB=C:\Indra\Mr Naina\Red-Framework\lib

:: Créer le dossier bin s'il n'existe pas
if not exist %BUILD_DIR% mkdir %BUILD_DIR%

:: Lister tous les fichiers Java
dir /s /b %SRC_DIR%\*.java > sources.txt

:: Compilation
javac -cp "%SERVLET_API_JAR%" -d %BUILD_DIR% @sources.txt

:: Suppression du fichier temporaire
del sources.txt

:: Création du JAR
cd %BUILD_DIR%
jar -cvf %APP_NAME%.jar *
cd ..

:: Copie du JAR
copy /Y %BUILD_DIR%\%APP_NAME%.jar "%TEST_LIB%\"

echo Build termine.
pause