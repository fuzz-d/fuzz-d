FILE=$1
OUTPUT=$2
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:cs $FILE" --merge-with $OUTPUT.json -f json -f cobertura -o $OUTPUT
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:java $FILE" --merge-with $OUTPUT.json -f json -f cobertura -o $OUTPUT
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:py $FILE" --merge-with $OUTPUT.json -f json -f cobertura -o $OUTPUT
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:go $FILE" --merge-with $OUTPUT.json -f json -f cobertura -o $OUTPUT
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /noVerify /compile:2 /compileTarget:js $FILE" --merge-with $OUTPUT.json -f json -f cobertura -o $OUTPUT
