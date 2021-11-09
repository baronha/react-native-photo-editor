/* eslint-disable react-native/no-inline-styles */
import React, { useEffect, useState } from 'react';
import { View, StyleSheet, StatusBar, Image as RNImage } from 'react-native';
import MasonryList from '@react-native-seoul/masonry-list';

import colors from '../../themes/colors';
import sizes from '../../themes/sizes';
import { Image } from '../../components';
import images from '../../assets/images';

import { Header, ButtonGroup } from './components';
import { ActivityIndicator } from 'react-native';

// const cover =
//   'https://raw.githubusercontent.com/baronha/react-native-photo-editor/dev/example/src/assets/images/adam-creator.png';

const clientId = 'JgOxefg8FPPlKNXCGLeawYcPc67V-C2PE6Z84tAjk_c';
const unsplashApi = `https://api.unsplash.com/collections/mBVp78Oe5kY/photos?per_page=100&client_id=${clientId}`;

const spacing = 4;

const ITEM_WIDTH = sizes.width / 2 - spacing / 2;

const Home = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response = await fetch(unsplashApi);
      console.log('response: ', response);
      if (response.ok) {
        const list = await response.json();
        // console.log('response: ', list);
        if (list?.length) setData(list);
      }
    } catch (e) {
    } finally {
      setLoading(false);
    }
  };

  const renderItem = ({ item, i }) => {
    const { urls, width: w, height: h } = item;
    const url = urls?.regular;

    // const width = w *
    const height = (h * ITEM_WIDTH) / w;

    return (
      <View
        style={[
          style.imageView,
          {
            height,
            width: ITEM_WIDTH,
            alignSelf: i % 2 === 0 ? 'flex-start' : 'flex-end',
          },
        ]}
        key={i}
      >
        <Image url={url} style={[style.image, {}]} resizeMode={'contain'} />
      </View>
    );
  };

  return (
    <View style={style.container}>
      <StatusBar barStyle={'light-content'} backgroundColor={colors.dark} />
      <MasonryList
        data={data}
        renderItem={renderItem}
        keyExtractor={(item, index) => index.toString()}
        ListHeaderComponent={
          <>
            <RNImage source={images.adamCreator} style={style.cover} />
            <Header />
            <ButtonGroup />
          </>
        }
        contentContainerStyle={style.content}
        numColumns={2}
        showsVerticalScrollIndicator={false}
        ListFooterComponent={
          loading && <ActivityIndicator color={colors.white} />
        }
      />
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
    paddingBottom: sizes.spaceMd * 2,
  },
  cover: {
    width: sizes.width,
    height: sizes.width / 1.5,
  },
  imageView: {
    marginBottom: spacing,
  },
  image: {
    ...StyleSheet.absoluteFill,
  },
});
