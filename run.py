#!/usr/bin/env python3 

import time
import os

from distutils.dir_util import copy_tree

def getErrorDirectories():
    compiler_crash_dirs = []
    execute_crash_dirs = []
    different_output_dirs = []
    timeout_dirs = []
    success_dirs = []
    directory = 'output'
    filename = 'fuzz-d.log'
    for dir_name in os.listdir(directory):
        if os.path.isfile(os.path.join(directory, dir_name)):
            continue

        f = os.path.join(directory, dir_name, filename)
        if not os.path.isfile(f):
            continue

        with open(f) as open_file:
            flag = False
            file_contents = open_file.read()
            if 'Java Compiler crash: true' in file_contents:
                flag = True
                java_crash_dirs += [str(dir_name)]
            if 'Compiler crash: true' in file_contents:
                flag = True
                compiler_crash_dirs += [str(dir_name)]
            if 'Execute crash: true' in file_contents:
                flag = True
                execute_crash_dirs += [str(dir_name)]
            if 'Different output: true' in file_contents:
                flag = True
                different_output_dirs += [str(dir_name)]
            if 'Exit code: 2' in file_contents or 'Exit code: 124' in file_contents:
                flag = True
                timeout_dirs += [str(dir_name)]
            if not flag:
                success_dirs += [str(dir_name)]
    return java_crash_dirs, compiler_crash_dirs, execute_crash_dirs, different_output_dirs, timeout_dirs, success_dirs

def copyDirectories(directories, directory):
    for dir_name in directories:
        dir_path = os.path.join('output', dir_name)
        res_path = os.path.join(directory, dir_name)
        copy_tree(dir_path, res_path)

if __name__ == '__main__':
    timeout_start = time.time()    
    timeout_secs = 60 * 60 * 8 # 8 hours
    while time.time() < timeout_start + timeout_secs:
        for i in range(10):
            print(time.time())
            os.system("timeout 60 java -jar /home/alex/fuzz-d/app/build/libs/app.jar fuzz")
        
        java_crash_dirs, compiler_crash_dirs, execute_crash_dirs, different_output_dirs, timeout_dirs, success_dirs = getErrorDirectories()
        copyDirectories(java_crash_dirs, os.path.join('result', 'java_crash'))
        copyDirectories(compiler_crash_dirs, os.path.join('result', 'compiler_crash'))
        copyDirectories(execute_crash_dirs, os.path.join('result', 'execute_crash'))
        copyDirectories(different_output_dirs, os.path.join('result', 'different_output'))
        copyDirectories(timeout_dirs, os.path.join('result', 'timeout'))
        copyDirectories(success_dirs, os.path.join('result', 'success'))
    
        os.system('rm -rf output/*')
