#!/usr/bin/env python3

import time
import os

from distutils.dir_util import copy_tree

def getErrorDirectories():
    resolution_dirs = []
    java_crash_dirs = []
    compiler_crash_dirs = []
    execute_crash_dirs = []
    different_output_dirs = []
    timeout_dirs = []
    directory = 'output'
    filename = 'fuzz-d.log'
    for dir_name in os.listdir(directory):
        if os.path.isfile(os.path.join(directory, dir_name)):
            continue

        f = os.path.join(directory, dir_name, filename)
        if not os.path.isfile(f):
            continue

        with open(f) as open_file:
            file_contents = open_file.read()
            if 'resolution/type errors detected' in file_contents:
                resolution_dirs += [str(dir_name)]
            if 'Java crash: true' in file_contents:
                java_crash_dirs += [str(dir_name)]
            if 'Compiler crash: true' in file_contents:
                compiler_crash_dirs += [str(dir_name)]
            if 'Execute crash: true' in file_contents:
                execute_crash_dirs += [str(dir_name)]
            if 'Different output: true' in file_contents:
                different_output_dirs += [str(dir_name)]
            if 'Exit code: 2' in file_contents or 'Exit code: 124' in file_contents:
                timeout_dirs += [str(dir_name)]
    return resolution_dirs, java_crash_dirs, compiler_crash_dirs, execute_crash_dirs, different_output_dirs, timeout_dirs

def copyDirectories(directories, directory):
    for dir_name in directories:
        dir_path = os.path.join('output', dir_name)
        res_path = os.path.join(directory, dir_name)
        copy_tree(dir_path, res_path)

if __name__ == '__main__':
    timeout_start = time.time()
    timeout_secs = 60 * 60 * 8 # 8 hours
    base_command = "timeout 60 java -jar /home/alex/fuzz-d/app/build/libs/app.jar fuzz"
    while time.time() < timeout_start + timeout_secs:
        for i in range(10):
            print(time.time())
            os.system(base_command)

        resolution_dirs, java_crash_dirs, compiler_crash_dirs, execute_crash_dirs, different_output_dirs, timeout_dirs = getErrorDirectories()
        copyDirectories(resolution_dirs, os.path.join('result', 'resolver'))
        copyDirectories(java_crash_dirs, os.path.join('result', 'java_crash'))
        copyDirectories(compiler_crash_dirs, os.path.join('result', 'compiler_crash'))
        copyDirectories(execute_crash_dirs, os.path.join('result', 'execute_crash'))
        copyDirectories(different_output_dirs, os.path.join('result', 'different_output'))
        copyDirectories(timeout_dirs, os.path.join('result', 'timeout'))

        os.system('rm -rf output/*')
        os.system('rm -rf success/*')
