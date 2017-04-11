@echo off
:dynClasspath
set JAVA_HOME=D:\tools\java\
set BATCH_HOME=D:\code\DishonestyDataGet
set _LIBJARS=.;%BATCH_HOME%\lib
for %%i in (%BATCH_HOME%\lib\*.jar) do call %BATCH_HOME%\cpappend.bat %%i
set BATCH_CP=%_LIBJARS%
echo %BATCH_CP%

@taskkill /f /IM java.exe
start /min /w mshta vbscript:setTimeout("window.close()",2000)
cd classes
java -cp %BATCH_CP% -Xms256M -Xmx1024M com.Main
java -cp %BATCH_CP% -Xms256M -Xmx1024M com.Main_forzhangkl