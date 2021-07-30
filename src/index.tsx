import { NativeModules } from 'react-native';

type PhotoEditorType = {
  open(option: Object): Promise<String>;
};

const { PhotoEditor } = NativeModules;

export default PhotoEditor as PhotoEditorType;
