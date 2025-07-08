# 🔍 jstrdups

<p align="center">
<a href="https://github.com/foldright/jstrdups/actions/workflows/ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/foldright/jstrdups/ci.yaml?branch=main&logo=github&logoColor=white" alt="Github Workflow Build Status"></a>
<a href="https://github.com/foldright/jstrdups/releases/download/v0.1.0/jstrdups-0.1.0.zip"><img src="https://img.shields.io/github/downloads/foldright/jstrdups/v0.1.0/jstrdups-0.1.0.zip.svg?logoColor=white&logo=GitHub" alt="GitHub release download - jstrdups.zip)"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-339933?logo=openjdk&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/foldright/jstrdups?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://github.com/foldright/jstrdups/releases"><img src="https://img.shields.io/github/release/foldright/jstrdups.svg" alt="GitHub Releases"></a>
<a href="https://github.com/foldright/jstrdups"><img src="https://img.shields.io/github/repo-size/foldright/jstrdups?logoColor=white&logo=GitHub" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/foldright/jstrdups"><img src="https://img.shields.io/badge/Gitpod-ready to code-339933?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
</p>

A tool to find duplicate string literals in source code files.
This helps identify potential code smells and opportunities for string constant refactoring.

Having multiple occurrences of the same string literal across a codebase can indicate:

- Potential need for constants/enums
- Opportunities for centralization and better maintainability
- Possible copy-paste code smells
