import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';
import colors from '../../../themes/colors';
import sizes from '../../../themes/sizes';
import icons from '../../../assets/icons';

const ButtonGroup = () => {
  return (
    <View style={style.container}>
      <TouchableOpacity activeOpacity={0.9} style={style.messageButton}>
        <Text style={style.messageText}>Message</Text>
      </TouchableOpacity>
      <TouchableOpacity style={[style.button, { marginLeft: sizes.spaceSm }]}>
        <Image style={style.icon} source={icons.tag} />
      </TouchableOpacity>
    </View>
  );
};

export default ButtonGroup;

const BUTTON_HEIGHT = 40;
const ICON_SIZE = 24;

const style = StyleSheet.create({
  container: {
    flexDirection: 'row',
    paddingHorizontal: sizes.spaceMd,
    paddingBottom: sizes.spaceLg,
  },
  messageButton: {
    flex: 1,
    backgroundColor: colors.white,
    alignItems: 'center',
    justifyContent: 'center',
    height: BUTTON_HEIGHT,
  },
  messageText: {
    paddingHorizontal: sizes.spaceUpperTiny,
    fontWeight: 'bold',
    fontFamily: 'Avenir',
    fontSize: sizes.fontLg,
    textAlign: 'center',
  },
  button: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.dark2,
    height: BUTTON_HEIGHT,
    width: BUTTON_HEIGHT,
    // height:
  },
  icon: {
    width: ICON_SIZE,
    height: ICON_SIZE,
  },
});
