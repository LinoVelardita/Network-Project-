#!/bin/bash

javac -d ./ServerOut -cp .:./JACKSON/jackson-annotations-2.11.2.jar:./JACKSON/jackson-core-2.11.2.jar:./JACKSON/jackson-databind-2.11.2.jar ./Server/src/*.java
