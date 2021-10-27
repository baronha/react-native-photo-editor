import { Dimensions } from 'react-native';

const { width, height } = Dimensions.get('window');

const sizes = {
  width,
  height,
  //space
  spaceMd: 24,
  spaceSm: 16,
  spaceTiny: 12,
  //font size
  fontMd: 14,
  fontLg: 20,
  fontUpper: 32,
};

export default sizes;
