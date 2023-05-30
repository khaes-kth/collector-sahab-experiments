# COLLECTOR-SAHAB EXPERIMENTS

This repo contains the results of our experiments with [COLLECTOR-SAHAB](https://github.com/algomaster99/collector-sahab/).

## RQ1: Effectiveness

Output of running COLLECTOR-SAHAB on MVN-DRR patches are generated using [PatchExplainer](https://github.com/khaes-kth/PatchExplainer). These outputs are saved in the `sahab-diff-reports` folder. For `depth={i}`, the `sahab-diff-reports/d{i}/output` contains the generated augmented diffs in the `state_diffs` folder.

Also, results of didiffff per patch are listed in [this link](https://github.com/khaes-kth/didiffff-drr/blob/master/results/aggregated-res.csv).

## RQ2: Applicability

All the `212` commits we shortlisted are listed [here](/rq2_commits.csv). It
has 5 columns: `Commit`, `Status`, `Time(seconds)`, `Notes`, `Command`.

1. `Commit`: The link to the commit.
2. `Status`: The status of the commit. It takes the following values.
    ```
    'S': 'Success',
    'SD': 'Sucess (no exec diff)',
    'TCC': 'Classpath building failed but clover runs the project',
    'NE': 'Breakpoints and returns not encountered (probably class prepare event is not triggered)',
    'OM': 'Out of memory(collector-sahab)',
    'OMP': 'Out of memory(patch explainer)',
    'MLF': 'Bug in MLF',
    'DM': 'Default method interface',
    'CS': 'Bug in collector-sahab',

    # We ignore the following commits and come down to 50 from 212.
    'M': '(Ignore) Merge commit',
    'CF': '(Ignore) Could not find test',
    'TC': '(Ignore) Test compile/classpath failed/dependency issue with test',
    'UP': '(Ignore) Patch not covered',
    'NUJ': '(Ignore) Cannot be detected by Surefire',
    'SW': '(Ignore) Single test class did not compile',
    'SMC': '(Ignore) Bug in Single method change', 
    ```
3. `Time(seconds)`: The time taken to run the diff. This was needed when our tool
    used JDI as it used to take a lot of time. It is not the case after instrumention.
    Hence, it is obsolete.

4. `Notes`: Any notes about the commit. It takes the following values. Usually,
    it has links to the hosted diffs. However, if they are absent, please refer
    to the links in the paper itself.

5. `Command`: Command for reproducing it. We accept only few are there, but
    all them are reproducible. The command is of the form:
    ```sh
    java -jar main/target/collector-sahab-0.5.2-SNAPSHOT-jar-with-dependencies.jar \
        -p <project_on_local> \
        -c <relative_path_to_source_class_file> \
        -l <left_commit> \
        -r <right_commit> \
        --slug <org/repository_name> \
        -t <test_class_name>
    ```

    You can get the argument for `-t` like so:

    1. Go to the hosted diff.
    2. You will see the name of the differentiating tests.
    3. Get the corresponding *fully qualified name* of the test class or method.
    4. Pass it to the `-t` argument.
        > If you leave it blank, all tests that surefire can discover will be run.

The results shown in Table 3 of the paper are generated using a
[python notebook](/rq2.ipynb). It also includes the link to the
original notebook used for the paper.

## RQ3: Manual Analysis

The answer for correctness and understandibility are discussd by the authors.
Since, human brain is not deterministic, it may not be reproducible. However,
you can generate the report for `rand-exc=true` by adding `--exclude-random-values`
to the command in the `rq2_commits.csv` file.

> This feature was implemented in
[`v0.4.0`](https://github.com/ASSERT-KTH/collector-sahab/releases/tag/v0.4.0).


## RQ4: User Study

Just trust us on this please :smile: .
