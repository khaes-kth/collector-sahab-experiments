import sys
import os
import multiprocessing as mp
from os.path import exists
import time

OUTPUT_DIR='/mnt/ssd2/khesoem/sahab-revision/first-run/d0/output'
DRR_DIR='/mnt/ssd2/khesoem/sahab-revision/first-run/d0/collector-sahab-experiments'
MAIN_DIR='/mnt/ssd2/khesoem/sahab-revision/first-run/d0'
DEPTH='0'

def current_milli_time():
    return round(time.time() * 1000)

def process(patchName):

    os.chdir(DRR_DIR)
    os.system(f'git checkout -f master')

    bugId = patchName.split('-')[1] + '-' + patchName.split('-')[2]
    proj = bugId.split('-')[0]
    bugNum = int(bugId.split('-')[1])
    
    with open(f'{DRR_DIR}/projects-metadata/{proj}-metadata.csv') as file:
        lines = file.readlines()
        lines = [line.rstrip() for line in lines]
        testName = lines[bugNum].split(',')[1].replace('"', "").split(';')[0].split('::')[0]

    GHDiffPath = f'{OUTPUT_DIR}/gh_full_{patchName}.html'
    #os.system(f'cp {hitDiffReportPath} {GHDiffPath}')
    os.system(f'git checkout -f {patchName}')
    rightCommit = os.popen(f'git log --grep="applied" --format="%H"').read().split('\n')[0]
    changedFile = '/'.join(os.popen(f'git diff-tree --no-commit-id --name-only -r {rightCommit}').read().split('\n')[0].split('/')[1:])
    os.system(f'git checkout -f HEAD~1')

    os.chdir(MAIN_DIR)

    sahabCommand = f'timeout 1800 java -Xmx40g -Xms4g -jar collector-sahab.jar -p {DRR_DIR}/{bugId}/ -r {rightCommit} -l e5d67a8 -c {changedFile} --slug ASSERT-KTH/collector-sahab-experiments --execution-depth {DEPTH} --selected-tests {testName} --output-path {OUTPUT_DIR}/{patchName}_{rightCommit}.html --cleanup 1>> {OUTPUT_DIR}/logs/sahab_{patchName}_{rightCommit}.log 2>> {OUTPUT_DIR}/logs/sahab_{patchName}_{rightCommit}.err'

    curTime = current_milli_time()
    os.system(sahabCommand)
    timeSpent = str(current_milli_time() - curTime)
    #print(sahabCommand)
    os.system(f'echo "Sahab spent time for {rightCommit} is {timeSpent}" >> {OUTPUT_DIR}/logs/sahab_{patchName}_{rightCommit}.log') 
    

def main(argv):
    with open(argv[0]) as file:
        lines = file.readlines()
        lines = [line.rstrip() for line in lines]
        for line in lines:
            patchName = line.split(' ')[-1].split('/')[0]
            if not ('-Time-' in patchName or '-Math-' in patchName):
                continue
            process(patchName)

if __name__ == "__main__":
    main(sys.argv[1:])
