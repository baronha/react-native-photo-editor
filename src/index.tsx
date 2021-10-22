import { NativeModules } from 'react-native';

export type Options = {
  path: String;
  stickers: Array<String>;
};

type PhotoEditorType = {
  open(option: Object): Promise<String>;
};

const { PhotoEditor } = NativeModules;

export default PhotoEditor as PhotoEditorType;
