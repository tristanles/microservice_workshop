# oo_boot_camp_kotlin_reference.04
Copyright (c) 2018 by Fred George  
May be used freely except for training; license required for training.

OO Boot Camp reference implementation for Netherlands client on 
1-5 October 2018, in Kotlin.

If creating sample code, create a new github public repository, named
consistently with other projects, including README.md and .gitignore. 
Checkout this skeleton to start.

If building a reference project, add skeleton files to an appropriate 
directory in the oo_boot_camps repository.

Create a new Kotlin project, targeting a subdirectory of
oo_boot_camps. Override the module defaults to create *exercises*
as the default module, placing it as a subdirectory.

Create a *tests* **module** under the root  
- Create a directory *test* under the tests module
- Create a directory *unit* under the test directory
- Delete the directory *src* under the tests module
- Delete the directory *src* under the overall project, leaving only src tag in the *example* module
- In settings for the tests module, establish *test* directory as a test directory 
- In settings for the tests module, establish a dependency on the *exercises* module
Create a test directory parallel to the src package  
Create a unit directory within the test directory  

Tag src and test directories as such:
- File/Project Structure...
- Select "Modules"
    - Tag src directory as Sources
    - Tag test directory as Tests
    - Click "OK"

Choose the Java JDK and Java levels to use (I use the latest of both)

Create a RectangleTest Kotlin class in the test/unit package.
- Create the first test:
    @Test fun area() {
        assertEquals(24.0, Rectangle(4, 6).area);
    }  
- Highlight @Test let IntelliSense suggest a JUnit binding
- Choose the JUnit 5 option

Confirm that everything builds correctly (and necessary libraries exist).
Start implementing Rectangle in a new rectangle package until all compile 
errors are gone. Then run all the tests in the test package.

And add this README.
