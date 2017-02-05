# JPAL - Javaparser Aid Library

[![build status](https://gitlab.com/arturbosch/jpal/badges/master/build.svg)](https://gitlab.com/arturbosch/jpal/commits/master)
[ ![Download](https://api.bintray.com/packages/arturbosch/code-analysis/Jpal/images/download.svg) ](https://bintray.com/arturbosch/code-analysis/Jpal/_latestVersion)

Jpal is a library for javaparser which provides additional features
like cross referencing, qualified types, symbol solving and other useful classes.

Development on GitLab: https://gitlab.com/arturbosch/JPAL 
Mirror on GitHub: https://github.com/arturbosch/JPAL

## Table of contents
1. [Build](#build)
2. [Cross Referencing](#cross)
3. [Symbol solving](#symbols)
3. [Helpers](#helpers)
4. [FAQ](#faq)
4. [Contribute](#contribute)
4. [Who uses JPAL](#usage)

## <a name="build">Build</a>

#### Bintray

For Gradle use:

```
repositories {
    maven {
        url  "http://dl.bintray.com/arturbosch/generic" 
    }
}
```

`compile 'io.gitlab.arturbosch.jpal:jpal:1.0.RC3'`

For Maven see `https://bintray.com/arturbosch/generic/JPAL` -> SetUp

```
<dependency>
  <groupId>io.gitlab.arturbosch.jpal</groupId>
  <artifactId>jpal</artifactId>
  <version>1.0.RC3</version>
</dependency>
```

#### Raw
- Make sure gradle is installed
- Download the jpal git repository
- Use gradle clean build publishToMavenLocal
- This will install the library into your local m2 folder

## <a name="cross">Cross Referencing</a>

Is provided by type resolving and caching of qualified types, paths
and compilation units via a CompilationStorage.

### CompilationStorage

Should be used before you process the AST as the compilation storage
analyzes given project path and caches all compilation units.
Compilation units are wrapped into CompilationInfo's which stores 
additional information like used qualified types within this unit.

```CompilationStorage storage = JPAL.new(projectPath)```

Obtaining a compilation info can be done through two methods:

```java 
def maybeInfo = CompilationStorage.getCompilationInfo(path)
def maybeInfo = CompilationStorage.getCompilationInfo(qualifiedType)
```

##### Updating the CompilationStorage

`UpdatableCompilationStorage storage = JPAL.updatable()`

This creates a new empty updatable storage instance.

`UpdatableCompilationStorage storage = JPAL.initializedUpdatable(projectPath)`

This creates a precompiled storage with all java sub paths down from project path.
All methods of __CompilationStorage__ to retrieve __CompilationInfo__ work also on updatable storages.

Use methods following methods to update the storage:
```java
storage.updateCompilationInfo(List<Path> paths)
storage.relocateCompilationInfo(Map<Path, Path> relocates)
storage.removeCompilationInfo(List<Path> paths)
```

### QualifiedType

The easiest way to obtain a qualified type is to use the Resolver:

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

The preferred way is to use a Resolver.

### Resolver

If you want the qualified type but only have a `Type` or any subclass
use the `Resolver` and `CompilationInfo` classes.

```java
CompilationStorage storage = JPAL.new(projectPath)
Resolver resolver = new Resolver(storage)
CompilationInfo info = storage.getCompilationInfo(myPath/myType)
ClassOrInterfaceType searchedType = ...
resolver.resolveType(seachedType, info)
```

The resolver checks for following situations:

- Type is a primitive or boxed primitive
- Type is in the imports and can be easily constructed
- Type is in within an asterisk import
- Type is in java.lang
- Type is within the package

## <a name="symbols">Symbol Solving</a>

SimpleName classes of __javaparser__ are treated as symbols. A Resolver can resolve symbols
and find the type and declaration of a symbol.

```java
CompilationStorage storage = JPAL.new(projectPath)
Resolver resolver = new Resolver(storage)
CompilationInfo info = storage.getCompilationInfo(myPath/myType)
resolver.resolveSymbol(symbol,info)
```

## <a name="helpers">Helpers - Useful Classes</a>

- ClassHelper - signatures, scope check etc
- LocaleVariableHelper - finds locale variable within a method
- MethodHelper - getter/setter/anonymous method checks etc
- NodeHelper - find (declaring) nodes
- TypeHelper - get qualified types
- VariableHelper - transforms parameters/fields/locale vars to JpalVariables

## <a name="faq">FAQ</a>

#### How to create qualified types for inner classes?

If you use the CompilationStorage with a CompilationUnit, you can get all
inner classes with `info.getInnerClasses()`.

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
final TypeDeclaration mainType
final ResolutionData data
final Map<QualifiedType, TypeDeclaration> innerClasses
```

## <a name="contribute">How to contribute?</a>

- Report Bugs or make feature requests through issues
- Push merge requests

## <a name="usage">Who uses JPAL?</a>

- SmartSmells (https://gitlab.com/arturbosch/SmartSmells/)