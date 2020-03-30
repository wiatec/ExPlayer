# ExPlayer

Android player library with four engine(native, Ijk, Exo, Vlc)

### Install
```
implementation 'com.ex.lib:explayer:1.0.2'
```

### Config

module build.gradle

defaultConfig scope
```
ndk {
    abiFilters "armeabi-v7a"
}
```

android scope
```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8     
    targetCompatibility JavaVersion.VERSION_1_8
}
```
