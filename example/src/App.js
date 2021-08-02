import React, { useState } from 'react';
import {
  SafeAreaView,
  Text,
  StyleSheet,
  Dimensions,
  TouchableOpacity,
  Image,
} from 'react-native';
import ImagePicker from '@baronha/react-native-multiple-image-picker';
import PhotoEditor from '@baronha/react-native-photo-editor';

const { width } = Dimensions.get('window');

const App = () => {
  const [photo, setPhoto] = useState({});

  const openPicker = () => {
    ImagePicker.openPicker({ singleSelectedMode: true })
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
        // path: 'https://images.unsplash.com/photo-1627532383712-981b37b4085c?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
        path: photo.path,
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
    <SafeAreaView style={style.container}>
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
      <TouchableOpacity style={style.openPicker} onPress={openPicker}>
        <Text style={style.titleOpen}>Open Picker</Text>
      </TouchableOpacity>
    </SafeAreaView>
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
