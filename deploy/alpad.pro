-injars       alpadAntOutput.jar
-outjars      alpad.jar
-libraryjars  <java.home>/lib/rt.jar

-dontobfuscate

-keep public class com.alvinalexander.alpad.AlPad {
    public static void main(java.lang.String[]);
}

