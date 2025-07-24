# <div align="center"><a href="#dummy"><img src="https://github.com/user-attachments/assets/82a68998-c0c8-4a3e-9908-f46048bc8429" alt="🔍 jstrdups"></a></div>

<p align="center">
<a href="https://github.com/foldright/jstrdups/actions/workflows/ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/foldright/jstrdups/ci.yaml?branch=main&logo=github&logoColor=white" alt="Github Workflow Build Status"></a>
<a href="https://github.com/foldright/jstrdups/releases/download/v0.2.1/jstrdups-0.2.1.zip"><img src="https://img.shields.io/github/downloads/foldright/jstrdups/v0.2.1/jstrdups-0.2.1.zip.svg?logoColor=white&logo=GitHub" alt="GitHub release download - jstrdups.zip)"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-339933?logo=openjdk&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/foldright/jstrdups?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://github.com/foldright/jstrdups/releases"><img src="https://img.shields.io/github/release/foldright/jstrdups.svg" alt="GitHub Releases"></a>
<a href="https://github.com/foldright/jstrdups"><img src="https://img.shields.io/github/repo-size/foldright/jstrdups?logoColor=white&logo=GitHub" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/foldright/jstrdups"><img src="https://img.shields.io/badge/Gitpod-ready to code-339933?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
</p>

A tool to find duplicate string literals in java source code files.
This helps identify potential code smells and opportunities for string constant refactoring.

Having multiple occurrences of the same string literal across a codebase can indicate:

- Potential need for constants/enums
- Opportunities for centralization and better maintainability
- Possible copy-paste code smells

## Installation

```shell
brew install foldright/tap/jstrdups
```

Or <a href="https://github.com/foldright/jstrdups/releases/download/v0.2.1/jstrdups-0.2.1.zip"><img src="https://img.shields.io/github/downloads/foldright/jstrdups/v0.2.1/jstrdups-0.2.1.zip.svg?logoColor=white&logo=GitHub" alt="GitHub release download - jstrdups.zip)"></a>

## Usage

```
$ jstrdups -h
Usage: jstrdups [-ahtvV] [-d=<minDuplicateCount>] [-l=<minStrLen>]
                [-L=<javaLanguageLevel>] <projectRootDir>
Find duplicate string literals in java files under current directory
      <projectRootDir>     Project root dir, default is the current directory
  -a, --absolute-path      always print the absolute path of java files,
                             default is false
  -d, --min-duplicate-count=<minDuplicateCount>
                           minimal duplicate count of string literal to find,
                             default is 2
  -h, --help               Show this help message and exit.
  -l, --min-string-len=<minStrLen>
                           minimal string length(char count) of string literal
                             to find, default is 4
  -L, --java-lang-level=<javaLanguageLevel>
                           set java language level of input java sources. Valid
                             keys: JAVA_1_0, JAVA_1_1, JAVA_1_2, JAVA_1_3,
                             JAVA_1_4, JAVA_5, JAVA_6, JAVA_7, JAVA_8, JAVA_9,
                             JAVA_10, JAVA_10_PREVIEW, JAVA_11,
                             JAVA_11_PREVIEW, JAVA_12, JAVA_12_PREVIEW,
                             JAVA_13, JAVA_13_PREVIEW, JAVA_14,
                             JAVA_14_PREVIEW, JAVA_15, JAVA_15_PREVIEW,
                             JAVA_16, JAVA_16_PREVIEW, JAVA_17,
                             JAVA_17_PREVIEW, JAVA_18, JAVA_19, JAVA_20,
                             JAVA_21. default is JAVA_21
  -t, --include-test-dir   include test dir, default is false
  -v, --verbose            print messages about progress
  -V, --version            Print version information and exit.
```
