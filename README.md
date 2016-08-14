# JPAL - Javaparser Aid Library

Jpal is a library

## Build

- Make sure gradle is installed
- Download the jpal git repository
- Use gradle clean build publishToMavenLocal
- This will install the library into your local m2 folder

## Cross referencing

Is provided by type resolving and caching of qualified types, paths
and compilation units.

Choose a mechanism:

### CompilationStorage - ahead of time

Should be used before you process the AST as the compilation storage
analyzes given project path and caches all compilation units.
Compilation units are wrapped into CompilationInfo's which stores 
additional information like used qualified types within this unit.

```CompilationStorage.create(projectPath)```

Obtaining a compilation info can be done through two methods:

```java 
def maybeInfo = CompilationStorage.getCompilationInfo(path)
def maybeInfo = CompilationStorage.getCompilationInfo(qualifiedType)
```

### CompilationTree - just in time

The project root path should be provided before the AST usage.

```CompilationTree.registerRoot(projectPath)```

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

```Resolver.getQualifiedType(data, type)```

The resolver checks for following situations:

- Type is a primitive or boxed primitive
- Type is in the imports and can be easily constructed
- Type is in java.lang
- Type is within the package

- Star imports are not considered right now (planned for 1.0)

## FAQ

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

#### What information is stored within a CompilationInfo

```
final QualifiedType qualifiedType
final CompilationUnit unit
final Path path
final List<QualifiedType> usedTypes
final Set<QualifiedType> innerClasses
```