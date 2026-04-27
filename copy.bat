@echo off
setlocal

:: Variables
set "srcDir=C:\dev\eng\mdpjob"
set "toDir=C:\dev\eng_mdpjob"

:: Create a temporary exclusion list
set "excludeFile=%TEMP%\xcopy_exclude.txt"
echo .idea> "%excludeFile%"
echo .git>> "%excludeFile%"

:: Copy excluding .idea and .git (no quotes around exclude file path)
xcopy /y /s /e /h "%srcDir%" "%toDir%" /EXCLUDE:%excludeFile%

:: Clean up
del "%excludeFile%"

echo Copy Done.

endlocal