# Download and setup Maven
Write-Host "Downloading Maven..." -ForegroundColor Green
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$mavenZip = "maven.zip"
$mavenDir = "apache-maven-3.9.6"

# Download Maven
Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip

# Extract Maven
Write-Host "Extracting Maven..." -ForegroundColor Green
Expand-Archive -Path $mavenZip -DestinationPath "." -Force

# Clean up zip file
Remove-Item $mavenZip -Force

# Set environment variables
$env:MAVEN_HOME = (Get-Location).Path + "\" + $mavenDir
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:PATH = $env:MAVEN_HOME + "\bin;" + $env:PATH

Write-Host "Maven setup complete!" -ForegroundColor Green
Write-Host "MAVEN_HOME: $env:MAVEN_HOME" -ForegroundColor Yellow

# Test Maven
Write-Host "Testing Maven..." -ForegroundColor Green
& "$env:MAVEN_HOME\bin\mvn.cmd" --version
