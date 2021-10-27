import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import colors from '../../../themes/colors';
import sizes from '../../../themes/sizes';

const ButtonGroup = () => {
  return (
    <View style={style.container}>
      <TouchableOpacity activeOpacity={0.9} style={style.messageButton}>
        <Text style={style.messageText}>Message</Text>
      </TouchableOpacity>
      {/* <TouchableOpacity
        style={[style.messageButton, { marginHorizontal: sizes.spaceMd }]}
      >
        <Text style={style.messageText}>Message</Text>
      </TouchableOpacity> */}
    </View>
  );
};

export default ButtonGroup;

const style = StyleSheet.create({
  container: {
    flexDirection: 'row',
    paddingHorizontal: sizes.spaceMd,
  },
  messageButton: {
    backgroundColor: colors.white,
    alignItems: 'center',
    flex: 1,
  },
  messageText: {
    padding: sizes.spaceTiny,
    fontWeight: 'bold',
    fontFamily: 'Avenir',
    fontSize: sizes.fontLg,
    textAlign: 'center',
  },
});
