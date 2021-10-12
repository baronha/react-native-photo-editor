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


  const onEdit = async () => {
    debugger
    try {
      const path = await PhotoEditor.open({path: photo.path});
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
      <TouchableOpacity style={style.openPicker} onPress={onEdit}>
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
