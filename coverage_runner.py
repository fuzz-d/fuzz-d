#!/usr/bin/env python3

import time
import os
import pathlib
import random
import sys

ROOT = os.path.dirname(os.path.realpath(__file__))

from distutils.dir_util import copy_tree

class Runner():
    def run(self, seed, output_dir):
        pass

class FuzzdRunner(Runner):
    def run(self, seed, output_dir):
        return_code = os.system(f'timeout 30 java -jar app/build/libs/app.jar fuzz -n -s {seed} -o {output_dir}')
        return return_code

class XDSmithRunner(Runner):
    def run(self, seed, output_dir):
        return os.system(f'racket xdsmith/fuzzer.rkt --dafny-syntax true --seed {seed} > {output_dir}/main.dfy')

def generate_random_seed():
    return random.randint(-1 * 2 ** 63, 2 ** 63 - 1)

def generate_program(output_log, runner: Runner):
    seed = generate_random_seed()

    output_dir = pathlib.Path(ROOT) / "coverage_experiment" / f'seed{seed}'
    output_dir.mkdir(parents=True, exist_ok=True)
    output_file = output_dir / "main.dfy"

    return_code = runner.run(seed, output_dir)
    if return_code == 0:
        os.system(f'/bin/bash -c "echo {seed} >> {output_log}"')
    return return_code, output_file
    
def run_coverage(program, coverage_report_json, coverage_report_cobertura):
    os.system(f'./coverlet.sh {program} {coverage_report_json} {coverage_report_cobertura}')

# usage: coverage_runner.py RUN_NAME OUTPUT_DIR 
if __name__ == '__main__':
    for i in range(1, 3):
        run_name = sys.argv[1]
        runner = FuzzdRunner() if run_name == "fuzzd" else XDSmithRunner()
        output_dir = pathlib.Path(sys.argv[2]) / "results" / f"{run_name}{i}"
        coverage_dir = output_dir / "coverage"

        output_dir.mkdir(parents=True, exist_ok=True)
        coverage_dir.mkdir(parents=True, exist_ok=True)

        output_log = output_dir / "run.log" 
        coverage_report_json = coverage_dir / "coverage.json"
        coverage_report_cobertura = coverage_dir / "coverage.cobertura.xml"

        output_log.write_text(f'Git commit: ')
        os.system(f'/bin/bash -c "git rev-parse HEAD >> {output_log}"')

        start_time = time.time()
        timeout_secs = 60 * 60 * 12
        checkpoints = [2, 4, 6, 8, 10, 12]
        checkpoints_saved = [False] * len(checkpoints)
        while (time.time() < start_time + timeout_secs):
            for (i, checkpoint) in enumerate(checkpoints):
                if (not checkpoints_saved[i] and time.time() >= start_time + checkpoint * 60 * 60):
                    coverage_checkpoint_dir = output_dir / f'coverage_{checkpoint}'
                    copy_tree(str(coverage_dir), str(coverage_checkpoint_dir))
                    checkpoints_saved[i] = True

            return_code, generated_file = generate_program(output_log, runner)
            if return_code == 0:
                run_coverage(generated_file, coverage_report_json, coverage_report_cobertura)

