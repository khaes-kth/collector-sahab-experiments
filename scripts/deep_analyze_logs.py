import os
import sys

analyzed = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'ls output/logs/sahab_*.log').read().split('\n')[:-1]])
ui_manipulated = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'find output/logs/diff*.err -type f -print0 | xargs -0 grep -l "UI manipul"').read().split('\n')[:-1]])
unexpected = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'find output/logs/diff*.err -type f -print0 | xargs -0 grep -l "Unexpected"').read().split('\n')[:-1]])
with_diff = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'find output/state_diffs/* -type f -print0 | xargs -0 grep -l "only occurs"').read().split('\n')[:-1]])
memory_limit = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'find output/logs/* -type f -print0 | xargs -0 grep -l "OutOfMemory"').read().split('\n')[:-1]])
no_report = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'find output/logs/* -type f -print0 | xargs -0 grep -l "FileNotFound"').read().split('\n')[:-1]])
npe = set([e.split('_')[-1].split('.')[0] for e in os.popen(f'find output/logs/* -type f -print0 | xargs -0 grep -l "NullPointerException"').read().split('\n')[:-1]])
#nd = set([e.split('_')[-1].split('.')[0] for e in os.popen('find output/logs/diff*.err -type f -size -3090c').read().split('\n')[:-1]])
#nd = nd - unexpected - memory_limit - no_report - npe
nd = set([e.split('_')[-1].split('.')[0] for e in os.popen('find output/logs/* -type f -print0 | xargs -0 grep -l "{firstUniqueStateLine=null, firstUniqueVarValLine=null, firstUniqueStateHash=null, firstUniqueVarVal=\'null\'}, firstPatchedUniqueStateSummary=UniqueStateSummary{firstUniqueStateLine=null, firstUniqueVarValLine=null, firstUniqueStateHash=null, firstUniqueVarVal=\'null\'}, originalUniqueReturn=UniqueReturnSummary{firstUniqueVarValLine=null, firstUniqueReturnLine=null, firstUniqueReturnHash=null, firstUniqueVarVal=\'null\'}, patchedUniqueReturn=UniqueReturnSummary{firstUniqueVarValLine=null, firstUniqueReturnLine=null, firstUniqueReturnHash=null, firstUniqueVarVal=\'null\'}}"').read().split('\n')[:-1]])
time_limit = analyzed - ui_manipulated - unexpected - npe - memory_limit
print(f'all: {len(analyzed)}, ui_manipulated: {len(ui_manipulated)}, unexpected: {len(unexpected)}, with_diff: {len(with_diff)}, ml: {len(memory_limit)}, no_report_file: {len(no_report)}, npe: {len(npe)}, ne: {len(nd)}, npe: {len(npe)}, tl: {len(no_report)}, nus: {len(nd)}, nuv: {len(ui_manipulated - with_diff - nd)}, nd: {len(ui_manipulated - with_diff)}')
#print('time_limit', len(time_limit), len(no_report))
#print(analyzed - no_report - nd - (ui_manipulated - with_diff - nd) - with_diff - memory_limit - npe)
#print(ui_manipulated - with_diff)
#print(unexpected)
#print(len(set([e.split('_')[-1].split('.')[0] for e in os.popen('find output/logs/* -type f -print0 | xargs -0 grep -l "{firstUniqueStateLine=null, firstUniqueVarValLine=null, firstUniqueStateHash=null, firstUniqueVarVal=\'null\'}, firstPatchedUniqueStateSummary=UniqueStateSummary{firstUniqueStateLine=null, firstUniqueVarValLine=null, firstUniqueStateHash=null, firstUniqueVarVal=\'null\'}, originalUniqueReturn=UniqueReturnSummary{firstUniqueVarValLine=null, firstUniqueReturnLine=null, firstUniqueReturnHash=null, firstUniqueVarVal=\'null\'}, patchedUniqueReturn=UniqueReturnSummary{firstUniqueVarValLine=null, firstUniqueReturnLine=null, firstUniqueReturnHash=null, firstUniqueVarVal=\'null\'}}"').read().split('\n')[:-1]])))
print(ui_manipulated - with_diff - nd)
