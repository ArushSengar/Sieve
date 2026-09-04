@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.

if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
if "%ANDROID_HOME%"=="" set ANDROID_HOME=C:\Users\Arush\AppData\Local\Android\Sdk
set PATH=%JAVA_HOME%\bin;%PATH%

if exist "C:\Users\Arush\gradle\gradle-8.4\bin\gradle.bat" (
    call "C:\Users\Arush\gradle\gradle-8.4\bin\gradle.bat" %*
    exit /b %ERRORLEVEL%
)

echo Gradle not found in C:\Users\Arush\gradle\gradle-8.4\bin\gradle.bat
exit /b 1
