param([string[]]$Tasks = @('build'))
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/java.exe'))) {
    throw 'Set JAVA_HOME to your Java 25 JDK before building.'
}
& gradle @Tasks --console=plain
exit $LASTEXITCODE
