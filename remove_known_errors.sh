for dir in result/java_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "Compiler crash: true" >/dev/null); then rm -rf $dir; fi
done

for dir in result/java_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "incompatible types" >/dev/null); then rm -rf $dir; fi
done

for dir in result/java_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "incompatible bounds" >/dev/null); then rm -rf $dir; fi
done

for dir in result/java_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "no suitable method" >/dev/null); then rm -rf $dir; fi
done

for dir in result/java_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "lambda" >/dev/null); then rm -rf $dir; fi
done

for dir in result/java_crash/*; do 
	if $(cat $dir/fuzz-d.log | grep "unreachable statement" >/dev/null); then rm -rf $dir; fi
done

for dir in result/execute_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "CodePoint" >/dev/null); then rm -rf $dir; fi
done

for dir in result/resolver/*; do
	if $(cat $dir/fuzz-d.log | grep "All elements of display must have some common supertype" >/dev/null); then rm -rf $dir; fi
done

for dir in result/resolver/*; do
	if $(cat $dir/fuzz-d.log | grep "type of left argument to +" >/dev/null); then rm -rf $dir; fi
done

for dir in result/resolver/*; do
	if $(cat $dir/fuzz-d.log | grep "type parameter is not declared in this scope" >/dev/null); then rm -rf $dir; fi
done

for dir in result/resolver/*; do 
	if $(cat $dir/fuzz-d.log | grep "Error: the type of this expression is underspecified" >/dev/null); then rm -rf $dir; fi
done 

for dir in result/compiler_crash/*; do
        if $(cat $dir/fuzz-d.log | grep "All elements of display must have some common supertype" >/dev/null); then rm -rf $dir; fi
done

for dir in result/compiler_crash/*; do
        if $(cat $dir/fuzz-d.log | grep "type of left argument to +" >/dev/null); then rm -rf $dir; fi
done

for dir in result/compiler_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "type parameter is not declared in this scope" >/dev/null); then rm -rf $dir; fi
done

for dir in result/compiler_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "error CS0103" >/dev/null); then rm -rf $dir; fi
done

for dir in result/compiler_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "at Microsoft.Dafny.Translator.TrForall_NewValueAssumption(IToken tok, List\`1 boundVars, List\`1 bounds, Expression range, Expression lhs, Expression rhs, Attributes attributes, ExpressionTranslator etran, ExpressionTranslator prevEtran) in /home/alex/dafny/Source/DafnyCore/Verifier/Translator.TrStatement.cs:line 1348" >/dev/null); then rm -rf $dir; fi
done

for dir in result/compiler_crash/*; do 
	if $(cat $dir/fuzz-d.log | grep "Error: the type of this expression is underspecified" >/dev/null); then rm -rf $dir; fi
done 

for dir in result/compiler_crash/*; do
	if $(cat $dir/fuzz-d.log | grep "error CS1628" >/dev/null); then rm -rf $dir; fi
done

rm -rf result/success/*
