const path = require("path");
// const TSDocgenPlugin = require("react-docgen-typescript-webpack-plugin");
module.exports = async ({config, mode}) => {
    config.module.rules.push({
        test: /\.(ts|tsx)$/,
        loader: require.resolve("awesome-typescript-loader")
    });
    //   config.plugins.push(new TSDocgenPlugin()); // optional
    config.resolve.extensions.push(".ts", ".tsx");
    return config;
};