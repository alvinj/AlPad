-injars       alpadAntOutput.jar:../lib/macios7components_2.12-0.3.jar:/Users/Al/bin/scala-2.12.2/lib/scala-library.jar
-outjars      alpad.jar
-libraryjars  ../lib/rt.jar
#-libraryjars  <java.home>/lib/rt.jar

-dontobfuscate

-dontshrink
-dontoptimize
-dontwarn
-dontnote

-keep public class com.alvinalexander.alpad.AlPad {
    public static void main(java.lang.String[]);
}

