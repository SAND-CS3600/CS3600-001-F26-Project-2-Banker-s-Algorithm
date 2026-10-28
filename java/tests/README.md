# Test Plan

Tests for README Sections 19 (error handling) and 20 (Tests 1-8).

## Running

From the repo root:

```bash
./java/tests/run_tests.sh
```

This does two things:

1. **`InvariantCheck.java`** runs Tests 1-8 and the bank-side edge cases directly
   against `BankImpl` and reports PASS/FAIL. After every operation it checks that
   `Need = Maximum - Allocation`, nothing is negative, `Available + Allocation`
   still equals the system total, and that denied or rejected operations changed
   nothing. Exits with status 1 if anything fails.
2. Pipes each file in `commands/` into `TestBankers`, and runs the bad input files
   and bad arguments. Outputs are saved in `tests/output/` (not committed) to
   compare against the tables below.

Compiled classes go to a temporary folder, so nothing new needs to be ignored.

## Resources: use 10 6 7 8, not 10 5 7 8

With the README's `10 5 7 8`, Customer 4's maximum for R1 is 6 but only 5 R1
exist. C4 can never finish, so the initial state is already unsafe and **every
request is denied**. That is correct behavior (InvariantCheck tests it), but it
makes Tests 2, 5, 6 and 8 impossible to show. All tests use `10 6 7 8`.

Worth asking the instructor whether the 5 was intentional.

## Section 20 tests

Run as `java TestBankers inPutFile.txt 10 6 7 8 < tests/commands/<file>`.

| Test | File | Commands | Expected |
|---|---|---|---|
| 1 Initial state | `test1_initial_state.txt` | `*` | Available `10 6 7 8`, Allocation all 0, Need = Maximum |
| 2 Valid safe request | `test2_valid_safe_request.txt` | `RQ 0 3 1 2 1` | Approved. Available `7 5 5 7`, C0 Allocation `3 1 2 1`, C0 Need `3 3 5 2` |
| 3 Exceeds need | `test3_request_exceeds_need.txt` | `RQ 1 5 0 0 0` | Denied (C1 needs only 4 of R0). State unchanged |
| 4 Not enough available | `test4_insufficient_available.txt` | `RQ 0 3 1 2 1`, `RQ 4 5 6 7 5` | Approved, then Denied (6 of R1 requested, 5 available). Request equals C4's Need, so only the Available check stops it |
| 5 Unsafe request | `test5_unsafe_request.txt` | `RQ 0 3 1 2 1`, `RQ 4 0 0 1 0` | Approved, then **Denied as unsafe**. See below |
| 6 Release | `test6_release_resources.txt` | `RQ 0 3 1 2 1`, `RL 0 1 0 1 0` | Available `8 5 6 7`, C0 Allocation `2 1 1 1`, C0 Need `4 3 6 2` |
| 7 Invalid release | `test7_invalid_release.txt` | `RQ 0 3 1 2 1`, `RL 0 4 0 0 0`, `RL 1 1 0 0 0` | Both releases rejected (C0 holds 3 of R0, C1 holds nothing). State same as after the request |
| 8 Multiple transactions | `test8_multiple_transactions.txt` | `RQ 0 3 1 2 1`, `RQ 1 2 1 1 1`, `RL 0 1 0 1 0`, `RQ 2 1 2 1 1` | All approved. Available `5 2 4 5`; Allocation C0 `2 1 1 1`, C1 `2 1 1 1`, C2 `1 2 1 1` |

### Why Test 5 is unsafe

C0 and C4 can each need all 7 of R2, and only 7 exist. After C0 holds 2 of R2,
giving C4 just 1 more leaves 4 available, but C0 still needs 5 and C4 would need
6. C1, C2 and C3 can finish, but they hold nothing, so finishing frees nothing.
C0 and C4 are stuck. The request passes the Need and Available checks, so only
the safety algorithm can catch it.

## Section 19 edge cases

### Bad commands: `commands/edge_cases.txt`

Each line should print an error (or Denied) and leave the bank unchanged. The
final `*` should show the initial state.

| Input | Problem |
|---|---|
| `XX 0 1 1 1 1` | Unknown command |
| `rq 0 1 1 1 1` | Lowercase command (reject, or accept if the team decides to) |
| *(blank line)* | Should be ignored, not crash |
| `RQ` / `RQ 0` | Missing customer and/or values |
| `RQ 7 1 1 1 1` / `RQ -1 1 1 1 1` | Invalid customer number |
| `RQ a 1 1 1 1` | Non-numeric customer |
| `RQ 0 1 1 1` / `RQ 0 1 1 1 1 1` | Too few / too many values |
| `RQ 0 -1 0 0 0` | Negative value |
| `RQ 0 a b c d` / `RQ 0 1.5 0 0 0` | Non-numeric values |
| `RL 0 1 0 0 0` | Releasing resources C0 does not hold |
| `RL 9 1 0 0 0` | Invalid customer |
| `RL 0 -1 0 0 0` | Negative release (would act like an unchecked request) |
| `RL 0 1 1` | Too few values |

### Bad input files and arguments: `output/bad_input.out`

Each should print a clear error and exit, not a stack trace.

| Run | Problem |
|---|---|
| `bad-files/non_numeric.txt` | `x` in a row |
| `bad-files/too_few_values.txt` / `too_many_values.txt` | Row length doesn't match the resource count |
| `bad-files/too_few_rows.txt` | Only 3 customers instead of 5 |
| `bad-files/negative_value.txt` | Negative maximum |
| `bad-files/empty.txt` | Empty file |
| `missing_file.txt` | File does not exist |
| `inPutFile.txt` (no resources) | Usage message |
| `inPutFile.txt 10 6 7` / `10 6 7 8 9` | Resource count doesn't match the file's rows |
| `inPutFile.txt 10 -6 7 8` / `10 six 7 8` | Negative / non-numeric resource |

## Submitting

After the final group run of all tests, from the repo root:

```bash
git status                                   # nothing important uncommitted
git bundle create team_<team_name>.bundle --all
git bundle verify team_<team_name>.bundle
```

`*.bundle` is in `.gitignore`, so the bundle never gets committed.
