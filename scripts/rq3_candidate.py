import json
import os
import shlex
import subprocess

from selenium import webdriver
from selenium.webdriver.common.by import By

import requests

with open(f'candidate_prs.txt') as f:
  CANDIDATE_PRS = f.readlines()

LINK_TO_REPOSITORIES = [i.split('pull')[0] for i in CANDIDATE_PRS]
PR_PATCHES = [
  f'https://patch-diff.githubusercontent.com/raw/{i.split("/")[3]}/{i.split("/")[4]}/pull/{i.split("/")[6]}.patch'
  for i in CANDIDATE_PRS
]


def _clover(goal):
  return f'org.openclover:clover-maven-plugin:4.4.1:{goal}'

def _clone(url):
  repo_name = url.split('/')[-2]
  if os.path.exists(f'/home/assert/Desktop/testrepos/{repo_name}'):
    return 0, repo_name
  
  p = subprocess.run(['git', 'clone', url], cwd='/home/assert/Desktop/testrepos')
  return p.returncode, repo_name

def _clover_instrument(repo_name):
  p = subprocess.run(['mvn', _clover('instrument'), "-Dmaven.clover.excludesFile=target"], cwd=f'/home/assert/Desktop/testrepos/{repo_name}')
  return p.returncode

def _clover_report(repo_name):
  p = subprocess.run(['mvn', _clover('clover')], cwd=f'/home/assert/Desktop/testrepos/{repo_name}')
  return p.returncode

def _apply_patch_and_get_commits(repo_name, url):
  if not os.path.exists(f'/home/assert/Desktop/testrepos/{repo_name}.patch'):
  
    r = requests.get(url)
    with open(f'/home/assert/Desktop/testrepos/{repo_name}.patch', 'w+') as f:
      f.write(r.text)
  # subprocess.run(['git', 'am', f'/home/assert/Desktop/testrepos/{repo_name}.patch'], cwd=f'/home/assert/Desktop/testrepos/{repo_name}')

  p = subprocess.run(['git', 'log', '--pretty=format:%H', '-n', '2'], cwd=f'/home/assert/Desktop/testrepos/{repo_name}', capture_output=True)
  class_file = subprocess.run(f'grep \'^+++\' {repo_name}.patch  | sed -e \'s#+++ [ab]/##\'', shell=True, cwd=f'/home/assert/Desktop/testrepos', capture_output=True)
  return p.stdout.decode('utf-8').split('\n'), class_file.stdout.decode('utf-8').strip()

def _run_MLF(project, filename, left, right):
  cmd = (
    "java "
    f"-classpath /home/assert/Desktop/assert-achievements/collector-sahab/target/collector-sahab-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
    "se.kth.debug.MatchedLineFinder "
    f"{project} {shlex.quote(filename)} {left} {right}"
  )
  subprocess.run(cmd, shell=True)
  subprocess.run(['git', 'checkout', '-'], cwd=f'/home/assert/Desktop/testrepos/{repo_name}')


def _get_matched_lines():
  mlf_output = json.load(open('input-left.txt'))
  return mlf_output[0]['breakpoints']

def _run_selenium(class_file):
  print(class_file)
  url_name = class_file.split('main/java/')[1][:-5]

  # p = subprocess.Popen(['python3', '-m', 'http.server'], cwd=f'/home/assert/Desktop/testrepos/{repo_name}/target/site/clover')
  driver = webdriver.Firefox()
  driver.get(f'http://0.0.0.0:8000/{url_name}.html')
  lines = _get_matched_lines()

  tests = set()
  for line in lines:
    element = driver.find_element(By.CSS_SELECTOR, f'tr#l{line} td:nth-child(2)')
    driver.execute_script("arguments[0].scrollIntoView(true)", element)
    driver.execute_script('arguments[0].click()', element)
    test_list = driver.execute_script(f"return document.querySelectorAll('tbody#tests-body-inline-{line} td:nth-child(2) a')")
    for i in test_list:
      test_method = i.text
      last_dot = test_method.rfind('.')
      
      formatted_test_method = f'{test_method[:last_dot]}::{test_method[last_dot+1:]}'
      tests.add(formatted_test_method)
      print(formatted_test_method, line)
  
  # p.terminate()
  return tests


def _run_collector_sahab(project, commits, class_file, tests):
  PATH_TO_COLLECTOR_SAHAB = '/home/assert/Desktop/assert-achievements/collector-sahab/'
  p = subprocess.run([
    'python3',
    'scripts/bribe-sahab.py',
    '-p',
    project,
    '-l',
    commits[1],
    '-r',
    commits[0],
    '-t',
    " ".join(tests),
    '-c',
    class_file
  ], cwd=PATH_TO_COLLECTOR_SAHAB)

pr_number = 29
for i in LINK_TO_REPOSITORIES[pr_number:pr_number+1]:
  print(f'Scanning {i} ...')
  clone_cmd = _clone(i)
  repo_name = clone_cmd[1]
  if clone_cmd[0] != 0:
    print(f'Failed to clone {repo_name}')
    continue


  # clover_instrument_cmd = _clover_instrument(repo_name)
  # if clover_instrument_cmd != 0:
  #   print(f'Failed to instrument {repo_name}')
  #   continue

  # clover_report_cmd = _clover_report(repo_name)
  # if clover_report_cmd != 0:
  #   print(f'Failed to generate report for {repo_name}')
  #   continue
  

  commits, class_file = _apply_patch_and_get_commits(repo_name, PR_PATCHES[pr_number])
  print(commits)
  
  _run_MLF(f'/home/assert/Desktop/testrepos/{repo_name}', class_file, commits[1], commits[0])

  tests = _run_selenium(class_file)
  print(tests)

  # _run_collector_sahab(f'/home/assert/Desktop/testrepos/{repo_name}', commits, class_file, tests)

  # print(f'{i} analyzed successfully.')


