"use strict";
var gw = require('./gateway');
function dismissKeyboard() {
    gw.prepareToCallNativeByName("b.dismissKeyboard");
    gw.callNativeIgnoreResult();
}
exports.dismissKeyboard = dismissKeyboard;
