import {NativeModules} from 'react-native';
import {Platform, NativeEventEmitter, DeviceEventEmitter} from 'react-native';

const NativeRNTritonPlayer = NativeModules.RNTritonPlayer;

class RNTritonPlayer {

    static configure({brand}) {
        NativeRNTritonPlayer.configure(brand)
    }

    static play(tritonName, tritonMount) {
        NativeRNTritonPlayer.play(tritonName, tritonMount);
    }

    static pause() {
        NativeRNTritonPlayer.pause();
    }

    static unPause() {
        NativeRNTritonPlayer.unPause();
    }

    static stop() {
        NativeRNTritonPlayer.stop();
    }

    static quit() {
        NativeRNTritonPlayer.quit();
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

    static addStateChangeListener(callback) {
        if (Platform.OS === 'ios') {
            const tritonEmitter = new NativeEventEmitter(NativeRNTritonPlayer);
            tritonEmitter.addListener('stateChanged', callback);
        } else {
            DeviceEventEmitter.addListener('stateChanged', callback);
        }
    }
}

export default RNTritonPlayer;
