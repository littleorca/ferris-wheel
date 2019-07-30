const path = require("path");
// const TSDocgenPlugin = require("react-docgen-typescript-webpack-plugin");
module.exports = async ({ config }) => {
    config.module.rules.push({
        test: /\.(ts|tsx)$/,
        use: [
            {
                loader: require.resolve("awesome-typescript-loader")
            }
        ],
    });
    config.resolve.extensions.push(".ts", ".tsx");
    config.resolve.modules = [
        ...(config.resolve.modules || []),
        path.resolve('src'),
    ];
    return config;
};