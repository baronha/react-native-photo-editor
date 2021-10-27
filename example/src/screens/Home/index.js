import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  StatusBar,
  ScrollView,
  Image,
} from 'react-native';

import images from '../../assets/images';
import colors from '../../themes/colors';
import sizes from '../../themes/sizes';

import { Header, ButtonGroup } from './components';

const Home = () => {
  return (
    <View style={style.container}>
      <StatusBar barStyle={'light-content'} backgroundColor={colors.dark} />
      <ScrollView contentContainerStyle={style.content}>
        <Image
          source={images.adamCreator}
          style={style.cover}
          resizeMode={'contain'}
        />
        <Header />
        <ButtonGroup />
      </ScrollView>
    </View>
  );
};

export default Home;

const style = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.dark,
  },
  content: {
    // backgroundColor: 'red',
  },
  cover: {
    width: sizes.width,
    height: sizes.width / 1.5,
  },
});
