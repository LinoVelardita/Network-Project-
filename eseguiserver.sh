#!/bin/bash


cd ./ServerOut
java -cp .:../JACKSON/jackson-annotations-2.11.2.jar:../JACKSON/jackson-core-2.11.2.jar:../JACKSON/jackson-databind-2.11.2.jar MainServer
