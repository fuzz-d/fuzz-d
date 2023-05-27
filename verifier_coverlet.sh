FILE=$1
OUTPUT=$2
REPORT=$3
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /compile:0 $FILE" --merge-with $OUTPUT -f json -o $OUTPUT
coverlet $DAFNYBIN --target "dotnet" --targetargs "$DAFNYBIN/Dafny.dll /compile:0 $FILE" --merge-with $OUTPUT -f cobertura -o $REPORT
