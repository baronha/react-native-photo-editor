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
  const remoteURL =
    'https://images.unsplash.com/photo-1634915728822-5ad85582837a?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=774&q=80';

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
        path: remoteURL,
        // path: photo.path,
        stickers,
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
        {/* {photo?.path && (
          <Image
            style={style.image}
            source={{
              uri: photo.path,
            }}
          />
        )} */}
        <Image
          style={style.image}
          source={{
            uri: remoteURL,
          }}
        />
      </TouchableOpacity>
      {/* <TouchableOpacity style={style.openPicker} onPress={openPicker}>
        <Text style={style.titleOpen}>Open Picker</Text>
      </TouchableOpacity> */}
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

const stickers = [
  'https://cdn-icons-png.flaticon.com/512/5272/5272912.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272913.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272916.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272918.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272920.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272923.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272925.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272926.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272929.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272931.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272932.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272934.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272936.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272939.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272940.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272942.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272944.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272948.png',
  'https://cdn-icons-png.flaticon.com/512/5272/5272950.png',
];
