# Starts Tomcat 9 and a Cloudflare quick tunnel. Keep this window open during review.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$mvn = Join-Path $root ".tools\apache-maven-3.9.9\bin\mvn.cmd"
$tomcat = Join-Path $root ".tools\apache-tomcat-9.0.108"
$war = Join-Path $root "target\sammart.war"
$tunnel = Join-Path $root ".tools\cloudflared.exe"

if (-not (Test-Path $war)) {
    & $mvn -B -f (Join-Path $root "pom.xml") -DskipTests package
}
Copy-Item $war (Join-Path $tomcat "webapps\sammart.war") -Force
$env:CATALINA_HOME = $tomcat
Start-Process -FilePath (Join-Path $tomcat "bin\catalina.bat") -ArgumentList "run" -WorkingDirectory $tomcat
Start-Sleep -Seconds 8
& $tunnel tunnel --url http://localhost:8080
