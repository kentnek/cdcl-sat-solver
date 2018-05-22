# cdcl-sat-solver

A CDCL SAT solver implemented in Java, for a university course.


## Prerequisites

- Java 8


## Test cases

### Sample test cases

Several sample CNF formulas are given in the folder `inputs`.

### Generate new test cases

Run the `main()` method in `src/com/kentnek/cdcl/FormulaHelper.java`. Generated CNF formulas will be written to the
`inputs/generated` folder. 


## Run the solver

1. Update the value of the `INPUT_FILE_PATH` constant in `src/com/kentnek/cdcl/Main.java` with the path of the desired
test case.
2. Run the `main()` method.
3. If the formula is satisfiable, the solver will output an assignment and verify it.
4. If the formula is unsatisfiable, the solver will generate a refutation proof, verify it and write it to an output file
in the `proofs` folder.


## Authors

* **Kent Nguyen** - *Initial work* - [Kent](https://github.com/kentnek)


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


## Acknowledgments

* Professor Kuldeep S. Meel, for his thorough introduction to CDCL solvers and guidance on the project.
* The Handbook of Satisfiability.
