REPORT=coverage/coverage.cobertura.xml
OUTPUT=coverage/result.json
for dir in ./coverage_testcases/*; do
echo "cs"
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:cs $dir/main.dfy" --merge-with $OUTPUT -f json
echo "java"
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:java $dir/main.dfy" --merge-with $OUTPUT -f json
echo "py"
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:py $dir/main.dfy" --merge-with $OUTPUT -f json
echo "go"
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:go $dir/main.dfy" --merge-with $OUTPUT -f json
echo "js"
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:js $dir/main.dfy" --merge-with $OUTPUT -f cobertura -o $REPORT
; done