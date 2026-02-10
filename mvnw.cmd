@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM://maven.apache.org/download.cgi
@REM Maven Wrapper script for Windows

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar

@REM Determine Maven install directory
set MAVEN_USER_HOME=%USERPROFILE%\.m2\wrapper

@REM Read properties
for /f "usebackq tokens=1,* delims==" %%a in ("%MAVEN_WRAPPER_PROPERTIES%") do (
    if "%%a"=="distributionUrl" set MAVEN_DIST_URL=%%b
    if "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
)

@REM Extract version from URL
for %%i in (%MAVEN_DIST_URL%) do set MAVEN_ZIP=%%~nxi
set MAVEN_VERSION=%MAVEN_ZIP:-bin.zip=%

set MAVEN_HOME=%MAVEN_USER_HOME%\dists\%MAVEN_VERSION%

@REM Download and extract if not present
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven from %MAVEN_DIST_URL%...
    if not exist "%MAVEN_USER_HOME%\dists" mkdir "%MAVEN_USER_HOME%\dists"
    
    powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest '%MAVEN_DIST_URL%' -OutFile '%MAVEN_USER_HOME%\dists\maven.zip' }"
    
    echo Extracting Maven...
    powershell -Command "& { Expand-Archive -Path '%MAVEN_USER_HOME%\dists\maven.zip' -DestinationPath '%MAVEN_USER_HOME%\dists' -Force }"
    del "%MAVEN_USER_HOME%\dists\maven.zip"
)

@REM Run Maven
"%MAVEN_HOME%\bin\mvn.cmd" %*
