import React from 'react';
import { View, StyleSheet, StatusBar, ScrollView } from 'react-native';

import colors from '../../themes/colors';
import sizes from '../../themes/sizes';
import { Image } from '../../components';

import { Header, ButtonGroup } from './components';

const url =
  'https://raw.githubusercontent.com/baronha/react-native-photo-editor/dev/example/src/assets/images/adam-creator.png';

const Home = () => {
  return (
    <View style={style.container}>
      <StatusBar barStyle={'light-content'} backgroundColor={colors.dark} />
      <ScrollView contentContainerStyle={style.content}>
        <Image url={url} style={style.cover} />
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
