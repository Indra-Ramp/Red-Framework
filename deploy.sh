#!/bin/bash

APP_NAME="red"
SRC_DIR="./"
BUILD_DIR="bin"
LIB_DIR="lib"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
TEST_LIB="/home/itu/Documents/L2/Web Dynamique/Red/Test/lib"

find $SRC_DIR -name "*.java" > sources.txt
javac -cp $SERVLET_API_JAR -d $BUILD_DIR/ @sources.txt
# rm sources.txt

# Générer le fichier .jar dans le dossier bin
cd $BUILD_DIR || exit
jar -cvf $APP_NAME.jar *
cd ..

# 
cp -f $BUILD_DIR/$APP_NAME.jar "$TEST_LIB/"
