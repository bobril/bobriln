"use strict";
var b = require('./bobril');
b.addEvent("onChange", 10, function (ev, node) {
    return b.bubble(node, "onChange", ev.value) !== undefined;
});
b.addEvent("backPressed", 10, function () {
    return b.broadcast("onBackPressed", undefined) !== undefined;
});
