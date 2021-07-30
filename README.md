# react-native-photo-editor

🌄 Image editor using native modules for iOS and Android. Inherit from 2 available libraries, [Brightroom](https://github.com/muukii/Brightroom) (iOS) and [PhotoEditor](https://github.com/burhanrashid52/PhotoEditor) (Android)

# Note
This library is for personal use, so if you customize your style or change something, please fork this library and check the detailed documentation in the original library.

###[iOS](https://github.com/muukii/Brightroom)
###[Android](https://github.com/burhanrashid52/PhotoEditor) 

## Installation

```sh
yarn add @baronha/react-native-photo-editor && cd ios/ && pod install && cd..
```

## Usage

```js
import PhotoEditor from "@baronha/react-native-photo-editor";

// ...

const result = await PhotoEditor.open([options](#Options));
```

## Options
| Property                         |       Type   | Default value |  Platform  | Description                              |
| -------------------------------- | :----------: | :-----------: | :--------: | :--------------------------------------- |
| uri                 | string         | required          | 

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
