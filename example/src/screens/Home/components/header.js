import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

import { Image } from '../../../components';
import colors from '../../../themes/colors';
import sizes from '../../../themes/sizes';

const AVATAR_URL =
  'https://raw.githubusercontent.com/baronha/react-native-photo-editor/dev/example/src/assets/images/david.png';

const Header = () => {
  return (
    <View style={style.container}>
      <View style={style.avatarView}>
        <View style={style.overlayAvatar} />
        <Image style={style.avatar} url={AVATAR_URL} />
      </View>
      <View style={style.info}>
        <Text style={style.name}>Adam</Text>
        <Text style={style.userName}>@adamchimbe</Text>
        <Text numberOfLines={2} style={style.description}>
          Mọi chuyện sẽ bớt quan trọng hơn khi bạn mắc ỉa. Ok nha fen tui!
        </Text>
      </View>
    </View>
  );
};

export default Header;

const AVATAR_SIZE = sizes.width / 3;

const style = StyleSheet.create({
  container: {
    padding: sizes.spaceMd,
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
  },
  overlayAvatar: {
    width: AVATAR_SIZE / 1.4,
    height: AVATAR_SIZE / 1.4,
    position: 'absolute',
    backgroundColor: colors.white32,
    borderRadius: sizes.width,
    borderWidth: 1.5,
    borderColor: colors.white,
  },
  avatarView: {
    // backgroundColor: colors.white,
    borderRadius: sizes.width,
    // borderWidth: 2.5,
    // borderColor: colors.white,
    justifyContent: 'center',
    alignItems: 'center',
  },
  info: {
    flex: 1,
    paddingLeft: sizes.spaceSm,
  },
  name: {
    fontSize: sizes.fontUpper,
    color: colors.white,
    fontFamily: 'Avenir',
    fontWeight: 'bold',
  },
  userName: {
    color: colors.white,
    fontFamily: 'Avenir',
    paddingBottom: sizes.spaceSm,
    fontSize: sizes.fontMd,
  },
  description: {
    color: colors.white92,
    fontSize: sizes.fontMd,
  },
});
