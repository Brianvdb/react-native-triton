
# react-native-triton-player

## Getting started

`$ npm install react-native-triton-player --save`

### Mostly automatic installation

`$ react-native link react-native-triton-player`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-triton-player` and add `RNTritonPlayer.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNTritonPlayer.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.tritonsdk.RNTritonPlayerPackage;` to the imports at the top of the file
  - Add `new RNTritonPlayerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-triton-player'
  	project(':react-native-triton-player').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-triton-player/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-triton-player')
  	```


## Usage
```javascript
import RNTritonPlayer from 'react-native-triton-player';

// TODO: What to do with the module?
RNTritonPlayer;
```
  