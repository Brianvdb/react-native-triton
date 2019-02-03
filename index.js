import {NativeModules} from 'react-native';
import {Platform, NativeEventEmitter, DeviceEventEmitter} from 'react-native';

const NativeRNTritonPlayer = NativeModules.RNTritonPlayer;

class RNTritonPlayer {

    static play(tritonName, tritonMount) {
        NativeRNTritonPlayer.play(tritonName, tritonMount);
    }

    static addStreamChangeListener(callback) {
        if (Platform.OS === 'ios') {
            const tritonEmitter = new NativeEventEmitter(NativeRNTritonPlayer);
            tritonEmitter.addListener('streamChanged', callback);
        } else {
            DeviceEventEmitter.addListener('streamChanged', callback);
        }
    }

    static addTrackChangeListener(callback) {
        if (Platform.OS === 'ios') {
            const tritonEmitter = new NativeEventEmitter(NativeRNTritonPlayer);
            tritonEmitter.addListener('trackChanged', callback);
        } else {
            DeviceEventEmitter.addListener('trackChanged', callback);
        }
    }
}

export default RNTritonPlayer;
