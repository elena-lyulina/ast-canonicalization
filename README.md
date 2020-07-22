# ast-canonicalization


[![elena-lyulina](https://circleci.com/gh/elena-lyulina/ast-canonicalization/tree/setup.svg?style=shield)](https://app.circleci.com/pipelines/github/elena-lyulina/ast-canonicalization?branch=setup)

This repository contains AST-transformations that transform AST to a canonicalized state. These transformations are applied for the AST that is parsed by [this](src/main/python/pythonparser-3.py) **python3** parser. Source code is in [this](https://github.com/Varal7/pythonparser) repository and [this](https://eth-sri.github.io/py150) project.

 Transformations that are implemented in this project are mentioned in the following articles:
 * K. Rivers, Data-Driven Hint Generation in Vast Solution Spaces a Self-Improving Python Programming Tutor [here](https://www.researchgate.net/profile/Kenneth_Koedinger2/publication/283468835_Data-Driven_Hint_Generation_in_Vast_Solution_Spaces_a_Self-Improving_Python_Programming_Tutor/links/5702a59e08ae646a9da8771b.pdf)
 * K. Rivers and K. R. Koedinger, A Canonicalizing Model for Building Programming Tutors [here](http://www.krivers.net/files/its2012-paper.pdf)
 * Songwen Xu and Yam San Chee, Transformation-based Diagnosis of Student Programs for Programming Tutoring Systems [here](https://www.researchgate.net/profile/Peter_Xu10/publication/3188319_Transformation-based_diagnosis_of_student_programs_for_programming_tutoring_systems/links/5631c81408ae3de9381d0f64.pdf)
