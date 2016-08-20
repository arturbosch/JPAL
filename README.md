# JPAL - Javaparser Aid Library

Jpal is a library for javaparser which provides additional features
like cross referencing, qualified types and other useful classes.

Development on GitLab: https://gitlab.com/arturbosch/JPAL 
Mirror on GitHub: https://github.com/arturbosch/JPAL

## Table of contents
1. [Build](#build)
2. [Cross Referencing](#cross)
3. [Helpers](#helpers)
4. [FAQ](#faq)
4. [Contribute](#contribute)
4. [Who uses JPAL](#usage)

## <a name="build">Build</a>

- Make sure gradle is installed
- Download the jpal git repository
- Use gradle clean build publishToMavenLocal
- This will install the library into your local m2 folder

## <a name="cross">Cross Referencing</a>

Is provided by type resolving and caching of qualified types, paths
and compilation units.

Choose a mechanism:

### CompilationStorage - ahead of time

Should be used before you process the AST as the compilation storage
analyzes given project path and caches all compilation units.
Compilation units are wrapped into CompilationInfo's which stores 
additional information like used qualified types within this unit.

```java
CompilationStorage.create(projectPath)
```

Obtaining a compilation info can be done through two methods:

```java 
def maybeInfo = CompilationStorage.getCompilationInfo(path)
def maybeInfo = CompilationStorage.getCompilationInfo(qualifiedType)
```

### CompilationTree - just in time

The project root path should be provided before the AST usage.

```java
CompilationTree.registerRoot(projectPath)
```

Now the compilation unit can be obtained by searching for paths
and qualified types. Is no compilation unit cached the path/type is 
searched within the project and later cached.

```java 
def maybeInfo = CompilationTree.findCompilationInfo(path)
def maybeInfo = CompilationTree.findCompilationInfo(qualifiedType)
```

### QualifiedType

The easiest way to obtain a qualified type is to use the TypeHelper:

```java
ClassOrInterfaceDeclaration clazz = ...;
`Optional<QualifiedType> maybeType = TypeHelper.getQualifiedType(clazz);
```

This has the advantage that only a class or interface declaration
is needed but costs extra time finding the declared compilation unit.

If the compilation unit is known or you are sure that the class is 
within a specific package, use:

```java
QualifiedType type = TypeHelper.getQualifiedType(ClassOrInterfaceDeclaration n, CompilationUnit unit)
QualifiedType type = TypeHelper.getQualifiedTypeFromPackage(TypeDeclaration n, PackageDeclaration packageDeclaration)
```

### Resolver

If you want the qualified type but only have a `Type` or any subclass
use the `Resolver` and `ResolutionData` classes.

The ResolutionData stores package and import information in a specific way
and can be constructed from a compilation unit.

```java
CompilationUnit unit = ...
ResolutionData.of(unit)
```

The Resolver tries to build the qualified type for given `Type` and the 
`ResolutionData`:

```java
Resolver.getQualifiedType(data, type)
```

The resolver checks for following situations:

- Type is a primitive or boxed primitive
- Type is in the imports and can be easily constructed
- Type is in within an asterisk import
- Type is in java.lang
- Type is within the package

## <a name="helpers">Helpers - Useful Classes</a>

- ClassHelper - signatures, scope check etc
- LocaleVariableHelper - finds locale variable within a method
- MethodHelper - getter/setter/anonymous method checks etc
- NodeHelper - find (declaring) nodes
- TypeHelper - get qualified types
- VariableHelper - transforms parameters/fields/locale vars to JpalVariables

## <a name="faq">FAQ</a>

#### How to create qualified types for inner classes?

```java
CompilationUnit unit = ...
ResolutionData data = ...
def outerClass = new ClassOrInterfaceType("OuterClass")
def handler = new InnerClassesHandler(unit)
String name = handler.getUnqualifiedNameForInnerClass(outerClass) 
def innerClass = new ClassOrInterfaceType(name)

def qualifiedType = Resolver.getQualifiedType(data, innerClass)
```

```java 
...
def unqualifiedName = ClassHelper.appendOuterClassIfInnerClass(ClassOrInterfaceDeclaration n)
def qualifiedType = Resolver.getQualifiedType(data, new ClassOrInterfaceType(unqualifiedName))
```

#### How to get all inner classes names

```java 
TypeHelper.getQualifiedTypesOfInnerClasses(CompilationUnit unit)
```

#### How to create a type signature?

```
ClassHelper.createFullSignature(ClassOrInterfaceDeclaration n)
```
This creates a full signature of given class with respect to anonymous or inner class checks.

If these checks are not needed use:
```java 
ClassHelper.createSignature(ClassOrInterfaceDeclaration n)
```

#### What information is stored within a CompilationInfo?

```
final QualifiedType qualifiedType
final CompilationUnit unit
final Path path
final List<QualifiedType> usedTypes
final Set<QualifiedType> innerClasses
```

## <a name="contribute">How to contribute?</a>

- Report Bugs or make feature requests through issues
- Push merge requests

## <a name="usage">Who uses JPAL?</a>

- SmartSmells (https://gitlab.com/arturbosch/SmartSmells/)