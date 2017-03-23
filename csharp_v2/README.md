# MicroService Workshop C# Implementation by Fred George
Copyright 2015-17 by Fred George. May be copied with this notice, but not used in classroom training.

C# is easier to setup than Java since there is a primary tool, Visual Studio, for
development. I don't do C# development professionally, so my tool knowledge is limited.
For this exercise, I use the latest Community edition of Visual Studio, plus Resharper
from JetBrains.

There are tests for a common library. Getting those tests to run is _not_ required
for the class.

## Visual Studio setup
Open the sample code:
- From the Start Page, select "Open Project..."
- Navigate to the csharp_v2\src\MicroServiceWorkshop directory
- Select the solution file (not project file) titled "MicroServiceWorkshop"
- Click "Open"

If you have Resharper installed, you can run the tests to confirm all your libraries
have been installed and are accessible. Via the Solution Explorer, select 
"MicroServiceWorkshop.Tests" by right clicking, and select "Run Unit Tests". Don't
worry about this if you don't have Resharper.

## Visual Studio execution
Each MicroService runs independently. So we need to setup up a separate project for 
each service. 

## Next Steps
You're running MicroServices. Now let's write some more to run with these two. Note
that in C#, you will have to define a new _project_ for each new MicroService.
