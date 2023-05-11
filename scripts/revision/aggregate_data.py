import os
import sys

depth = int(input())
invalid_commits = set([c.replace('\n', '') for c in open('invalid_commits.txt', 'r').readlines()])
log_generated = set(['patch' + (e.split('patch')[-1].split('.')[0]) for e in os.popen(f'ls d{depth}/output/logs/sahab*').read().split('\n')[:-1]])
invalid_logs = set()
for l in log_generated:
    if l.split('_')[-1] in invalid_commits:
        invalid_logs.add(l)
log_generated = log_generated - invalid_logs
memory_issue = set(['patch' + (e.split('patch')[-1].split('.')[0]) for e in os.popen(f'find d{depth}/output/logs/sahab_patch* -type f -print0 | sudo xargs -0 grep -l "Memory"').read().split('\n')[:-1]])
diff_computed = set(['patch' + (e.split('patch')[-1].split('.')[0]) for e in os.popen(f'find d{depth}/output/logs/sahab_patch* -type f -print0 | sudo xargs -0 grep -l "firstOriginalUniqueStateSummary=UniqueStateSummary"').read().split('\n')[:-1]])
output_generated = set(['patch' + (e.split('patch')[-1].split('.')[0]) for e in os.popen(f'find d{depth}/output/patch* -type f -print0 | sudo xargs -0 grep -l "a"').read().split('\n')[:-1]])
with_diff = set(['patch' + (e.split('patch')[-1].split('.')[0]) for e in os.popen(f'find d{depth}/output/patch* -type f -print0 | sudo xargs -0 grep -l "only occurs"').read().split('\n')[:-1]])

log_without_diff = log_generated - with_diff

print(len(log_generated), len(with_diff), len(log_generated - with_diff), len(log_without_diff - (log_without_diff - diff_computed)), len(memory_issue - with_diff))

for p in log_generated - with_diff:
    p = p.split('_')[0]
    print(p)
