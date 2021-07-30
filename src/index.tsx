import { NativeModules } from 'react-native';

type PhotoEditorType = {
  multiply(a: number, b: number): Promise<number>;
};

const { PhotoEditor } = NativeModules;

export default PhotoEditor as PhotoEditorType;
