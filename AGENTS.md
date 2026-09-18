# Agent Directives & Permissions

## 1. Autonomous Execution
- The agent is granted full permission to work autonomously from start to finish within this project: `D:\ISTAD\Bachelor_Degree\Year1\Semester2\OOP\FinalProject\hantedSchool_3\`.
- Proactively create files, edit code, generate assets, compile (`javac`, `mvn`), run test scripts, and complete tasks end-to-end without pausing for step-by-step confirmation.

## 2. STRICT SAFETY CONSTRAINT: Drive C Deletion Safeguard
- **CRITICAL**: The agent is **STRICTLY FORBIDDEN** from deleting, removing, purging, or permanently altering any files or directories on **Drive C (`C:\...`)** without explicitly asking the user and receiving affirmative permission first.
- Destructive commands such as `rm`, `Remove-Item`, `del`, `rmdir`, `erase`, or file unlinking targeting `C:\` require manual user consent under all circumstances.
- File modifications and file creation within the project directory on `D:\` remain fully autonomous.
