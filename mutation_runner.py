#!/usr/bin/env python3
# (adjust StrykerTests.csproj include)
# OUTPUT DIRECTORY EXPECTED TO BE DAFNY/Test/<tool>

import time
import os
import pathlib
import random
import sys

ROOT = os.path.dirname(os.path.realpath(__file__))

from distutils.dir_util import copy_tree

FILE_NAME = 1

class Runner():
    def run(self, seed, output_dir):
        pass

    def execute(self, output_dir):
        global FILE_NAME
        os.system(f'echo "// RUN: %dafny /noVerify /compile:4 /compileVerbose:0 /compileTarget:py \\"%s\\" > \\"%t\\"" > "{output_dir}/{FILE_NAME}.dfy"')
        os.system(f'echo "// RUN: %dafny /noVerify /compile:4 /compileVerbose:0 /compileTarget:js \\"%s\\" > \\"%t\\"" >> "{output_dir}/{FILE_NAME}.dfy"')
        os.system(f'echo "// RUN: %dafny /noVerify /compile:4 /compileVerbose:0 /compileTarget:go \\"%s\\" > \\"%t\\"" >> "{output_dir}/{FILE_NAME}.dfy"')
        os.system(f'echo "// RUN: %dafny /noVerify /compile:4 /compileVerbose:0 /compileTarget:cs \\"%s\\" > \\"%t\\"" >> "{output_dir}/{FILE_NAME}.dfy"')
        os.system(f'echo "// RUN: %diff \\"%s.expect\\" \\"%t\\"" >> "{output_dir}/{FILE_NAME}.dfy"')
        os.system(f'cat {output_dir}/main.dfy >> {output_dir}/{FILE_NAME}.dfy')
        
        dafny_return_code = os.system(f'dafny /noVerify /compile:4 /compileTarget:py /compileVerbose:0 {output_dir}/{FILE_NAME}.dfy > {output_dir}/{FILE_NAME}.dfy.expect')        
        dafny_return_code &= os.system(f'dafny /noVerify /compile:4 /compileTarget:js /compileVerbose:0 {output_dir}/{FILE_NAME}.dfy >> {output_dir}/{FILE_NAME}.dfy.expect')        
        dafny_return_code &= os.system(f'dafny /noVerify /compile:4 /compileTarget:go /compileVerbose:0 {output_dir}/{FILE_NAME}.dfy >> {output_dir}/{FILE_NAME}.dfy.expect')        
        dafny_return_code &= os.system(f'dafny /noVerify /compile:4 /compileTarget:cs /compileVerbose:0 {output_dir}/{FILE_NAME}.dfy >> {output_dir}/{FILE_NAME}.dfy.expect')        
        os.system(f'rm {output_dir}/main.dfy')
        
        if dafny_return_code == 0:
            FILE_NAME += 1
        else:
            os.system(f'rm {output_dir}/{FILE_NAME}.*')
        return dafny_return_code

class FuzzdRunner(Runner):
    def run(self, seed, output_dir):
        return os.system(f'timeout 120 java -jar app/build/libs/app.jar fuzz -n -s {seed} -o {output_dir}')

class XDSmithRunner(Runner):
    def run(self, seed, output_dir):
        return os.system(f'timeout 120 racket xdsmith/fuzzer.rkt --dafny-syntax true --seed {seed} > {output_dir}/main.dfy')

class DafnyFuzzRunner(Runner):
    def __init__(self):
        os.system(f'javac -cp src/main/java/ -d ./out/ src/main/java/Main/BaseProgram.java')

    def run(self, seed, output_dir):
        return_code = os.system(f'timeout 120 java -cp out/ Main.BaseProgram {seed}')
        if return_code == 0:
            os.system(f'mv tests/test.dfy {output_dir}/main.dfy')
        return return_code

def generate_random_seed():
    return random.randint(-1 * 2 ** 63, 2 ** 63 - 1)

def generate_program(output_log, runner: Runner):
    seed = generate_random_seed()

    output_dir.mkdir(parents=True, exist_ok=True)
    output_file = output_dir / "main.dfy"

    return_code = runner.run(seed, output_dir)
    execute_return_code = return_code
    if return_code == 0:
        os.system(f'/bin/bash -c "echo {seed} >> {output_log}"')
        execute_return_code = runner.execute(output_dir)

    return execute_return_code, output_file
    
def run_coverage(program, coverage_report_json, coverage_report_cobertura):
    os.system(f'./coverlet.sh {program} {coverage_report_json} {coverage_report_cobertura}')

# usage: coverage_runner.py RUN_NAME OUTPUT_DIR 
if __name__ == '__main__':
    run_name = sys.argv[1]
    runner = FuzzdRunner() if run_name == "fuzzd" else (XDSmithRunner() if run_name == "xdsmith" else DafnyFuzzRunner())
    output_dir = pathlib.Path(sys.argv[2])

    output_dir.mkdir(parents=True, exist_ok=True)

    output_log = output_dir / "run.log" 

    output_log.write_text(f'Git commit: ')
    os.system(f'/bin/bash -c "git rev-parse HEAD >> {output_log}"')

    start_time = time.time()
    timeout_secs = 300
    while (time.time() < start_time + timeout_secs):
        return_code, generated_file = generate_program(output_log, runner)


