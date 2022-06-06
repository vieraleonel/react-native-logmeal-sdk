// @ts-nocheck
import React, { useEffect, useRef, useState } from 'react';

import {
  StyleSheet,
  View,
  Dimensions,
  TouchableOpacity,
  Text,
  ScrollView,
} from 'react-native';
import {
  DepthCamera,
  checkDeviceSupport,
  DepthCameraRefType,
  TakePhotoResponse,
} from 'react-native-logmeal-sdk';
import { check, request, PERMISSIONS, RESULTS } from 'react-native-permissions';

const App = () => {
  const [takePhotoResult, setTakePhotoResult] =
    useState<TakePhotoResponse | null>(null);
  const ref = useRef<DepthCameraRefType>(null);

  const _checkDeviceSupport = async () => {
    const cameraPermissionCheck = await check(PERMISSIONS.ANDROID.CAMERA);
    if (cameraPermissionCheck !== RESULTS.GRANTED) {
      await request(PERMISSIONS.ANDROID.CAMERA);
    }
    const checkDeviceSupportResult = await checkDeviceSupport();
    console.log('checkDeviceSupport', checkDeviceSupportResult);
  };

  const _takePhoto = () => {
    if (ref.current) {
      ref.current.takePhoto().then((result) => {
        setTakePhotoResult(result);
      });
    }
  };

  useEffect(() => {
    _checkDeviceSupport();
  }, []);

  return (
    <ScrollView style={styles.container}>
      {/* @ts-ignore */}
      <DepthCamera style={styles.box} ref={ref} />
      <TouchableOpacity style={styles.touchable} onPress={_takePhoto}>
        <Text>Touch Me</Text>
      </TouchableOpacity>
      {takePhotoResult && (
        <View style={styles.resultsContainer}>
          <Text style={styles.resultsTitle}>Focal length</Text>
          <Text>{JSON.stringify(takePhotoResult.focalLength)}</Text>

          <Text style={styles.resultsTitle}>Principal point</Text>
          <Text>{JSON.stringify(takePhotoResult.principalPoint)}</Text>

          <Text style={styles.resultsTitle}>Depth Image Size</Text>
          <Text>{JSON.stringify(takePhotoResult.depthImageSize)}</Text>

          <Text style={styles.resultsTitle}>Depth Image Uri</Text>
          <Text>{takePhotoResult.depthImageUri}</Text>

          <Text style={styles.resultsTitle}>Image Uri</Text>
          <Text>{takePhotoResult.imageUri}</Text>
        </View>
      )}
    </ScrollView>
  );
};

export default App;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  box: {
    width: Dimensions.get('window').width,
    height: Dimensions.get('window').width,
  },
  touchable: {
    marginTop: 15,
    width: 150,
    height: 50,
    backgroundColor: '#32CD32',
    alignItems: 'center',
    justifyContent: 'center',
    alignSelf: 'center',
  },
  resultsContainer: {
    marginTop: 15,
    flex:1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  resultsTitle: {
    fontWeight: 'bold',
  },
});
