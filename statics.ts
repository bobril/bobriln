import * as gw from './gateway';

export function dismissKeyboard() {
    gw.prepareToCallNativeByName("b.dismissKeyboard");
    gw.callNativeIgnoreResult();
}
