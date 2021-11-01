import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import Home from './Home';
export const routes = {
  Home: {
    name: 'Home',
    component: Home,
  },
};

const { Navigator, Screen } = createNativeStackNavigator();
console.disableYellowBox = true;

const App = () => {
  return (
    <NavigationContainer>
      <Navigator
        screenOptions={{ headerShown: false }}
        initialRouteName={routes.Home.name}
      >
        <Screen {...routes.Home} />
      </Navigator>
    </NavigationContainer>
  );
};

export default App;
