var argscheck = require('cordova/argscheck'),
    cordova = require('cordova'),
    exec = require('cordova/exec');

var VR = function () {

};

VR.prototype.startPlaying = function (url, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "Vr", "startPlaying", [url]);
};

VR.prototype.stopPlaying = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "Vr", "stopPlaying", []);
};

var vr = new VR();

module.exports = vr;
