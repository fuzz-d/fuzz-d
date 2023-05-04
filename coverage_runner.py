#!/usr/bin/env python3

import time
import os
import pathlib
import random
import sys

ROOT = os.path.dirname(os.path.realpath(__file__))

from distutils.dir_util import copy_tree

class Runner():
    def run(seed, output_file):
        pass

class FuzzdRunner(Runner):
    def run(seed, output_file):
        os.system(f'fuzzd fuzz -n -s {seed} -o {output_file}')

class XDSmithRunner(Runner):
    def run(seed, output_file):
        os.system(f'racket xdsmith/fuzzer.rkt --seed {seed} > {output_file}')

def generate_random_seed():
    return random.randint(-1 * 2 ** 63, 2 ** 63 - 1)

def generate_program(output_log, runner: Runner):
    seed = generate_random_seed()
    output_log.write(f'{seed}\n')

    output_dir = pathlib.Path(ROOT) / "coverage_experiment" / time.time()
    output_dir.mkdir(parents=True, exists_ok=True)
    output_file = output_dir / "main.dfy"

    runner.run(seed, output_file)
    return output_file
    
def run_coverage(program, coverage_report_json, coverage_report_cobertura):
    os.system(f'./coverlet.sh {program} {coverage_report_json} {coverage_report_cobertura}')

# usage: coverage_runner.py RUN_NAME OUTPUT_DIR 
if __name__ == '__main__':
    run_name = sys.argv[1]
    runner = FuzzdRunner() if run_name == "fuzzd" else XDSmithRunner()
    output_dir = pathlib.Path(sys.argv[2]) / "results" / run_name
    coverage_dir = output_dir / "coverage"

    output_dir.mkdir(parents=True, exist_ok=True)
    coverage_dir.mkdir(parents=True, exist_ok=True)

    output_log = output_dir / "run.log" 
    coverage_report_json = coverage_dir / "coverage.json"
    coverage_report_cobertura = coverage_dir / "coverage.cobertura.xml"

    output_log.write_text(f'Git commit: ')
    os.system(f'git rev-parse HEAD >> {output_log}')

    output_log = output_log.open("a")
    output_log.write(f'test case seeds: ')

    start_time = time.time()
    timeout_secs = 60 * 60 * 24
    checkpoints = [1, 2, 6, 12, 24]
    checkpoints_saved = [False] * len(checkpoints)
    while (time.time() < start_time + timeout_secs):
        for (i, checkpoint) in enumerate(checkpoints):
            if (not checkpoints_saved[i] and time.time() >= start_time + checkpoint * 60 * 60):
                coverage_checkpoint_dir = output_dir / f'coverage_{checkpoint}'
                copy_tree(coverage_dir, coverage_checkpoint_dir)
                checkpoints_saved[i] = True

        generated_file = generate_program(output_log, runner)
        run_coverage(generated_file, coverage_report_json, coverage_report_cobertura)

