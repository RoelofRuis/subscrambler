@ECHO off
IF "%1"=="" GOTO ParseFault
	IF NOT EXIST "%CD%\%1" GOTO MissingDir
		CD %1
		SET CLASSPATH=%CD%
		DIR /S /B *.java > sources.txt
		javac @sources.txt
		ERASE sources.txt
		CD ..
		GOTO:EOF
:ParseFault
ECHO Error: No argument given; expects subdirectory.
GOTO:EOF
:MissingDir
ECHO Error: Nonexistent subdirectory %1.