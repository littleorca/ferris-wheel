const proxy = require('http-proxy-middleware');

module.exports = function (app) {
    app.use(proxy("/wsapi", {
        "target": "ws://127.0.0.1:8080",
        "ws": true
    }));
}

