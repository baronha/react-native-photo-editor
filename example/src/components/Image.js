import React, { useState } from 'react';
import {
  TouchableOpacity,
  Image as RNImage,
  StyleSheet,
  ViewPropTypes,
} from 'react-native';
import PhotoEditor from '@baronha/react-native-photo-editor';
import PropTypes from 'prop-types';

import { stickers } from '../assets/data';

const Image = (props) => {
  const { url, style: imageStyle } = props;
  const [path, setPath] = useState(url);

  const onEdit = async () => {
    try {
      const result = await PhotoEditor.open({
        path,
        stickers,
      });
      console.log('resultEdit: ', result);
      setPath(result);
    } catch (e) {
      console.log('error', e);
    } finally {
      console.log('finally');
    }
  };

  return (
    <TouchableOpacity activeOpacity={0.9} onPress={onEdit} style={imageStyle}>
      <RNImage {...props} source={{ uri: path }} style={style.image} />
    </TouchableOpacity>
  );
};

export default Image;

Image.propTypes = {
  url: PropTypes.string,
  style: ViewPropTypes.style,
};

const style = StyleSheet.create({
  image: {
    ...StyleSheet.absoluteFill,
  },
});
