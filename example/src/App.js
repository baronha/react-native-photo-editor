import React, { useState } from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import ImagePicker from '@baronha/react-native-multiple-image-picker';
import { Image } from 'react-native';
import { TouchableOpacity } from 'react-native';
import PhotoEditor from 'react-native-photo-editor';

const { width } = Dimensions.get('window');

const App = () => {
  const [photo, setPhoto] = useState({});

  const openPicker = () => {
    ImagePicker.openPicker()
      .then((result) => {
        console.log('result', result);
        setPhoto(result[0]);
      })
      .then((e) => {
        // console.log('error');
      });
  };

  const onEdit = async () => {
    try {
      const path = await PhotoEditor.open({
        // path: photo.path.replace('file://', ''),
      });
      setPhoto({
        ...photo,
        path,
      });
      console.log('resultEdit', path);
    } catch (e) {
      console.log('e', e);
    }
  };

  return (
    <View style={style.container}>
      <TouchableOpacity onPress={onEdit}>
        {photo?.path && (
          <Image
            style={style.image}
            source={{
              uri: photo.path,
            }}
          />
        )}
      </TouchableOpacity>
      <TouchableOpacity style={style.openPicker} onPress={onEdit}>
        <Text style={style.titleOpen}>Open Picker</Text>
      </TouchableOpacity>
    </View>
  );
};

export default App;

const style = StyleSheet.create({
  container: {},
  image: {
    width,
    height: width,
  },
  openPicker: {
    margin: 12,
    backgroundColor: '#000',
    justifyContent: 'center',
    alignItems: 'center',
  },
  titleOpen: {
    color: '#fff',
    fontWeight: 'bold',
    padding: 12,
  },
});
