@echo off

set APP_NAME=red
set SRC_DIR=./@echo off
setlocal

:: Nom de l'application
set APP_NAME=red

:: Répertoires
set SRC_DIR=.
set BUILD_DIR=bin
set LIB_DIR=lib
set SERVLET_API_JAR=%LIB_DIR%\*
set TEST_LIB=C:\Indra\Mr_Naina\Test-Framework\lib

:: Créer le dossier bin s'il n'existe pas
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

:: Lister tous les fichiers Java
dir /s /b "%SRC_DIR%\*.java" > sources.txt

:: Compiler les fichiers Java
javac -cp "%SERVLET_API_JAR%" -d "%BUILD_DIR%" @sources.txt

:: Vérifier si la compilation a réussi
if errorlevel 1 (
    echo Erreur lors de la compilation.
    pause
    exit /b 1
)

:: Supprimer le fichier temporaire (optionnel)
:: del sources.txt

:: Générer le fichier JAR
cd "%BUILD_DIR%"
"C:\Program Files\Java\jdk-21.0.11\bin\jar.exe" -cvf "%APP_NAME%.jar" *
cd ..

:: Copier le JAR dans le dossier de test
copy /Y "%BUILD_DIR%\%APP_NAME%.jar" "%TEST_LIB%"

echo.
echo ================================
echo Compilation terminee avec succes.
echo JAR genere : %BUILD_DIR%\%APP_NAME%.jar
echo ================================

